package com.epam.training.microservicefoundation.storageservice.common;

import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
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
