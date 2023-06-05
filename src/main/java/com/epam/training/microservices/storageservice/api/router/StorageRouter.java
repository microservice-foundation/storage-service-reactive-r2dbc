package com.epam.training.microservices.storageservice.api.router;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import com.epam.training.microservices.storageservice.api.handler.StorageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class StorageRouter {
  @Bean
  RouterFunction<ServerResponse> routes(StorageHandler handler) {
    return RouterFunctions.nest(RequestPredicates.path("/api/v1/storages"),
        RouterFunctions
            .route(GET("/{id}").and(accept(APPLICATION_JSON)), handler::getById)
            .andRoute(POST("").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), handler::save)
            .andRoute(DELETE("").and(RequestPredicates.queryParam("id", t -> true)).and(accept(APPLICATION_JSON)),
                handler::deleteByIds));
  }
}
