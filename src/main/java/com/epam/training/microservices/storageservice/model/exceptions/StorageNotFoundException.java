package com.epam.training.microservices.storageservice.model.exceptions;

public final class StorageNotFoundException extends RuntimeException {
  public StorageNotFoundException(String message) {
    super(message);
  }

  public StorageNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
