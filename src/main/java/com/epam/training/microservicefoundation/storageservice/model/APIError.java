package com.epam.training.microservicefoundation.storageservice.model;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Getter
@NoArgsConstructor
public class APIError implements Serializable {
  private static final long serialVersionUID = 2023_07_15_16_27L;
  private HttpStatus status;
  private final long timestamp = System.currentTimeMillis();
  private String message;
  private String debugMessage;

  public APIError(HttpStatus status, Throwable ex) {
    this(status, null, ex);
  }

  public APIError(HttpStatus status, String message, Throwable ex) {
    this.status = status;
    this.message = message == null ? "Unexpected error" : message;
    this.debugMessage = ex.getLocalizedMessage();
  }
}