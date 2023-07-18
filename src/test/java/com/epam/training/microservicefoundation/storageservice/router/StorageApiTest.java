package com.epam.training.microservicefoundation.storageservice.router;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.epam.training.microservicefoundation.storageservice.configuration.WebFluxConfiguration;
import com.epam.training.microservicefoundation.storageservice.mapper.DeleteStorageMapper;
import com.epam.training.microservicefoundation.storageservice.mapper.GetStorageMapper;
import com.epam.training.microservicefoundation.storageservice.mapper.SaveStorageMapper;
import com.epam.training.microservicefoundation.storageservice.model.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.entity.Storage;
import com.epam.training.microservicefoundation.storageservice.model.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.model.exception.CloudStorageException;
import com.epam.training.microservicefoundation.storageservice.repository.CloudStorageRepository;
import com.epam.training.microservicefoundation.storageservice.repository.StorageRepository;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
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
  private StorageRepository storageRepository;
  @MockBean
  private CloudStorageRepository cloudStorageRepository;
  @MockBean
  private SaveStorageMapper saveStorageMapper;
  @MockBean
  private GetStorageMapper getStorageMapper;
  @MockBean
  private DeleteStorageMapper deleteStorageMapper;
  @Autowired
  private WebTestClient webClient;

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldSaveStorage(StorageType type) {
    final SaveStorageDTO saveStorageDTO = new SaveStorageDTO("test-bucket", "files/", type);
    final Storage storageEntity = getStorageEntityBySaveStorageDTO(saveStorageDTO);
    when(saveStorageMapper.toEntity(any())).thenReturn(storageEntity);
    when(cloudStorageRepository.createBucket(any())).thenReturn(Mono.empty());

    final Storage savedStorageEntity = storageEntity.toBuilder().id(1L).build();
    when(storageRepository.save(any())).thenReturn(Mono.just(savedStorageEntity));
    when(getStorageMapper.toDto(any())).thenReturn(getStorageDTOBySavedStorageEntity(savedStorageEntity));

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.id").isEqualTo(savedStorageEntity.getId())
        .jsonPath("$.bucket").isEqualTo(savedStorageEntity.getBucket())
        .jsonPath("$.path").isEqualTo(savedStorageEntity.getPath())
        .jsonPath("$.type").value(equalToIgnoringCase(savedStorageEntity.getType().name()));
  }


  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn400WhenSaveStorageWithInvalidName(StorageType type) {
    final SaveStorageDTO saveStorageDTO1 = new SaveStorageDTO("", "files/", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO1), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    final SaveStorageDTO saveStorageDTO2 = new SaveStorageDTO("ab", "files/", type);
    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO2), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    final SaveStorageDTO saveStorageDTO3 = new SaveStorageDTO("WhenshesawWalmarthadrotisseriechickensshethoughtthisisamiracle",
        "files/", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO3), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    final SaveStorageDTO saveStorageDTO4 = new SaveStorageDTO("When she saw Walmart had", "files/", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO4), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    final SaveStorageDTO saveStorageDTO5 = new SaveStorageDTO("127.0.0.1", "files/", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO5), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn400WhenSaveStorageWithInvalidPath(StorageType type) {
    final SaveStorageDTO saveStorageDTO1 = new SaveStorageDTO("test-bucket", "files", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO1), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    final SaveStorageDTO saveStorageDTO2 = new SaveStorageDTO("test-bucket", "", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO2), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    final SaveStorageDTO saveStorageDTO3 = new SaveStorageDTO("test-bucket", "file&path/", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO3), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    final SaveStorageDTO saveStorageDTO4 = new SaveStorageDTO("test-bucket",
        "IfyourevisitingthispageyourelikelyherebecauseyouresearchingforarandomsentenceSometimesarandomwordjust/", type);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO4), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");
  }
  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn400WhenSaveStorageAndDataIntegrityException(StorageType type) {
    final SaveStorageDTO saveStorageDTO = new SaveStorageDTO("test-bucket", "files/", type);
    final Storage storageEntity = getStorageEntityBySaveStorageDTO(saveStorageDTO);
    when(saveStorageMapper.toEntity(any())).thenReturn(storageEntity);
    when(cloudStorageRepository.createBucket(any())).thenReturn(Mono.empty());
    when(storageRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO), SaveStorageDTO.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Storage is already existed");
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn500WhenSaveStorageAndCloudBucketException(StorageType type) {
    final SaveStorageDTO saveStorageDTO = new SaveStorageDTO("test-bucket", "files/", type);
    final Storage storageEntity = getStorageEntityBySaveStorageDTO(saveStorageDTO);
    when(saveStorageMapper.toEntity(any())).thenReturn(storageEntity);

    SdkResponse sdkResponse = CreateBucketResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(
        HttpStatus.INTERNAL_SERVER_ERROR.value()).statusText("Something bad has happened during bucket creation").build()).build();
    when(cloudStorageRepository.createBucket(any())).thenThrow(new CloudStorageException(sdkResponse));

    webClient.post()
        .uri("/api/v1/storages")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(saveStorageDTO), SaveStorageDTO.class)
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody()
        .jsonPath("$.status").isEqualTo("INTERNAL_SERVER_ERROR")
        .jsonPath("$.message").isEqualTo("Something bad has happened during bucket creation");
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldGetBucketById(StorageType type) {
    final Storage savedStorageEntity = getSavedStorageByType(type);
    when(storageRepository.findById(anyLong())).thenReturn(Mono.just(savedStorageEntity));
    when(cloudStorageRepository.sendHeadRequest(savedStorageEntity.getBucket())).thenReturn(Mono.empty());
    when(getStorageMapper.toDto(any())).thenReturn(getStorageDTOBySavedStorageEntity(savedStorageEntity));

    webClient.get()
        .uri("/api/v1/storages/{id}", savedStorageEntity.getId())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(savedStorageEntity.getId())
        .jsonPath("$.path").isEqualTo(savedStorageEntity.getPath())
        .jsonPath("$.bucket").isEqualTo(savedStorageEntity.getBucket())
        .jsonPath("$.type").value(equalToIgnoringCase(savedStorageEntity.getType().name()));
  }

  @Test
  void shouldReturn404WhenGetBucketByIdAndEnityNotFoundException() {
    long id = 123L;
    when(storageRepository.findById(anyLong())).thenReturn(Mono.empty());
    webClient.get()
        .uri("/api/v1/storages/{id}", id)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.status").isEqualTo("NOT_FOUND")
        .jsonPath("$.message").isEqualTo("Storage is not found")
        .jsonPath("$.debugMessage").isEqualTo("Storage with id=123 is not found");
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn404WhenGetBucketByIdAndHeadBucketFailedException(StorageType type) {
    SdkResponse sdkResponse =
        CreateBucketResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(HttpStatus.NOT_FOUND.value())
            .statusText("Something bad happened during bucket head request").build()).build();

    final Storage savedStorageEntity = getSavedStorageByType(type);
    when(storageRepository.findById(anyLong())).thenReturn(Mono.just(savedStorageEntity));
    when(cloudStorageRepository.sendHeadRequest(savedStorageEntity.getBucket())).thenThrow(new CloudStorageException(sdkResponse));
    
    webClient.get()
        .uri("/api/v1/storages/{id}", savedStorageEntity.getId())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.status").isEqualTo("NOT_FOUND")
        .jsonPath("$.message").isEqualTo("Something bad happened during bucket head request");
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldDeleteStorageByIds(StorageType type) {
    final Storage storage1 = getSavedStorageByType(type);
    final Storage storage2 = getSavedStorageByType(type);
    long[] ids = {storage1.getId(), storage2.getId()};
    when(storageRepository.findById(ids[0])).thenReturn(Mono.just(storage1));
    when(storageRepository.findById(ids[1])).thenReturn(Mono.just(storage2));
    when(cloudStorageRepository.deleteBucket(storage1.getBucket())).thenReturn(Mono.empty());
    when(cloudStorageRepository.deleteBucket(storage2.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(storage1)).thenReturn(Mono.empty());
    when(storageRepository.delete(storage2)).thenReturn(Mono.empty());
    when(deleteStorageMapper.toDto(storage1)).thenReturn(getDeleteStorageDTO(storage1));
    when(deleteStorageMapper.toDto(storage2)).thenReturn(getDeleteStorageDTO(storage2));
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

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldDeleteStorageWhenFailDeletingPartially(StorageType type) {
    final Storage storage1 = getSavedStorageByType(type);
    final Storage storage2 = getSavedStorageByType(type);
    long[] ids = {storage1.getId(), storage2.getId()};
    when(storageRepository.findById(ids[0])).thenReturn(Mono.just(storage1));
    when(storageRepository.findById(ids[1])).thenReturn(Mono.empty());
    when(cloudStorageRepository.deleteBucket(storage1.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(storage1)).thenReturn(Mono.empty());
    when(deleteStorageMapper.toDto(storage1)).thenReturn(getDeleteStorageDTO(storage1));
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", ids[0] + "," + ids[1])
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[*].id").isEqualTo((int)ids[0]);
  }

  @Test
  void shouldReturn200WhenFailDeleting() {
    long[] ids = {123L, 455L};
    when(storageRepository.findById(ids[0])).thenReturn(Mono.empty());
    when(storageRepository.findById(ids[1])).thenReturn(Mono.empty());
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", ids[0] + "," + ids[1])
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void shouldReturn400WhenInvalidIdQueryParam() {
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", "1,3;")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", "a,b")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");

    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", "1,")
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.status").isEqualTo("BAD_REQUEST")
        .jsonPath("$.message").isEqualTo("Invalid request");
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn500WhenGetStorageByIdAndDeleteBucketFailedException(StorageType type) {
    final SdkResponse sdkResponse1 =
        DeleteBucketResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .statusText("Something bad happened during bucket deletion").build()).build();

    final Storage storage1 = getSavedStorageByType(type);
    final Storage storage2 = getSavedStorageByType(type);
    final long[] ids1 = {storage1.getId(), storage2.getId()};
    when(storageRepository.findById(ids1[0])).thenReturn(Mono.just(storage1));
    when(storageRepository.findById(ids1[1])).thenReturn(Mono.just(storage2));
    when(cloudStorageRepository.deleteBucket(storage1.getBucket())).thenThrow(new CloudStorageException(sdkResponse1));
    when(cloudStorageRepository.deleteBucket(storage2.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(storage2)).thenReturn(Mono.empty());
    when(deleteStorageMapper.toDto(storage2)).thenReturn(getDeleteStorageDTO(storage2));
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", ids1[0] + "," + ids1[1])
            .build())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody()
        .jsonPath("$.status").isEqualTo("INTERNAL_SERVER_ERROR")
        .jsonPath("$.message").isEqualTo("Something bad happened during bucket deletion");

    final SdkResponse sdkResponse2 =
        DeleteBucketResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .statusText("Something bad happened during bucket deletion").build()).build();

    final Storage storage3 = getSavedStorageByType(type);
    final Storage storage4 = getSavedStorageByType(type);
    final long[] ids2 = {storage3.getId(), storage4.getId()};
    when(storageRepository.findById(ids2[0])).thenReturn(Mono.just(storage3));
    when(storageRepository.findById(ids2[1])).thenReturn(Mono.just(storage4));
    when(cloudStorageRepository.deleteBucket(storage3.getBucket())).thenThrow(new CloudStorageException(sdkResponse2));
    when(cloudStorageRepository.deleteBucket(storage4.getBucket())).thenThrow(new CloudStorageException(sdkResponse2));
    webClient.delete()
        .uri(uriBuilder -> uriBuilder
            .path("/api/v1/storages")
            .queryParam("id", ids2[0] + "," + ids2[1])
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
    Storage savedStorage1 = getSavedStorageByType(type);
    Storage savedStorage2 = getSavedStorageByType(type);
    when(storageRepository.findAllByType(type)).thenReturn(Flux.just(savedStorage1, savedStorage2));
    when(getStorageMapper.toDto(savedStorage1)).thenReturn(getStorageDTOBySavedStorageEntity(savedStorage1));
    when(getStorageMapper.toDto(savedStorage2)).thenReturn(getStorageDTOBySavedStorageEntity(savedStorage2));
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
                (int) savedStorage1.getId(),
                (int) savedStorage2.getId()
            )
        )
        .jsonPath("$[*].type").value(containsInAnyOrder(
                equalToIgnoringCase(savedStorage1.getType().name()),
                equalToIgnoringCase(savedStorage2.getType().name())
            )
        );
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturn404WhenGetAllStoragesByType(StorageType type) {
    when(storageRepository.findAllByType(type)).thenReturn(Flux.empty());
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
        .jsonPath("$.message").isEqualTo("Storage is not found");
  }

  private Storage getStorageEntityBySaveStorageDTO(SaveStorageDTO dto) {
    return Storage.builder().bucket(dto.getBucket()).path(dto.getPath()).type(dto.getType()).build();
  }

  private GetStorageDTO getStorageDTOBySavedStorageEntity(Storage storage) {
    return new GetStorageDTO(storage.getId(), storage.getBucket(), storage.getPath(), storage.getType());
  }

  private final static Random RANDOM = new Random();
  private Storage getSavedStorageByType(StorageType type) {
    final int id = RANDOM.nextInt(1000);
    return Storage.builder()
        .id(id)
        .bucket("test-bucket-" + id)
        .path("files/")
        .type(type)
        .build();
  }

  private DeleteStorageDTO getDeleteStorageDTO(Storage storage) {
    return new DeleteStorageDTO(storage.getId());
  }
}
