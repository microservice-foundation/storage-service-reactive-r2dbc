package com.epam.training.microservices.storageservice.service.implementation;

import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.model.StorageType;
import com.epam.training.microservices.storageservice.service.StorageEventListener;
import java.util.List;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StorageListener {
  private final StorageEventListener<Mono<StorageDTO>> storagePopulatorEventListener;

  public StorageListener(StorageEventListener<Mono<StorageDTO>> storagePopulatorEventListener) {
    this.storagePopulatorEventListener = storagePopulatorEventListener;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void listen() {
    storages().forEach(storagePopulatorEventListener::eventListened);
  }

  private List<Mono<StorageDTO>> storages() {
    return List.of(
        Mono.just(new StorageDTO.Builder()
            .type(StorageType.STAGING)
            .bucket("storage-staging")
            .path("files/")
            .build()),
        Mono.just(new StorageDTO.Builder()
            .type(StorageType.PERMANENT)
            .bucket("storage-permanent")
            .path("files/")
            .build()));
  }
}
