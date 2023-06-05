package com.epam.training.microservices.storageservice.model;

import com.epam.training.microservices.storageservice.common.StorageTypeDeserializer;
import com.epam.training.microservices.storageservice.common.StorageTypeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = StorageTypeSerializer.class)
@JsonDeserialize(using = StorageTypeDeserializer.class)
public enum StorageType {
  PERMANENT,
  STAGING;
}
