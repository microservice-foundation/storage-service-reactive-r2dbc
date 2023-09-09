package com.epam.training.microservicefoundation.storageservice.web.validator;

public interface QueryParamValidator {
  boolean supports(Class<?> clazz);
  void validate(Object object, QueryParamValidationErrors errors);
}
