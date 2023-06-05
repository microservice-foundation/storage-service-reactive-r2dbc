package com.epam.training.microservices.storageservice.model;

import java.io.Serializable;

public class StorageDTOTest implements Serializable {
  private final long id;
  private final String bucket;
  private final String path;
  private final String type;

  public StorageDTOTest(long id, String bucket, String path, String type) {
    this.id = id;
    this.bucket = bucket;
    this.path = path;
    this.type = type;
  }

  public long getId() {
    return id;
  }

  public String getBucket() {
    return bucket;
  }

  public String getPath() {
    return path;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "StorageDTOTest{" +
        "id=" + id +
        ", bucket='" + bucket + '\'' +
        ", path='" + path + '\'' +
        ", type=" + type +
        '}';
  }
}
