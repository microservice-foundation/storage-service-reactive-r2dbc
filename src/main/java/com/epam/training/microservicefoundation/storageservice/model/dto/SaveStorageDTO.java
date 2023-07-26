package com.epam.training.microservicefoundation.storageservice.model.dto;

import com.epam.training.microservicefoundation.storageservice.model.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.validator.ValidBucket;
import com.epam.training.microservicefoundation.storageservice.validator.ValidPath;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
