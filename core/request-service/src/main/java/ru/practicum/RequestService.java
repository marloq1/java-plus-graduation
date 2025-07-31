package ru.practicum;


import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> findRequestsByUserId(Long userId);

    ParticipationRequestDto saveRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> findRequestsByEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest requestDto, Long userId,
                                                       Long eventId);
}
