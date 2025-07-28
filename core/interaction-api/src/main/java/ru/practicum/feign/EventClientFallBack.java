package ru.practicum.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;

@Component
@Slf4j
public class EventClientFallBack implements EventClient {
    @Override
    public EventFullDto getEventByIdInternal(Long id) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(id);
        return eventFullDto;

    }

    @Override
    public void changeEventFields(EventFullDto eventFullDto) {
      log.info("Сервис недоступен");
    }
}
