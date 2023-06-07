package com.epam.training.microservices.storageservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.epam.training.microservices.storageservice.model.Storage;
import com.epam.training.microservices.storageservice.model.StorageDTO;
import com.epam.training.microservices.storageservice.model.StorageType;
import com.epam.training.microservices.storageservice.model.exceptions.CreateBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.DeleteBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.HeadBucketFailedException;
import com.epam.training.microservices.storageservice.model.exceptions.StorageNotFoundException;
import com.epam.training.microservices.storageservice.repository.CloudStorageRepository;
import com.epam.training.microservices.storageservice.repository.StorageRepository;
import com.epam.training.microservices.storageservice.service.implementation.StorageServiceImpl;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

  @Mock
  private StorageRepository storageRepository;
  @Mock
  private CloudStorageRepository cloudStorageRepository;
  @Mock
  private Validator<StorageDTO> validator;
  @Mock
  private Mapper<Storage, StorageDTO> mapper;
  @InjectMocks
  private StorageServiceImpl storageService;

  @Test
  void shouldSaveStorage() {
    StorageDTO storageDTO = new StorageDTO.Builder().bucket("test-bucket").path("/files").type(StorageType.STAGING).build();
    when(validator.validate(storageDTO)).thenReturn(true);
    Storage storage =
        new Storage.Builder().bucket(storageDTO.getBucket()).path(storageDTO.getPath()).type(storageDTO.getType()).id(123L).build();
    when(mapper.mapToEntity(storageDTO)).thenReturn(storage);
    when(cloudStorageRepository.createBucket(storage.getBucket())).thenReturn(Mono.just(CreateBucketResponse.builder().build()));
    when(storageRepository.save(storage)).thenReturn(Mono.just(storage));

    StepVerifier.create(storageService.save(Mono.just(storageDTO)))
        .assertNext(result -> {
          assertEquals(storage.getId(), result.getId());
        })
        .verifyComplete();
  }

  @Test
  void shouldThrowExceptionWhenSaveInvalidStorage() {
    StorageDTO storageDTO = new StorageDTO.Builder().bucket(null).path("/files").type(StorageType.STAGING).build();
    when(validator.validate(storageDTO)).thenReturn(false);
    StepVerifier.create(storageService.save(Mono.just(storageDTO)))
        .expectError(IllegalArgumentException.class)
        .verify();
  }

  @Test
  void shouldThrowCreateBucketFailedExceptionWhenSaveStorage() {
    StorageDTO storageDTO = new StorageDTO.Builder().bucket(null).path("/files").type(StorageType.STAGING).build();
    when(validator.validate(storageDTO)).thenReturn(true);
    Storage storage =
        new Storage.Builder().bucket(storageDTO.getBucket()).path(storageDTO.getPath()).type(storageDTO.getType()).id(123L).build();
    when(mapper.mapToEntity(storageDTO)).thenReturn(storage);
    when(cloudStorageRepository.createBucket(storage.getBucket())).thenThrow(CreateBucketFailedException.class);

    StepVerifier.create(storageService.save(Mono.just(storageDTO)))
        .expectError(CreateBucketFailedException.class)
        .verify();
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenSaveStorage() {
    StorageDTO storageDTO = new StorageDTO.Builder().bucket("test-bucket").path("/files").type(StorageType.STAGING).build();
    when(validator.validate(storageDTO)).thenReturn(true);
    Storage storage =
        new Storage.Builder().bucket(storageDTO.getBucket()).path(storageDTO.getPath()).type(storageDTO.getType()).id(123L).build();
    when(mapper.mapToEntity(storageDTO)).thenReturn(storage);
    when(cloudStorageRepository.createBucket(storage.getBucket())).thenReturn(Mono.just(CreateBucketResponse.builder().build()));
    when(storageRepository.save(storage)).thenThrow(DataIntegrityViolationException.class);

    StepVerifier.create(storageService.save(Mono.just(storageDTO)))
        .expectError(IllegalArgumentException.class)
        .verify();
  }

  @Test
  void shouldFindStorageById() {
    Storage storage = storage();
    when(storageRepository.findById(storage.getId())).thenReturn(Mono.just(storage));
    when(cloudStorageRepository.checkIfExists(storage.getBucket())).thenReturn(Mono.empty());
    StorageDTO storageDTO = new StorageDTO.Builder()
        .id(storage.getId())
        .bucket(storage.getBucket())
        .type(storage.getType())
        .path(storage.getPath())
        .build();

    when(mapper.mapToRecord(storage)).thenReturn(storageDTO);
    StepVerifier.create(storageService.findById(storage.getId()))
        .assertNext(result -> {
          assertEquals(storage.getId(), result.getId());
          assertEquals(storage.getBucket(), result.getBucket());
          assertEquals(storage.getType(), result.getType());
          assertEquals(storage.getPath(), result.getPath());
        })
        .verifyComplete();
  }

  @Test
  void shouldThrowStorageNotFoundExceptionWhenFindStorageById() {
    when(storageRepository.findById(anyLong())).thenReturn(Mono.empty());
    StepVerifier.create(storageService.findById(144L))
        .expectSubscription()
        .expectError(StorageNotFoundException.class)
        .verify();
  }

  @Test
  void shouldThrowHeadBucketFailedExceptionWhenFindStorageById() {
    Storage storage = storage();
    when(storageRepository.findById(storage.getId())).thenReturn(Mono.just(storage));
    when(cloudStorageRepository.checkIfExists(storage.getBucket())).thenThrow(HeadBucketFailedException.class);

    StepVerifier.create(storageService.findById(storage.getId()))
        .expectSubscription()
        .expectError(HeadBucketFailedException.class)
        .verify();
  }

  @Test
  void shouldDeleteStorage() {
    Storage storage1 = storage();
    when(storageRepository.findById(storage1.getId())).thenReturn(Mono.just(storage1));
    when(cloudStorageRepository.deleteBucket(storage1.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(storage1)).thenReturn(Mono.empty());

    Storage storage2 = storage();
    when(storageRepository.findById(storage2.getId())).thenReturn(Mono.just(storage2));
    when(cloudStorageRepository.deleteBucket(storage2.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(storage2)).thenReturn(Mono.empty());

    StepVerifier.create(storageService.deleteByIds(Flux.fromIterable(List.of(storage1.getId(), storage2.getId()))))
        .assertNext(result -> {
          assertEquals(storage1.getId(), result.getId());
        })
        .assertNext(result -> {
          assertEquals(storage2.getId(), result.getId());
        })
        .expectComplete()
        .verify();
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenDeleteStoragesByIdWithEmptyFlux() {
    StepVerifier.create(storageService.deleteByIds(Flux.empty()))
        .expectError(IllegalArgumentException.class)
        .verify();
  }

  @Test
  void shouldDeleteStoragesByIdAndOneOfThemNotFound() {
    Storage storage1 = storage();
    when(storageRepository.findById(storage1.getId())).thenReturn(Mono.empty());

    Storage storage2 = storage();
    when(storageRepository.findById(storage2.getId())).thenReturn(Mono.just(storage2));
    when(cloudStorageRepository.deleteBucket(storage2.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(storage2)).thenReturn(Mono.empty());

    StepVerifier.create(storageService.deleteByIds(Flux.fromIterable(List.of(storage1.getId(), storage2.getId()))))
        .assertNext(result -> {
          assertEquals(storage2.getId(), result.getId());
        })
        .expectComplete()
        .verify();
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenDeleteStoragesByIdAndAllItemsNotFound() {
    Storage storage1 = storage();
    when(storageRepository.findById(storage1.getId())).thenReturn(Mono.empty());

    Storage storage2 = storage();
    when(storageRepository.findById(storage2.getId())).thenReturn(Mono.empty());

    StepVerifier.create(storageService.deleteByIds(Flux.fromIterable(List.of(storage1.getId(), storage2.getId()))))
        .expectError(IllegalArgumentException.class)
        .verify();
  }

  @Test
  void shouldThrowDeleteBucketFailedExceptionWhenDeleteStoragesById() {
    Storage storage1 = storage();
    when(storageRepository.findById(storage1.getId())).thenReturn(Mono.just(storage1));
    when(cloudStorageRepository.deleteBucket(storage1.getBucket())).thenThrow(DeleteBucketFailedException.class);

    StepVerifier.create(storageService.deleteByIds(Flux.fromIterable(List.of(storage1.getId()))))
        .expectError(DeleteBucketFailedException.class)
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldFindAllStoragesByType(StorageType type) {
    Storage stagingStorage1 = new Storage.Builder()
        .id(123L)
        .bucket("test-bucket1")
        .path("/files")
        .type(type)
        .build();
    Storage stagingStorage2 = new Storage.Builder()
        .id(124L)
        .bucket("test-bucket2")
        .path("/files")
        .type(type)
        .build();
    when(storageRepository.findAllByType(type)).thenReturn(Flux.just(stagingStorage1, stagingStorage2));
    when(mapper.mapToRecord(stagingStorage1))
        .thenReturn(new StorageDTO.Builder().id(stagingStorage1.getId()).type(stagingStorage1.getType()).build());

    when(mapper.mapToRecord(stagingStorage2))
        .thenReturn(new StorageDTO.Builder().id(stagingStorage2.getId()).type(stagingStorage2.getType()).build());

    StepVerifier.create(storageService.findAllByType(type))
        .assertNext(result -> {
          assertEquals(stagingStorage1.getId(), result.getId());
          assertEquals(stagingStorage1.getType(), result.getType());
        })
        .assertNext(result -> {
          assertEquals(stagingStorage2.getId(), result.getId());
          assertEquals(stagingStorage2.getType(), result.getType());
        }).verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowExceptionWhenFindAllStoragesByType(StorageType type) {
    when(storageRepository.findAllByType(type)).thenReturn(Flux.empty());
    StepVerifier.create(storageService.findAllByType(type))
        .expectError(StorageNotFoundException.class)
        .verify();
  }

  private static Storage storage() {
    Random random = new Random();
    long id = random.nextInt(1000);
    return new Storage.Builder()
        .id(id)
        .bucket("test-bucket" + id)
        .path("/files")
        .type(random.nextBoolean() ? StorageType.STAGING : StorageType.PERMANENT)
        .build();
  }
}
