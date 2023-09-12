package com.epam.training.microservicefoundation.storageservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.epam.training.microservicefoundation.storageservice.service.mapper.DeleteStorageMapper;
import com.epam.training.microservicefoundation.storageservice.service.mapper.GetStorageMapper;
import com.epam.training.microservicefoundation.storageservice.service.mapper.SaveStorageMapper;
import com.epam.training.microservicefoundation.storageservice.domain.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.entity.Storage;
import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.domain.exception.CloudStorageException;
import com.epam.training.microservicefoundation.storageservice.domain.exception.EntityExistsException;
import com.epam.training.microservicefoundation.storageservice.domain.exception.EntityNotFoundException;
import com.epam.training.microservicefoundation.storageservice.repository.CloudStorageRepository;
import com.epam.training.microservicefoundation.storageservice.repository.StorageRepository;
import com.epam.training.microservicefoundation.storageservice.service.implementation.StorageServiceImpl;
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

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

  @Mock
  private StorageRepository storageRepository;
  @Mock
  private CloudStorageRepository cloudStorageRepository;
  @Mock
  private SaveStorageMapper saveStorageMapper;
  @Mock
  private GetStorageMapper getStorageMapper;
  @Mock
  private DeleteStorageMapper deleteStorageMapper;
  @InjectMocks
  private StorageServiceImpl storageService;

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldSaveStorage(StorageType type) {
    final SaveStorageDTO saveStorageDTO = getSaveStorageDTOByType(type);
    final Storage storageEntity = getStorageEntityBySaveStorageDTO(saveStorageDTO);
    when(saveStorageMapper.toEntity(saveStorageDTO)).thenReturn(storageEntity);
    when(cloudStorageRepository.createBucket(storageEntity.getBucket())).thenReturn(Mono.empty());
    final Storage savedStorageEntity = storageEntity.toBuilder().id(123L).build();
    when(storageRepository.save(storageEntity)).thenReturn(Mono.just(savedStorageEntity));
    when(getStorageMapper.toDto(savedStorageEntity)).thenReturn(getGetStorageDTOBySavedStorageEntity(savedStorageEntity));

    assertStorage(savedStorageEntity, storageService.save(Mono.just(saveStorageDTO)));
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowCreateBucketFailedExceptionWhenSaveStorage(StorageType type) {
    final SaveStorageDTO saveStorageDTO = getSaveStorageDTOByType(type);
    final Storage storageEntity = getStorageEntityBySaveStorageDTO(saveStorageDTO);
    when(saveStorageMapper.toEntity(saveStorageDTO)).thenReturn(storageEntity);
    when(cloudStorageRepository.createBucket(storageEntity.getBucket())).thenThrow(CloudStorageException.class);

    StepVerifier.create(storageService.save(Mono.just(saveStorageDTO)))
        .expectError(CloudStorageException.class)
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowIllegalArgumentExceptionWhenSaveStorage(StorageType type) {
    final SaveStorageDTO saveStorageDTO = getSaveStorageDTOByType(type);
    final Storage storageEntity = getStorageEntityBySaveStorageDTO(saveStorageDTO);
    when(saveStorageMapper.toEntity(saveStorageDTO)).thenReturn(storageEntity);
    when(cloudStorageRepository.createBucket(storageEntity.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.save(storageEntity)).thenThrow(DataIntegrityViolationException.class);

    StepVerifier.create(storageService.save(Mono.just(saveStorageDTO)))
        .expectError(EntityExistsException.class)
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldFindStorageById(StorageType type) {
    final Storage savedStorageEntity = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity.getId())).thenReturn(Mono.just(savedStorageEntity));
    when(cloudStorageRepository.sendHeadRequest(savedStorageEntity.getBucket())).thenReturn(Mono.empty());
    final GetStorageDTO getStorageDTO = getGetStorageDTOBySavedStorageEntity(savedStorageEntity);
    when(getStorageMapper.toDto(savedStorageEntity)).thenReturn(getStorageDTO);

    assertStorage(savedStorageEntity, storageService.getById(savedStorageEntity.getId()));
  }

  @Test
  void shouldThrowStorageNotFoundExceptionWhenFindStorageById() {
    when(storageRepository.findById(anyLong())).thenReturn(Mono.empty());
    StepVerifier.create(storageService.getById(144L))
        .expectSubscription()
        .expectError(EntityNotFoundException.class)
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowHeadBucketFailedExceptionWhenFindStorageById(StorageType type) {
    final Storage savedStorageEntity = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity.getId())).thenReturn(Mono.just(savedStorageEntity));
    when(cloudStorageRepository.sendHeadRequest(savedStorageEntity.getBucket())).thenThrow(CloudStorageException.class);

    StepVerifier.create(storageService.getById(savedStorageEntity.getId()))
        .expectSubscription()
        .expectError(CloudStorageException.class)
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldDeleteStorage(StorageType type) {
    final Storage savedStorageEntity1 = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity1.getId())).thenReturn(Mono.just(savedStorageEntity1));
    when(cloudStorageRepository.deleteBucket(savedStorageEntity1.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(savedStorageEntity1)).thenReturn(Mono.empty());
    final DeleteStorageDTO deleteStorageDTO1 = getDeleteStorageDTO(savedStorageEntity1);
    when(deleteStorageMapper.toDto(savedStorageEntity1)).thenReturn(deleteStorageDTO1);

    final Storage savedStorageEntity2 = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity2.getId())).thenReturn(Mono.just(savedStorageEntity2));
    when(cloudStorageRepository.deleteBucket(savedStorageEntity2.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(savedStorageEntity2)).thenReturn(Mono.empty());
    final DeleteStorageDTO deleteStorageDTO2 = getDeleteStorageDTO(savedStorageEntity2);
    when(deleteStorageMapper.toDto(savedStorageEntity2)).thenReturn(deleteStorageDTO2);

    StepVerifier.create(storageService.deleteByIds(new Long[] {savedStorageEntity1.getId(), savedStorageEntity2.getId()}))
        .assertNext(result -> {
          assertEquals(savedStorageEntity1.getId(), result.getId());
        })
        .assertNext(result -> {
          assertEquals(savedStorageEntity2.getId(), result.getId());
        })
        .expectComplete()
        .verify();
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenDeleteStoragesByIdWithEmptyFlux() {
    StepVerifier.create(storageService.deleteByIds(new Long[0]))
        .expectSubscription()
        .expectNextCount(0)
        .verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldDeleteStoragesByIdAndOneOfThemNotFound(StorageType type) {
    final Storage savedStorageEntity1 = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity1.getId())).thenReturn(Mono.empty());

    final Storage savedStorageEntity2 = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity2.getId())).thenReturn(Mono.just(savedStorageEntity2));
    when(cloudStorageRepository.deleteBucket(savedStorageEntity2.getBucket())).thenReturn(Mono.empty());
    when(storageRepository.delete(savedStorageEntity2)).thenReturn(Mono.empty());
    final DeleteStorageDTO deleteStorageDTO2 = getDeleteStorageDTO(savedStorageEntity2);
    when(deleteStorageMapper.toDto(savedStorageEntity2)).thenReturn(deleteStorageDTO2);

    StepVerifier.create(storageService.deleteByIds(new Long[]{savedStorageEntity1.getId(), savedStorageEntity2.getId()}))
        .assertNext(result -> {
          assertEquals(savedStorageEntity2.getId(), result.getId());
        })
        .expectComplete()
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowIllegalArgumentExceptionWhenDeleteStoragesByIdAndAllItemsNotFound(StorageType type) {
    final Storage savedStorageEntity1  = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity1.getId())).thenReturn(Mono.empty());
    final Storage savedStorageEntity2 = getSavedStorageByType(type);
    when(storageRepository.findById(savedStorageEntity2.getId())).thenReturn(Mono.empty());

    StepVerifier.create(storageService.deleteByIds(new Long[]{savedStorageEntity1.getId(), savedStorageEntity2.getId()}))
        .expectSubscription()
        .expectNextCount(0)
        .verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowDeleteBucketFailedExceptionWhenDeleteStoragesById(StorageType type) {
    final Storage savedStagingEntity = getSavedStorageByType(type);
    when(storageRepository.findById(savedStagingEntity.getId())).thenReturn(Mono.just(savedStagingEntity));
    when(cloudStorageRepository.deleteBucket(savedStagingEntity.getBucket())).thenThrow(CloudStorageException.class);

    StepVerifier.create(storageService.deleteByIds(new Long[]{savedStagingEntity.getId()}))
        .expectError(CloudStorageException.class)
        .verify();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldFindAllStoragesByType(StorageType type) {
    Storage savedStorageEntity1 = getSavedStorageByType(type);
    Storage savedStorageEntity2 = getSavedStorageByType(type);
    when(storageRepository.findAllByType(type)).thenReturn(Flux.just(savedStorageEntity1, savedStorageEntity2));
    when(getStorageMapper.toDto(savedStorageEntity1)).thenReturn(getGetStorageDTOBySavedStorageEntity(savedStorageEntity1));
    when(getStorageMapper.toDto(savedStorageEntity2)).thenReturn(getGetStorageDTOBySavedStorageEntity(savedStorageEntity2));

    StepVerifier.create(storageService.findAllByType(type))
        .assertNext(result -> {
          assertEquals(savedStorageEntity1.getId(), result.getId());
          assertEquals(savedStorageEntity1.getType(), result.getType());
        })
        .assertNext(result -> {
          assertEquals(savedStorageEntity2.getId(), result.getId());
          assertEquals(savedStorageEntity2.getType(), result.getType());
        }).verifyComplete();
  }

  @ParameterizedTest
  @EnumSource(StorageType.class)
  void shouldThrowExceptionWhenFindAllStoragesByType(StorageType type) {
    when(storageRepository.findAllByType(type)).thenReturn(Flux.empty());
    StepVerifier.create(storageService.findAllByType(type))
        .expectError(EntityNotFoundException.class)
        .verify();
  }

  private Storage getStorageEntityBySaveStorageDTO(SaveStorageDTO dto) {
    return Storage.builder().bucket(dto.getBucket()).path(dto.getPath()).type(dto.getType()).build();
  }

  private GetStorageDTO getGetStorageDTOBySavedStorageEntity(Storage storage) {
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

  private SaveStorageDTO getSaveStorageDTOByType(StorageType type) {
    return new SaveStorageDTO("test-bucket-" + RANDOM.nextInt(1000), "files/", type);
  }

  private void assertStorage(Storage expected, Mono<GetStorageDTO> actual) {
    StepVerifier.create(actual)
        .assertNext(result -> {
          assertEquals(expected.getId(), result.getId());
          assertEquals(expected.getBucket(), result.getBucket());
          assertEquals(expected.getPath(), result.getPath());
          assertEquals(expected.getType(), result.getType());
        })
        .verifyComplete();
  }
}
