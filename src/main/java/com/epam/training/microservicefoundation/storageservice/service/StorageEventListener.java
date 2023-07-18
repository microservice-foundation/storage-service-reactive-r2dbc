package com.epam.training.microservicefoundation.storageservice.service;

import reactor.core.publisher.Mono;

public interface StorageEventListener<E> {
  Mono<Void> eventListened(E event);
}
