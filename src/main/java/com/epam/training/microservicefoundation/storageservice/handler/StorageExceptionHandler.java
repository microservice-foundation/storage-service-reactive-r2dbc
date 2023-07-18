package com.epam.training.microservicefoundation.storageservice.handler;


import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.epam.training.microservicefoundation.storageservice.model.APIError;
import com.epam.training.microservicefoundation.storageservice.model.exception.EntityExistsException;
import com.epam.training.microservicefoundation.storageservice.model.exception.EntityNotFoundException;
import com.epam.training.microservicefoundation.storageservice.model.exception.CloudStorageException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

public class StorageExceptionHandler extends AbstractErrorWebExceptionHandler {
  private final Map<Class<? extends Throwable>, Function<Throwable, Mono<ServerResponse>>> exceptionToHandlers;

  private static final HttpStatus DEFAULT_HTTP_STATUS = INTERNAL_SERVER_ERROR;
  private static final String DEFAULT_MESSAGE = "Hmm.. there is an unknown issue occurred";

  public StorageExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ApplicationContext applicationContext) {
    super(errorAttributes, resources, applicationContext);
    exceptionToHandlers = new HashMap<>();

    registerExceptionHandler(CloudStorageException.class);
    registerExceptionHandler(List.of(NumberFormatException.class, IllegalArgumentException.class, ResponseStatusException.class),
        BAD_REQUEST, "Invalid request");
    registerExceptionHandler(IllegalStateException.class, INTERNAL_SERVER_ERROR, "Internal error happened");
    registerExceptionHandler(NoSuchBucketException.class, NOT_FOUND, "Cloud bucket is not found");
    registerExceptionHandler(EntityExistsException.class, BAD_REQUEST, "Storage is already existed");
    registerExceptionHandler(EntityNotFoundException.class, NOT_FOUND, "Storage is not found");
  }

  private void registerExceptionHandler(Class<? extends Throwable> exceptionClass) {
    exceptionToHandlers.put(exceptionClass, this::handleBucketException);
  }

  private void registerExceptionHandler(List<Class<? extends Throwable>> exceptionClasses, HttpStatus status, String message) {
    exceptionClasses.forEach(exceptionClass -> this.registerExceptionHandler(exceptionClass, status, message));
  }

  private void registerExceptionHandler(Class<? extends Throwable> exceptionClass, HttpStatus status, String message) {
    exceptionToHandlers.put(exceptionClass, exception -> handleException(exception, status, message));
  }

  private Mono<ServerResponse> handleBucketException(Throwable exception) {
    CloudStorageException cloudStorageException = (CloudStorageException) exception;
    return ServerResponse
        .status(HttpStatus.valueOf(cloudStorageException.getStatusCode()))
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new APIError(HttpStatus.valueOf(cloudStorageException.getStatusCode()),
            cloudStorageException.getStatusText(), exception)));
  }

  private Mono<ServerResponse> handleException(Throwable exception, HttpStatus status, String message) {
    return ServerResponse
        .status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(new APIError(status, message, exception)));
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), request -> {
      Throwable error = getError(request);
      Function<Throwable, Mono<ServerResponse>> exceptionHandler = exceptionToHandlers.getOrDefault(error.getClass(),
          throwable -> handleException(throwable, DEFAULT_HTTP_STATUS, DEFAULT_MESSAGE));

      return exceptionHandler.apply(error);
    });
  }
}
