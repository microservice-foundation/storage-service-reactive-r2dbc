package com.epam.training.microservicefoundation.storageservice.repository;

import com.epam.training.microservicefoundation.storageservice.model.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.model.entity.Storage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StorageRepository extends ReactiveCrudRepository<Storage, Long> {
  Flux<Storage> findAllByType(StorageType type);
}
