package com.epam.training.microservicefoundation.storageservice.web.validator;

import com.epam.training.microservicefoundation.storageservice.domain.exception.ExceptionSupplier;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class RequestBodyValidator<T> {
  private static final Logger log = Loggers.getLogger(RequestBodyValidator.class);
  private final Validator validator;
  private final Class<T> validationClass;

  public RequestBodyValidator(Validator validator, Class<T> validationClass) {
    this.validator = validator;
    this.validationClass = validationClass;
  }

  public Mono<T> validateBody(Mono<T> monoBody) {
    log.info("Validating request body.");
    return monoBody.map(body -> {
      Errors errors = new BeanPropertyBindingResult(body, this.validationClass.getName());
      this.validator.validate(body, errors);

      if (errors == null || errors.getAllErrors().isEmpty()) {
        log.debug("Request body is validated successfully: {}", body);
        return body;
      } else {
        log.error("Validation failed: {}", errors.getAllErrors().toString());
        throw ExceptionSupplier.invalidRequest(errors).get();
      }
    });
  }
}
