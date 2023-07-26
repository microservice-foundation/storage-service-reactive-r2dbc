package com.epam.training.microservicefoundation.storageservice.service;

public interface Mapper<Entity, Record> {
  Record mapToRecord(Entity entity);
  Entity mapToEntity(Record record);
}
