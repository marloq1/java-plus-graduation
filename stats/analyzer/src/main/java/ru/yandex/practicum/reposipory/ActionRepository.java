package ru.yandex.practicum.reposipory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Action;

import java.util.List;
import java.util.Optional;

public interface ActionRepository extends JpaRepository<Action, Long> {

    Optional<Action> findByEventIdAndUserId(Long eventId, Long userId);

    List<Action> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    List<Action> findByUserId(Long userId);

    List<Action> findByEventIdIn(List<Long> eventIds);
}
