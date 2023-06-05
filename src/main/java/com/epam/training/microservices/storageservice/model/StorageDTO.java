package com.epam.training.microservices.storageservice.model;

import java.io.Serializable;

public class StorageDTO implements Serializable {
  private static final long serialVersionUID = 2023_05_29_11_37L;
  private long id;
  private String bucket;
  private String path;
  private StorageType type;

  protected StorageDTO() {
  }

  private StorageDTO(Builder builder) {
    this.id = builder.id;
    this.bucket = builder.bucket;
    this.path = builder.path;
    this.type = builder.type;
  }

  public static class Builder {
    private long id;
    private String bucket;
    private String path;
    private StorageType type;

    public Builder() {
    }

    public Builder id(long id) {
      this.id = id;
      return this;
    }

    public Builder bucket(String bucket) {
      this.bucket = bucket;
      return this;
    }

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder type(StorageType type) {
      this.type = type;
      return this;
    }

    public StorageDTO build() {
      return new StorageDTO(this);
    }
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

  public StorageType getType() {
    return type;
  }

  @Override
  public String toString() {
    return
        "StorageDTO{" +
        "id=" + id +
        ", bucket='" + bucket + '\'' +
        ", path='" + path + '\'' +
        ", type=" + type +
        '}';
  }
}
