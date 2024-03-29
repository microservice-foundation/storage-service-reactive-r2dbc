package com.epam.training.microservicefoundation.storageservice.domain.dto;

import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.web.validator.ValidBucket;
import com.epam.training.microservicefoundation.storageservice.web.validator.ValidPath;
import java.io.Serializable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveStorageDTO implements Serializable {
  private static final long serialVersionUID = 2023_07_15_16_56L;

  @NotEmpty
  @ValidBucket
  private String bucket;

  @NotEmpty
  @ValidPath
  private String path;

  @NotNull
  private StorageType type;
}
