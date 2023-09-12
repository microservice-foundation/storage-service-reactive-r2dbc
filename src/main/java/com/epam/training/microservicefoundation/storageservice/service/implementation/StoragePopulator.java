package com.epam.training.microservicefoundation.storageservice.service.implementation;

import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.domain.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.service.StorageEventListener;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.Logger;
import reactor.util.Loggers;

@Component
public class StoragePopulator {
  private static final Logger log = Loggers.getLogger(StoragePopulator.class);
  private final StorageEventListener<SaveStorageDTO> storagePopulatorEventListener;

  public StoragePopulator(StorageEventListener<SaveStorageDTO> storagePopulatorEventListener) {
    this.storagePopulatorEventListener = storagePopulatorEventListener;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void listen() {
    log.info("Populate storages at application started event");
    storages().flatMap(storagePopulatorEventListener::eventListened).subscribe();
  }

  private Flux<SaveStorageDTO> storages() {
    return Flux.just(new SaveStorageDTO("epam-training-microservice-storage-staging", "files/", StorageType.STAGING),
        new SaveStorageDTO("epam-training-microservice-storage-permanent", "files/", StorageType.PERMANENT));
  }
}
