package ewm.event.pub;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;

import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                  String rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                  Integer size, HttpServletRequest request);

    EventFullDto getEventById(Long id, HttpServletRequest request);

    void changeEventFields(EventFullDto eventFullDto);


    EventFullDto getEventByIdInternal(Long id);
}
