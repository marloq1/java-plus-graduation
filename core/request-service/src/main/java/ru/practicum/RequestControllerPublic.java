package ru.practicum;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestControllerPublic {

    final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> findRequestsByEventId(@PathVariable Long userId,
                                                               @PathVariable Long eventId) {
        return requestService.findRequestsByEventId(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateRequestStatus(
            @RequestBody EventRequestStatusUpdateRequest requestDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return requestService.updateRequestStatus(requestDto, userId, eventId);
    }
}
