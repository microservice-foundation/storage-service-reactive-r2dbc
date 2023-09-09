package com.epam.training.microservicefoundation.storageservice.web.handler;

import com.epam.training.microservicefoundation.storageservice.domain.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.service.StorageService;
import com.epam.training.microservicefoundation.storageservice.web.validator.RequestBodyValidator;
import com.epam.training.microservicefoundation.storageservice.web.validator.RequestQueryParamValidator;
import java.net.URI;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class StorageHandler {
  private static final Logger log = LoggerFactory.getLogger(StorageHandler.class);
  private final StorageService service;
  private final RequestBodyValidator<SaveStorageDTO> saveStorageDTOValidator;
  private final RequestQueryParamValidator idQueryParamValidator;

  @Autowired
  public StorageHandler(StorageService service, RequestBodyValidator<SaveStorageDTO> saveStorageDTOValidator,
      RequestQueryParamValidator idQueryParamValidator) {
    this.service = service;
    this.saveStorageDTOValidator = saveStorageDTOValidator;
    this.idQueryParamValidator = idQueryParamValidator;
  }

  public Mono<ServerResponse> save(ServerRequest request) {
    log.info("Incoming request: {}", request);
    final Mono<SaveStorageDTO> saveStorageDTOMono =
        saveStorageDTOValidator.validateBody(request.bodyToMono(SaveStorageDTO.class));
    return ServerResponse.created(URI.create(request.path()))
        .body(service.save(saveStorageDTOMono), GetStorageDTO.class);
  }

  public Mono<ServerResponse> deleteByIds(ServerRequest request, final String queryParam) {
    log.info("Incoming request: {}", request);
    final String validQueryParamValue = idQueryParamValidator.validateQueryParam(request.queryParam(queryParam), queryParam);
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.deleteByIds(getIds(validQueryParamValue)), DeleteStorageDTO.class);
  }

  public Mono<ServerResponse> getById(ServerRequest request) {
    log.info("Incoming request: {}", request);
    final long id = Long.parseLong(request.pathVariable("id"));
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.getById(id), GetStorageDTO.class);
  }

  public Mono<ServerResponse> getAllByType(ServerRequest request, final String queryParam) {
    log.info("Incoming request: {}", request);
    final StorageType type = request.queryParam(queryParam)
        .map(StorageType::valueOf)
        .orElse(null);

    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.findAllByType(type), GetStorageDTO.class);
  }

  private Long[] getIds(final String paramValue) {
    return Arrays.stream(paramValue.split(","))
        .map(String::trim)
        .map(Long::valueOf)
        .toArray(Long[]::new);
  }
}
