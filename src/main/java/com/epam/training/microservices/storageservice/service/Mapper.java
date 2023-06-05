package com.epam.training.microservices.storageservice.service;

public interface Mapper<Entity, Record> {
  Record mapToRecord(Entity entity);
  Entity mapToEntity(Record record);
}
