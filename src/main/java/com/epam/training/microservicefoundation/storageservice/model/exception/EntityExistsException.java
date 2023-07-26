package com.epam.training.microservicefoundation.storageservice.model.exception;

public class EntityExistsException extends RuntimeException {
  public EntityExistsException(String message, Throwable error) {
    super(message, error);
  }
}
