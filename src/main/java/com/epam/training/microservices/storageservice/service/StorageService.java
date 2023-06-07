package com.epam.training.microservices.storageservice.service;

import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.model.StorageRecord;
import com.epam.training.microservices.storageservice.model.StorageType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StorageService {
  Mono<StorageRecord> save(Mono<StorageDTO> storage);
  Mono<StorageDTO> findById(long id);
  Flux<StorageRecord> deleteByIds(Flux<Long> ids);
  Flux<StorageDTO> findAllByType(StorageType type);
}
