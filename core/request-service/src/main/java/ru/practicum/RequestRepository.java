package ru.practicum;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dto.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<Request> findAllByIdInAndStatus(List<Long> requestIds, RequestStatus requestStatus);

    List<Request> findByEventId(Long eventId);
}
