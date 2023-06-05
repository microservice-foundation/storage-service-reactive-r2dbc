package com.epam.training.microservices.storageservice.service;

import reactor.core.publisher.Mono;

public interface StorageEventListener<E> {
  Mono<Void> eventListened(E event);
}
