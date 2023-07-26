package com.epam.training.microservicefoundation.storageservice.service.implementation;

import com.epam.training.microservicefoundation.storageservice.model.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.service.StorageEventListener;
import com.epam.training.microservicefoundation.storageservice.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StoragePopulatorEventListener implements StorageEventListener<SaveStorageDTO> {
  private final StorageService service;

  @Autowired
  public StoragePopulatorEventListener(StorageService service) {
    this.service = service;
  }

  @Override
  public Mono<Void> eventListened(SaveStorageDTO saveStorageDTO) {
    return service.save(Mono.just(saveStorageDTO)).then();
  }
}
