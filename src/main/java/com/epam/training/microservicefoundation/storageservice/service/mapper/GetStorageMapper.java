package com.epam.training.microservicefoundation.storageservice.service.mapper;

import com.epam.training.microservicefoundation.storageservice.domain.dto.GetStorageDTO;
import com.epam.training.microservicefoundation.storageservice.domain.entity.Storage;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface GetStorageMapper extends BaseMapper<Storage, GetStorageDTO> {
}
