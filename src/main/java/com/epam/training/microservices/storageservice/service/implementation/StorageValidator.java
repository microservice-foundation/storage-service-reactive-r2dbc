package com.epam.training.microservices.storageservice.service.implementation;

import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.service.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StorageValidator implements Validator<StorageDTO> {
  private final Logger log = LoggerFactory.getLogger(StorageValidator.class);

  public boolean validate(StorageDTO storage) {
    log.info("Validating storage data: {}", storage);
    return StringUtils.hasText(storage.getBucket()) && StringUtils.hasText(storage.getPath());
  }
}
