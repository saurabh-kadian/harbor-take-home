package xyz.harbor.calendly_based_take_home.mapper;

import xyz.harbor.calendly_based_take_home.response.UserResponse;
import xyz.harbor.calendly_based_take_home.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse modelToResponse(User user);
}
