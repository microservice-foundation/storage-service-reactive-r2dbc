package com.epam.training.microservicefoundation.storageservice.service;

import com.epam.training.microservicefoundation.storageservice.model.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.entity.StorageType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StorageService {
  Mono<GetStorageDTO> save(final Mono<SaveStorageDTO> saveStorageDTOMono);
  Mono<GetStorageDTO> getById(final long id);
  Flux<DeleteStorageDTO> deleteByIds(final Long[] ids);
  Flux<GetStorageDTO> findAllByType(final StorageType type);
}
