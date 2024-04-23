package com.earthone.earthone.mapper;


import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.earthone.earthone.dto.UserDto;
import com.earthone.earthone.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto map(UserEntity userEntity);

    @InheritInverseConfiguration
    UserEntity map(UserDto dto);
}
