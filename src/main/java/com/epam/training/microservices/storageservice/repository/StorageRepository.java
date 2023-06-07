package com.epam.training.microservices.storageservice.repository;

import com.epam.training.microservices.storageservice.model.Storage;
import com.epam.training.microservices.storageservice.model.StorageType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StorageRepository extends ReactiveCrudRepository<Storage, Long> {
  Flux<Storage> findAllByType(StorageType type);
}
