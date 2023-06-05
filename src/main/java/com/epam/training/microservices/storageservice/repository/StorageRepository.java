package com.epam.training.microservices.storageservice.repository;

import com.epam.training.microservices.storageservice.model.Storage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends ReactiveCrudRepository<Storage, Long> {
}
