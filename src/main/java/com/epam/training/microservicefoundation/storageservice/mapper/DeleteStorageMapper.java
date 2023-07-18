package com.epam.training.microservicefoundation.storageservice.mapper;

import com.epam.training.microservicefoundation.storageservice.model.dto.DeleteStorageDTO;
import com.epam.training.microservicefoundation.storageservice.model.entity.Storage;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface DeleteStorageMapper extends BaseMapper<Storage, DeleteStorageDTO>{
}
