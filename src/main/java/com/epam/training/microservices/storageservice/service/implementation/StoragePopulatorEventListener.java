package com.epam.training.microservices.storageservice.service.implementation;


import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.service.StorageEventListener;
import com.epam.training.microservices.storageservice.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

@Component
public class StoragePopulatorEventListener implements StorageEventListener<Mono<StorageDTO>> {
  private static final Logger log = Loggers.getLogger(StoragePopulatorEventListener.class);
  private final StorageService service;

  @Autowired
  public StoragePopulatorEventListener(StorageService service) {
    this.service = service;
  }

  @Override
  public Mono<Void> eventListened(Mono<StorageDTO> storage) {
    log.info("Saving storages");
    return service.save(storage).then();
  }
}
