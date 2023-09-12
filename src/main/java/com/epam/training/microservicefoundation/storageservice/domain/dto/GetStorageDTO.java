package com.epam.training.microservicefoundation.storageservice.domain.dto;

import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetStorageDTO extends AuditableDTO implements Serializable {
  private static final long serialVersionUID = 2023_07_15_16_56L;
  private long id;
  private String bucket;
  private String path;
  private StorageType type;
}
