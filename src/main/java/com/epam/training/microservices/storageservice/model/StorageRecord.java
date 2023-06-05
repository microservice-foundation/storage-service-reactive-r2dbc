package com.epam.training.microservices.storageservice.model;

import java.io.Serializable;

public class StorageRecord implements Serializable {
  private static final long serialVersionUID = 2023_05_29_11_40L;
  private final long id;

  public StorageRecord(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }
}
