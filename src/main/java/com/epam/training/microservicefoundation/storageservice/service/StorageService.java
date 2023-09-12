package com.epam.training.microservicefoundation.storageservice.service;

import com.epam.training.microservicefoundation.storageservice.domain.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StorageService {
  Mono<GetStorageDTO> save(final Mono<SaveStorageDTO> saveStorageDTOMono);
  Mono<GetStorageDTO> getById(final long id);
  Flux<DeleteStorageDTO> deleteByIds(final Long[] ids);
  Flux<GetStorageDTO> findAllByType(final StorageType type);
}
