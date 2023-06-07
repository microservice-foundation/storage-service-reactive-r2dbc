package com.epam.training.microservices.storageservice.service.implementation;

import com.epam.training.microservices.storageservice.model.Storage;
import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.model.StorageRecord;
import com.epam.training.microservices.storageservice.model.StorageType;
import com.epam.training.microservices.storageservice.model.exceptions.StorageNotFoundException;
import com.epam.training.microservices.storageservice.repository.CloudStorageRepository;
import com.epam.training.microservices.storageservice.repository.StorageRepository;
import com.epam.training.microservices.storageservice.service.Mapper;
import com.epam.training.microservices.storageservice.service.StorageService;
import com.epam.training.microservices.storageservice.service.Validator;
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
  private final Validator<StorageDTO> validator;
  private final Mapper<Storage, StorageDTO> mapper;

  @Autowired
  public StorageServiceImpl(StorageRepository storageRepository, CloudStorageRepository cloudStorageRepository,
      Validator<StorageDTO> validator, Mapper<Storage, StorageDTO> mapper) {

    this.storageRepository = storageRepository;
    this.cloudStorageRepository = cloudStorageRepository;
    this.validator = validator;
    this.mapper = mapper;
  }

  @Transactional
  @Override
  public Mono<StorageRecord> save(Mono<StorageDTO> storage) {
    log.info("Saving a storage record '{}'", storage);
    return storage
        .filter(validator::validate)
        .map(mapper::mapToEntity)
        .flatMap(result -> cloudStorageRepository.createBucket(result.getBucket()).thenReturn(result))
        .flatMap(storageRepository::save)
        .map(result -> new StorageRecord(result.getId()))
        .onErrorMap(DataIntegrityViolationException.class, e -> new IllegalArgumentException(String.format(
        "Saving a storage record with invalid parameters length or duplicate value '%s'", e.getLocalizedMessage()), e))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Saving invalid song record")));
  }

  @Override
  public Mono<StorageDTO> findById(long id) {
    log.info("Finding a storage with id '{}'", id);
    return storageRepository.findById(id)
        .flatMap(result -> cloudStorageRepository.checkIfExists(result.getBucket()).thenReturn(result))
        .map(mapper::mapToRecord)
        .switchIfEmpty(Mono.error(new StorageNotFoundException(String.format("Storage is not found with id '%d'", id))));
  }

  @Override
  public Flux<StorageDTO> findAllByType(StorageType type) {
    log.info("Finding all storages by type '{}'", type);
    return storageRepository.findAllByType(type)
        .map(mapper::mapToRecord)
        .switchIfEmpty(Mono.error(new StorageNotFoundException(String.format("Storage is not found by type '%s'", type))));
  }

  @Transactional
  @Override
  public Flux<StorageRecord> deleteByIds(Flux<Long> ids) {
    log.info("Deleting storage(s) by id(s) '{}'", ids);
    return ids
        .flatMap(storageRepository::findById)
        .flatMap(result -> cloudStorageRepository.deleteBucket(result.getBucket()).thenReturn(result))
        .flatMap(storage -> storageRepository.delete(storage).thenReturn(new StorageRecord(storage.getId())))
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Id param is not validated, check your ids")));
  }
}
