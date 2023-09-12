package com.epam.training.microservicefoundation.storageservice.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.training.microservicefoundation.storageservice.configuration.TestsMappersConfig;
import com.epam.training.microservicefoundation.storageservice.domain.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.entity.Storage;
import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.service.mapper.DeleteStorageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestsMappersConfig.class)
class DeleteStorageMapperTest {
  @Autowired
  private DeleteStorageMapper deleteStorageMapper;

  private final Storage stagingStorage = Storage.builder().id(1L).bucket("test-bucket1").path("files/").type(StorageType.STAGING).build();
  private final Storage permanentStorage = Storage.builder().id(2L).bucket("test-bucket2").path("files/").type(StorageType.PERMANENT).build();
  private final DeleteStorageDTO deleteStagingStorageDTO = new DeleteStorageDTO(1L);
  private final DeleteStorageDTO deletePermanentStorageDTO = new DeleteStorageDTO(2L);

  @Test
  void toDtoMapping() {
    DeleteStorageDTO stagingStorageDto = deleteStorageMapper.toDto(stagingStorage);
    assertEquals(deleteStagingStorageDTO, stagingStorageDto);

    DeleteStorageDTO permanentStorageDto = deleteStorageMapper.toDto(permanentStorage);
    assertEquals(deletePermanentStorageDTO, permanentStorageDto);
  }

  @Test
  void toEntityMapping() {
    Storage stagingStorageEntity = deleteStorageMapper.toEntity(deleteStagingStorageDTO);
    assertEquals(stagingStorage.getId(), stagingStorageEntity.getId());

    Storage permanentStorageEntity = deleteStorageMapper.toEntity(deletePermanentStorageDTO);
    assertEquals(permanentStorage.getId(), permanentStorageEntity.getId());
  }
}
