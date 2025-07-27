package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.EventFullDto;

@FeignClient(name = "event-service", path = "/events")
public interface EventClient {

    @GetMapping("/internal/{id}")
    public EventFullDto getEventByIdInternal(@PathVariable Long id);

    @PutMapping
    void changeEventFields(@RequestBody EventFullDto eventFullDto);
}
