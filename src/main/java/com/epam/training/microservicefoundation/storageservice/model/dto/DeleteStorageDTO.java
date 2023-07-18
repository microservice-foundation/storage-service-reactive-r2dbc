package com.epam.training.microservicefoundation.storageservice.model.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteStorageDTO implements Serializable {
  private long id;
}
