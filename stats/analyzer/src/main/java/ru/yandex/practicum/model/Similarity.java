package ru.yandex.practicum.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "similarities")
public class Similarity {
    @EmbeddedId
    private SimilarityKey key;

    private double score;

    private Instant timestamp;
}
