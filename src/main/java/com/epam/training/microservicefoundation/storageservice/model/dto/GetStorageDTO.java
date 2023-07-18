package com.epam.training.microservicefoundation.storageservice.model.dto;

import com.epam.training.microservicefoundation.storageservice.model.entity.StorageType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetStorageDTO implements Serializable {
  private static final long serialVersionUID = 2023_07_15_16_56L;
  private long id;
  private String bucket;
  private String path;
  private StorageType type;
}
