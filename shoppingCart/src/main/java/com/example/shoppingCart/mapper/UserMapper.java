package com.example.shoppingCart.mapper;

import com.example.shoppingCart.entity.User;
import com.example.shoppingCart.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring") // "spring" integrates with Spring DI
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Entity To Model
    @Mapping(target="roles", ignore = true)
    UserModel userToUserModel(User user);

    // Model To Entity
    @Mapping(target="userRoles", ignore = true)
    User userModelToUser(UserModel userModel);

    // Model To Entity
    User updateUserModel(UserModel userModel, @MappingTarget User user);

}
