package com.epam.training.microservices.storageservice.common;

import com.epam.training.microservices.storageservice.model.StorageType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class StorageTypeDeserializer extends JsonDeserializer<StorageType> {
  @Override
  public StorageType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String value = p.getValueAsString();
    return StorageType.valueOf(value.toUpperCase());
  }
}
