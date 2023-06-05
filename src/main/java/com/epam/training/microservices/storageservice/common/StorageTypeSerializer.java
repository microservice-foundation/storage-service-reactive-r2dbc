package com.epam.training.microservices.storageservice.common;

import com.epam.training.microservices.storageservice.model.StorageType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class StorageTypeSerializer extends JsonSerializer<StorageType> {
  @Override
  public void serialize(StorageType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value.name().toLowerCase());
  }
}
