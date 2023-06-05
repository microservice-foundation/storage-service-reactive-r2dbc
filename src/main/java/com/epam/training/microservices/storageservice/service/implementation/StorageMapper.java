package com.epam.training.microservices.storageservice.service.implementation;

import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.service.Mapper;
import com.epam.training.microservices.storageservice.model.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageMapper implements Mapper<Storage, StorageDTO> {
  private final Logger log = LoggerFactory.getLogger(StorageMapper.class);
  @Override
  public StorageDTO mapToRecord(Storage storage) {
    log.info("Mapping from entity: {}", storage);
    if(storage == null) {
      return null;
    }
    return new StorageDTO.Builder()
        .id(storage.getId())
        .path(storage.getPath())
        .bucket(storage.getBucket())
        .type(storage.getType())
        .build();
  }

  @Override
  public Storage mapToEntity(StorageDTO storageDTO) {
    log.info("Mapping to record: {}", storageDTO);
    if(storageDTO == null) {
      return null;
    }
    return new Storage.Builder()
        .path(storageDTO.getPath())
        .bucket(storageDTO.getBucket())
        .type(storageDTO.getType())
        .build();
  }
}
