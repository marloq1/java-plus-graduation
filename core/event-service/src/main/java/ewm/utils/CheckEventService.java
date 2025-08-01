package ewm.utils;

import ewm.event.Event;
import ewm.event.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;
import ru.practicum.dto.State;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckEventService {
    private final EventRepository eventRepository;

    public Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    public Event checkPublishedEvent(Long eventId) {
        return eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }
}
