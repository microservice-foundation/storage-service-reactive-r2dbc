package com.epam.training.microservicefoundation.storageservice.domain.entity;

import com.epam.training.microservicefoundation.storageservice.common.StorageTypeDeserializer;
import com.epam.training.microservicefoundation.storageservice.common.StorageTypeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = StorageTypeSerializer.class)
@JsonDeserialize(using = StorageTypeDeserializer.class)
public enum StorageType {
  PERMANENT,
  STAGING
}
