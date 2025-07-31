package ru.practicum;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);
}
