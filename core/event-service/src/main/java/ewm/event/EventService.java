package ewm.event;



import ru.practicum.dto.*;

import java.util.List;

public interface EventService {
    EventFullDto saveEvent(NewEventDto newEventDto, Long userId);

    List<EventShortDto> findEventsByInitiatorId(Long userId, Integer from, Integer size);

    EventFullDto findById(Long userId, Long eventId);

    EventFullDto updateEvent(UpdateEventRequest updateEventRequest, Long userId, Long eventId);

    /*List<ParticipationRequestDto> findRequestsByEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                       Long eventId);*/
}

