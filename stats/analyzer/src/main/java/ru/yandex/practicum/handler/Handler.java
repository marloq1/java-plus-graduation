package ru.yandex.practicum.handler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.ActionType;
import ru.yandex.practicum.model.Similarity;
import ru.yandex.practicum.model.SimilarityKey;
import ru.yandex.practicum.reposipory.ActionRepository;
import ru.yandex.practicum.reposipory.SimilarityRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {

    private final SimilarityRepository similarityRepository;
    private final ActionRepository actionRepository;

    @Transactional
    public void handle(EventSimilarityAvro eventSimilarityAvro) {
        Optional<Similarity> similarityOptional = similarityRepository.findByKey(SimilarityKey.builder()
                .eventId(eventSimilarityAvro.getEventA())
                .otherEventId(eventSimilarityAvro.getEventB())
                .build());
        Similarity similarity;
        if (similarityOptional.isPresent()) {
            similarity = similarityOptional.get();
            similarity.setScore(eventSimilarityAvro.getScore());
            similarity.setTimestamp(eventSimilarityAvro.getTimestamp());
        } else {
            similarity = Similarity.builder()
                    .key(SimilarityKey.builder().eventId(eventSimilarityAvro.getEventA())
                            .otherEventId(eventSimilarityAvro.getEventB()).build())
                    .score(eventSimilarityAvro.getScore())
                    .timestamp(eventSimilarityAvro.getTimestamp()).build();
        }
        similarityRepository.save(similarity);


    }

    @Transactional
    public void handle(UserActionAvro userActionAvro) {
        Optional<Action> actionOptional = actionRepository.findByEventIdAndUserId(userActionAvro.getEventId(),
                userActionAvro.getUserId());
        Action action;
        if (actionOptional.isPresent()) {
            action = actionOptional.get();
            if (actionTypeTODouble(action.getActionType()) <
                    actionTypeTODouble(ActionType.valueOf(userActionAvro.getActionType().name()))) {
                action.setActionType(ActionType.valueOf(userActionAvro.getActionType().name()));
                action.setTimestamp(userActionAvro.getTimestamp());
            }

        } else {
            action = Action.builder()
                    .eventId(userActionAvro.getEventId())
                    .userId(userActionAvro.getUserId())
                    .actionType(ActionType.valueOf(userActionAvro.getActionType().name()))
                    .timestamp(userActionAvro.getTimestamp()).build();
        }
        actionRepository.save(action);

    }

    private Double actionTypeTODouble(ActionType actionType) {
        switch (actionType) {
            case VIEW -> {
                return 0.4;
            }
            case REGISTER -> {
                return 0.8;
            }
            case LIKE -> {
                return 1D;
            }
        }
        return 0D;
    }


}
