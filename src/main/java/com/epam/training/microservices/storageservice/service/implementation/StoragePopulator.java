package com.epam.training.microservices.storageservice.service.implementation;

import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.model.StorageType;
import com.epam.training.microservices.storageservice.service.StorageEventListener;
import java.util.List;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@Component
public class StoragePopulator {
  private static final Logger log = Loggers.getLogger(StoragePopulator.class);
  private final StorageEventListener<Mono<StorageDTO>> storagePopulatorEventListener;

  public StoragePopulator(StorageEventListener<Mono<StorageDTO>> storagePopulatorEventListener) {
    this.storagePopulatorEventListener = storagePopulatorEventListener;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void listen() {
    log.info("Populate storages at application started event");
    storages().flatMap(storageDTO -> storagePopulatorEventListener.eventListened(Mono.just(storageDTO))).subscribe();
  }

  private Flux<StorageDTO> storages() {
    return Flux.just(new StorageDTO.Builder()
            .type(StorageType.STAGING)
            .bucket("epam-training-microservice-storage-staging")
            .path("files/")
            .build(),
        new StorageDTO.Builder()
            .type(StorageType.PERMANENT)
            .bucket("epam-training-microservice-storage-permanent")
            .path("files/")
            .build());
  }
}
