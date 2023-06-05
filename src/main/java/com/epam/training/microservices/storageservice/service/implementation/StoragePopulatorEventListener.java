package com.epam.training.microservices.storageservice.service.implementation;


import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.service.StorageEventListener;
import com.epam.training.microservices.storageservice.service.StorageService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StoragePopulatorEventListener implements StorageEventListener<Mono<StorageDTO>> {
  private final StorageService service;

  public StoragePopulatorEventListener(StorageService service) {
    this.service = service;
  }

  @Override
  public Mono<Void> eventListened(Mono<StorageDTO> storage) {
    return service.save(storage).then();
  }
}
