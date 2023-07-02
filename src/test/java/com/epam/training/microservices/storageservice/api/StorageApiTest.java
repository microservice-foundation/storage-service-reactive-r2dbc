package com.epam.training.microservices.storageservice.api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.epam.training.microservices.storageservice.configuration.WebFluxConfiguration;
import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.model.StorageRecord;
import com.epam.training.microservices.storageservice.model.StorageType;
import com.epam.training.microservices.storageservice.model.exceptions.CreateBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.DeleteBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.HeadBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.StorageNotFoundException;
import com.epam.training.microservices.storageservice.service.StorageService;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;

@WebFluxTest
@DirtiesContext
@ContextConfiguration(classes = {WebFluxConfiguration.class})
@TestPropertySource(locations = "classpath:application.properties")
class StorageApiTest {
  @MockBean
  private StorageService service;
  @Autowired
  private WebTestClient webClient;

  @Test
  void shouldSaveStorage() {
    StorageDTO storageDTO = new StorageDTO.Builder()
        .bucket("test-bucket")
        .path("/files")
        .type(StorageType.STAGING)
        .build();
    StorageRecord storageRecord = new StorageRecord(1L);
    when(service.save(any(Mono.class))).thenReturn(Mono.just(storageRecord));
    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(storageDTO), StorageDTO.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.id").isEqualTo(storageRecord.getId());
  }

  @Test
  void shouldReturn400WhenSaveInvalidStorage() {
    when(service.save(any(Mono.class))).thenThrow(new IllegalArgumentException("Saving invalid song record"));
    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(new StorageDTO.Builder().build()), StorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request")
        .jsonPath("$.debugMessage").isEqualTo("Saving invalid song record");
  }

  @Test
  void shouldReturn400WhenSaveStorageAndDataIntegrityException() {
    when(service.save(any(Mono.class))).thenThrow(new IllegalArgumentException("Saving a storage record with invalid parameters length or" +
        " duplicate value .."));
    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(new StorageDTO.Builder().build()), StorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request")
        .jsonPath("$.debugMessage").value(containsString("Saving a storage record with invalid parameters"));
  }

  @Test
  void shouldReturn500WhenSaveStorageAndCreateBucketFailedException() {
    SdkResponse sdkResponse =
        CreateBucketResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .statusText("Something bad happened during bucket creation").build()).build();

    when(service.save(any(Mono.class))).thenThrow(new CreateBucketFailedException(sdkResponse));
    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(new StorageDTO.Builder().build()), StorageDTO.class)
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody()
        .jsonPath("$.status").isEqualTo("INTERNAL_SERVER_ERROR")
        .jsonPath("$.message").isEqualTo("Something bad happened during bucket creation");
  }

  @Test
  void shouldGetBucketById() {
    StorageDTO storageDTO = new StorageDTO.Builder().id(123L).bucket("test-bucket").path("/files").type(StorageType.STAGING).build();
    when(service.findById(anyLong())).thenReturn(Mono.just(storageDTO));
    webClient.get()
        .uri("/api/v1/storages/{id}", storageDTO.getId())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(storageDTO.getId())
        .jsonPath("$.path").isEqualTo(storageDTO.getPath())
        .jsonPath("$.bucket").isEqualTo(storageDTO.getBucket())
        .jsonPath("$.type").isEqualTo("staging");
  }

  @Test
  void shouldReturn404WhenGetBucketByIdAndStorageNotFoundException() {
    long id = 123L;
    when(service.findById(anyLong())).thenThrow(new StorageNotFoundException(String.format("Storage is not found with id '%d'", id)));
    webClient.get()
        .uri("/api/v1/storages/{id}", id)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.status").isEqualTo("NOT_FOUND")
        .jsonPath("$.message").isEqualTo("Storage is not found")
        .jsonPath("$.debugMessage").isEqualTo("Storage is not found with id '123'");
  }

  @Test
  void shouldReturn404WhenGetBucketByIdAndHeadBucketFailedException() {
    long id = 123L;
    SdkResponse sdkResponse =
        CreateBucketResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(HttpStatus.NOT_FOUND.value())
            .statusText("Something bad happened during bucket head request").build()).build();

    when(service.findById(anyLong())).thenThrow(new HeadBucketFailedException(sdkResponse));
    webClient.get()
        .uri("/api/v1/storages/{id}", id)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.status").isEqualTo("NOT_FOUND")
        .jsonPath("$.message").isEqualTo("Something bad happened during bucket head request");
  }

  @Test
  void shouldDeleteStorageByIds() {
    long[] ids = {1L, 2L};
    when(service.deleteByIds(any(Flux.class))).thenReturn(Flux.just(new StorageRecord(ids[0]), new StorageRecord(ids[1])));
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", ids[0] + "," + ids[1])
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[*].id").value(containsInAnyOrder(is((int) ids[0]), is((int) ids[1])));
  }

  @Test
  void shouldReturn403WhenGetStorageByIdAndIllegalArgumentException() {
    when(service.deleteByIds(any(Flux.class))).thenThrow(new IllegalArgumentException("Id param is not validated, check your ids"));
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", "1,2")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request")
        .jsonPath("$.debugMessage").isEqualTo("Id param is not validated, check your ids");
  }

  @Test
  void shouldReturn500WhenGetStorageByIdAndDeleteBucketFailedException() {
    SdkResponse sdkResponse =
        DeleteBucketResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .statusText("Something bad happened during bucket deletion").build()).build();

    when(service.deleteByIds(any(Flux.class))).thenThrow(new DeleteBucketFailedException(sdkResponse));
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", "1,2")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody()
        .jsonPath("$.status").isEqualTo("INTERNAL_SERVER_ERROR")
        .jsonPath("$.message").isEqualTo("Something bad happened during bucket deletion");
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldGetAllStoragesByType(StorageType type) {
    StorageDTO storageDTO1 = storageDTO(type);
    StorageDTO storageDTO2 = storageDTO(type);
    when(service.findAllByType(type)).thenReturn(Flux.just(storageDTO1, storageDTO2));
    webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("type", type)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[*].id").value(containsInAnyOrder(
                (int) storageDTO1.getId(),
                (int) storageDTO2.getId()
            )
        )
        .jsonPath("$[*].type").value(containsInAnyOrder(
                equalToIgnoringCase(storageDTO1.getType().name()),
                equalToIgnoringCase(storageDTO2.getType().name())
            )
        );
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn404WhenGetAllStoragesByType(StorageType type) {
    when(service.findAllByType(type)).thenThrow(new StorageNotFoundException(String.format("Storage is not found by type '%s'", type)));
    webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("type", type)
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.status").isEqualTo("NOT_FOUND")
        .jsonPath("$.message").isEqualTo("Storage is not found")
        .jsonPath("$.debugMessage").isEqualTo(String.format("Storage is not found by type '%s'", type));
  }

  private static StorageDTO storageDTO(StorageType type) {
    Random random = new Random();
    long id = random.nextInt(1000);
    return new StorageDTO.Builder()
        .id(id)
        .type(type)
        .bucket("test-bucket-" + id)
        .path("/files")
        .build();
  }
}
