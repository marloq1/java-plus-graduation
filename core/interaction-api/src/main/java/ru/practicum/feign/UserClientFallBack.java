package ru.practicum.feign;

import org.springframework.stereotype.Component;
import ru.practicum.dto.UserDto;

import java.util.List;

@Component
public class UserClientFallBack implements UserClient {
    @Override
    public UserDto getUser(Long userId) {
        return UserDto.builder().build();
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        return List.of();
    }
}
