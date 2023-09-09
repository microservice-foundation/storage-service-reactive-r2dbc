package com.epam.training.microservicefoundation.storageservice.domain.exception;

import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.web.validator.QueryParamValidationErrors;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.SdkResponse;

public class ExceptionSupplier {
  private ExceptionSupplier() {}

  public static Supplier<EntityNotFoundException> entityNotFound(Class<?> entityClass, long id) {
    return () -> new EntityNotFoundException(String.format("%s with id=%d is not found", entityClass.getSimpleName(), id));
  }

  public static Supplier<EntityNotFoundException> entityNotFound(Class<?> entityClass, StorageType storageType) {
    return () -> new EntityNotFoundException(String.format("%s with type=%s is not found", entityClass.getSimpleName(), storageType));
  }

  public static Supplier<EntityExistsException> entityAlreadyExists(Class<?> entityClass, Throwable error) {
    return () -> new EntityExistsException(String.format("%s with these parameters already exists", entityClass.getSimpleName()), error);
  }

  public static Supplier<CloudStorageException> cloudStorageProcessFailed(SdkResponse response) {
    return () -> new CloudStorageException(response);
  }

  public static Supplier<ResponseStatusException> invalidRequest(Errors errors) {
    return () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getAllErrors().toString());
  }

  public static Supplier<ResponseStatusException> invalidRequest(QueryParamValidationErrors errors) {
    return () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getAllErrors().toString());
  }
}
