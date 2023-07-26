package com.epam.training.microservicefoundation.storageservice.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.training.microservicefoundation.storageservice.configuration.TestsMappersConfig;
import com.epam.training.microservicefoundation.storageservice.model.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.entity.Storage;
import com.epam.training.microservicefoundation.storageservice.model.entity.StorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestsMappersConfig.class)
class SaveStorageMapperTest {
  @Autowired
  private SaveStorageMapper saveStorageMapper;
  private final Storage stagingStorage = Storage.builder().bucket("test-bucket1").path("files/").type(StorageType.STAGING).build();
  private final Storage permanentStorage = Storage.builder().bucket("test-bucket2").path("files/").type(StorageType.PERMANENT).build();
  private final SaveStorageDTO saveStagingStorageDTO = new SaveStorageDTO("test-bucket1", "files/", StorageType.STAGING);
  private final SaveStorageDTO savePermanentStorageDTO = new SaveStorageDTO("test-bucket2", "files/", StorageType.PERMANENT);

  @Test
  void toEntityMapping() {
    Storage stagingStorageEntity = saveStorageMapper.toEntity(saveStagingStorageDTO);
    assertEquals(stagingStorage, stagingStorageEntity);

    Storage permanentStorageEntity = saveStorageMapper.toEntity(savePermanentStorageDTO);
    assertEquals(permanentStorage, permanentStorageEntity);
  }

  @Test
  void toDtoMapping() {
    SaveStorageDTO stagingStorageDto = saveStorageMapper.toDto(stagingStorage);
    assertEquals(saveStagingStorageDTO, stagingStorageDto);

    SaveStorageDTO permanentStorageDto = saveStorageMapper.toDto(permanentStorage);
    assertEquals(savePermanentStorageDTO, permanentStorageDto);
  }
}
