package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/users/{userId}/requests")
public interface RequestClient {

    @GetMapping
    List<ParticipationRequestDto> findRequestsByUserId(@PathVariable Long userId);
}
