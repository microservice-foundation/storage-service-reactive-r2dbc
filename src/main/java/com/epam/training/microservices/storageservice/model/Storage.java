package com.epam.training.microservices.storageservice.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Table("STORAGES")
public final class Storage implements Serializable {
  public static final long serialVersionUID = 2023_05_29_11_16L;
  @Id
  private long id;
  private String bucket;
  private String path;
  private StorageType type;
  @CreatedDate
  private LocalDateTime createdDate;
  @LastModifiedDate
  private LocalDateTime lastModifiedDate;

  private Storage() {
  }
  private Storage(Builder builder) {
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

    public Storage build() {
      return new Storage(this);
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

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public LocalDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Storage storage = (Storage) o;
    return id == storage.id && bucket.equals(storage.bucket);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, bucket);
  }
}
