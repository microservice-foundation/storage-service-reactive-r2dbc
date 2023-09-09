package com.epam.training.microservicefoundation.storageservice.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.training.microservicefoundation.storageservice.configuration.TestsMappersConfig;
import com.epam.training.microservicefoundation.storageservice.domain.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.entity.Storage;
import com.epam.training.microservicefoundation.storageservice.domain.entity.StorageType;
import com.epam.training.microservicefoundation.storageservice.service.mapper.GetStorageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestsMappersConfig.class)
class GetStorageMapperTest {
  @Autowired
  private GetStorageMapper getStorageMapper;

  private final Storage stagingStorage = Storage.builder().id(1L).bucket("test-bucket1").path("files/").type(StorageType.STAGING).build();
  private final Storage permanentStorage = Storage.builder().id(2L).bucket("test-bucket2").path("files/").type(StorageType.PERMANENT).build();
  private final GetStorageDTO getStagingStorageDTO = new GetStorageDTO(1L, "test-bucket1", "files/", StorageType.STAGING);
  private final GetStorageDTO getPermanentStorageDTO = new GetStorageDTO(2L, "test-bucket2", "files/", StorageType.PERMANENT);

  @Test
  void toDtoMapping() {
    GetStorageDTO stagingStorageDto = getStorageMapper.toDto(stagingStorage);
    assertEquals(getStagingStorageDTO, stagingStorageDto);

    GetStorageDTO permanentStorageDto = getStorageMapper.toDto(permanentStorage);
    assertEquals(getPermanentStorageDTO, permanentStorageDto);
  }

  @Test
  void toEntityMapping() {
    Storage stagingStorageEntity = getStorageMapper.toEntity(getStagingStorageDTO);
    assertEquals(stagingStorage, stagingStorageEntity);

    Storage permanentStorageEntity = getStorageMapper.toEntity(getPermanentStorageDTO);
    assertEquals(permanentStorage, permanentStorageEntity);
  }
}
