package com.epam.training.microservicefoundation.storageservice.mapper;

import com.epam.training.microservicefoundation.storageservice.model.dto.SaveStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.entity.Storage;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface SaveStorageMapper extends BaseMapper<Storage, SaveStorageDTO> {
}
