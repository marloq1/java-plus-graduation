package ru.yandex.practicum.reposipory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Similarity;
import ru.yandex.practicum.model.SimilarityKey;

import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity,Long> {

    Optional<Similarity> findByKey(SimilarityKey similarityKey);

    @Query("""
    SELECT s
    FROM Similarity s
    WHERE (
        s.key.eventId IN :eventIds AND s.key.otherEventId NOT IN :eventIds
    ) OR (
        s.key.otherEventId IN :eventIds AND s.key.eventId NOT IN :eventIds
    )
    ORDER BY s.score DESC
""")
    List<Similarity> findNPairsByEventIds(@Param("eventIds")List<Long> eventIds);
}
