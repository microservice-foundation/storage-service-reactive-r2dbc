package com.epam.training.microservices.storageservice.api.handler;

import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.model.StorageRecord;
import com.epam.training.microservices.storageservice.model.StorageType;
import com.epam.training.microservices.storageservice.service.StorageService;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class StorageHandler {
  private static final Logger log = LoggerFactory.getLogger(StorageHandler.class);
  private final StorageService service;

  @Autowired
  public StorageHandler(StorageService service) {
    this.service = service;
  }

  public Mono<ServerResponse> save(ServerRequest request) {
    log.info("Incoming request: {}", request);
    return ServerResponse.created(URI.create(request.path()))
        .body(service.save(request.bodyToMono(StorageDTO.class)), StorageRecord.class);
  }

  public Mono<ServerResponse> deleteByIds(ServerRequest request) {
    log.info("Incoming request: {}", request);
    Flux<Long> idsFlux = request
        .queryParam("id")
        .map(string -> Flux.fromArray(string.split(",")).map(Long::parseLong))
        .orElse(Flux.empty());

    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.deleteByIds(idsFlux), StorageRecord.class);
  }

  public Mono<ServerResponse> getById(ServerRequest request) {
    log.info("Incoming request: {}", request);
    long id = Long.parseLong(request.pathVariable("id"));
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.findById(id), StorageDTO.class);
  }

  public Mono<ServerResponse> getAllByType(ServerRequest request) {
    log.info("Incoming request: {}", request);
    StorageType type = request.queryParam("type")
        .map(StorageType::valueOf)
        .orElse(StorageType.STAGING);

    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(service.findAllByType(type), StorageDTO.class);
  }
}
