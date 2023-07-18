package com.epam.training.microservicefoundation.storageservice.service.implementation;

import com.epam.training.microservicefoundation.storageservice.mapper.DeleteStorageMapper;
import com.epam.training.microservicefoundation.storageservice.mapper.GetStorageMapper;
import com.epam.training.microservicefoundation.storageservice.mapper.SaveStorageMapper;
import com.epam.training.microservicefoundation.storageservice.model.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.entity.Storage;
import com.epam.training.microservicefoundation.storageservice.model.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.model.exception.ExceptionSupplier;
import com.epam.training.microservicefoundation.storageservice.repository.CloudStorageRepository;
import com.epam.training.microservicefoundation.storageservice.repository.StorageRepository;
import com.epam.training.microservicefoundation.storageservice.service.StorageService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@Service
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {
  private static final Logger log = Loggers.getLogger(StorageServiceImpl.class);
  private final StorageRepository storageRepository;
  private final CloudStorageRepository cloudStorageRepository;
  private final SaveStorageMapper saveStorageMapper;
  private final GetStorageMapper getStorageMapper;
  private final DeleteStorageMapper deleteStorageMapper;
  @Autowired
  public StorageServiceImpl(StorageRepository storageRepository, CloudStorageRepository cloudStorageRepository,
      SaveStorageMapper saveStorageMapper, GetStorageMapper getStorageMapper, DeleteStorageMapper deleteStorageMapper) {
    this.storageRepository = storageRepository;
    this.cloudStorageRepository = cloudStorageRepository;
    this.saveStorageMapper = saveStorageMapper;
    this.getStorageMapper = getStorageMapper;
    this.deleteStorageMapper = deleteStorageMapper;
  }

  @Transactional
  @Override
  public Mono<GetStorageDTO> save(final Mono<SaveStorageDTO> saveStorageDTOMono) {
    log.info("Saving a storage.");
    return saveStorageDTOMono
        .map(saveStorageMapper::toEntity)
        .flatMap(this::createBucket)
        .flatMap(storageRepository::save)
        .map(getStorageMapper::toDto)
        .onErrorMap(DataIntegrityViolationException.class, error -> ExceptionSupplier.entityAlreadyExists(Storage.class, error).get());
  }

  @Override
  public Mono<GetStorageDTO> getById(final long id) {
    log.info("Finding a storage with id '{}'", id);
    return storageRepository.findById(id)
        .flatMap(this::checkIfExists)
        .map(getStorageMapper::toDto)
        .switchIfEmpty(Mono.error(ExceptionSupplier.entityNotFound(Storage.class, id)));
  }

  @Override
  public Flux<GetStorageDTO> findAllByType(final StorageType type) {
    log.info("Finding all storages by type '{}'", type);
    return storageRepository.findAllByType(type)
        .map(getStorageMapper::toDto)
        .switchIfEmpty(Mono.error(ExceptionSupplier.entityNotFound(Storage.class, type)));
  }

  @Transactional
  @Override
  public Flux<DeleteStorageDTO> deleteByIds(final Long[] ids) {
    log.info("Deleting storage(s) by id(s) '{}'", Arrays.toString(ids));
    return Flux.fromArray(ids)
        .flatMap(storageRepository::findById)
        .flatMap(this::deleteBucket)
        .flatMap(this::deleteStorage)
        .map(deleteStorageMapper::toDto);
  }

  private Mono<Storage> createBucket(Storage storage) {
    return cloudStorageRepository.createBucket(storage.getBucket()).thenReturn(storage);
  }

  private Mono<Storage> checkIfExists(Storage storage) {
    return cloudStorageRepository.sendHeadRequest(storage.getBucket()).thenReturn(storage);
  }

  private Mono<Storage> deleteBucket(Storage storage) {
    return cloudStorageRepository.deleteBucket(storage.getBucket()).thenReturn(storage);
  }

  private Mono<Storage> deleteStorage(Storage storage) {
    return storageRepository.delete(storage).thenReturn(storage);
  }
}
