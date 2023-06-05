package com.epam.training.microservices.storageservice.service;

public interface Validator<T> {
  boolean validate(T input);
}
