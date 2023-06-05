package com.epam.training.microservices.storageservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.training.microservices.storageservice.common.PostgresExtension;
import com.epam.training.microservices.storageservice.configuration.DatasourceConfiguration;
import com.epam.training.microservices.storageservice.model.Storage;
import com.epam.training.microservices.storageservice.model.StorageType;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ExtendWith(PostgresExtension.class)
@DirtiesContext
@ContextConfiguration(classes = DatasourceConfiguration.class)
@TestPropertySource(locations = "classpath:application.properties")
class StorageRepositoryTest {

  @Autowired
  private StorageRepository repository;

  @AfterEach
  public void cleanUp() {
    StepVerifier.create(repository.deleteAll())
        .verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldSaveStorage(StorageType type) {
    Storage storage = storage(type);
    assertStorageResult(storage, repository.save(storage));
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowExceptionWhenSaveStorageWithNullBucketValue(StorageType type) {
    Storage storage = new Storage.Builder().bucket(null).path("/files").type(type).build();
    StepVerifier.create(repository.save(storage))
        .expectError(DataIntegrityViolationException.class)
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowExceptionWhenSaveStorageWithNullPathValue(StorageType type) {
    Storage storage = new Storage.Builder().bucket("test-bucket").path(null).type(type).build();
    StepVerifier.create(repository.save(storage))
        .expectError(DataIntegrityViolationException.class)
        .verify();
  }

  @Test
  void shouldThrowExceptionWhenSaveStorageWithNullTypeValue() {
    Storage storage = new Storage.Builder().bucket("test-bucket").path("/files").type(null).build();
    StepVerifier.create(repository.save(storage))
        .expectError(DataIntegrityViolationException.class)
        .verify();
  }

  @Test
  void shouldThrowExceptionWhenSaveStorageWithDuplicateName() {
    Storage storage1 = storage(StorageType.PERMANENT);
    assertStorageResult(storage1, repository.save(storage1));

    Storage storage2 = new Storage.Builder().bucket(storage1.getBucket()).path("/files").type(StorageType.STAGING).build();
    StepVerifier.create(repository.save(storage2))
        .expectError(DataIntegrityViolationException.class).verify();
  }

  @Test
  void shouldGetStorageById() {
    Storage storage = storage(StorageType.STAGING);
    assertStorageResult(storage, repository.save(storage).flatMap(result -> repository.findById(result.getId())));
  }

  @Test
  void shouldReturnEmptyWhenFindStorageById() {
    long id = 123_433_664L;
    StepVerifier.create(repository.findById(id))
        .expectSubscription()
        .expectNextCount(0L)
        .verifyComplete();
  }

  @Test
  void shouldFindAllStorages() {
    Storage storage1 = storage(StorageType.STAGING);
    Storage storage2 = storage(StorageType.PERMANENT);
    assertStorageResult(storage1, repository.save(storage1));
    assertStorageResult(storage2, repository.save(storage2));


    StepVerifier.create(repository.findAll())
        .expectNext(storage1)
        .expectNext(storage2)
        .expectComplete()
        .verify();
  }

  @Test
  void shouldReturnEmptyWhenFindAllStorages() {
    StepVerifier.create(repository.findAll())
        .expectSubscription()
        .expectNextCount(0L)
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldDelete(StorageType type) {
    Storage storage = storage(type);
    assertStorageResult(storage, repository.save(storage));

    StepVerifier.create(repository.findAll().flatMap(result -> repository.delete(result)))
        .expectSubscription()
        .expectNextCount(0L)
        .verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldReturnEmptyWhenDelete(StorageType type) {
    Storage storage = storage(type);
    StepVerifier.create(repository.delete(storage))
        .expectSubscription()
        .expectNextCount(0L)
        .verifyComplete();
  }

  private static void assertStorageResult(Storage expected, Mono<Storage> actual) {
    StepVerifier.create(actual)
        .assertNext(result -> {
          assertTrue(result.getId() > 0L);
          assertEquals(expected.getBucket(), result.getBucket());
          assertEquals(expected.getPath(), result.getPath());
          assertEquals(expected.getType(), result.getType());
          assertNotNull(result.getCreatedDate());
          assertNotNull(result.getLastModifiedDate());
        }).verifyComplete();
  }

  private static Storage storage(StorageType type) {
    Random random = new Random();
    return new Storage.Builder()
        .bucket("test-bucket-" + random.nextInt(1000))
        .path("/files")
        .type(type)
        .build();
  }
}
