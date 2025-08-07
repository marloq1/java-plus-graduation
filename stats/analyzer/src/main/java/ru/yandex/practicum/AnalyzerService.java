package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.user.InteractionsCountRequestProto;
import ru.yandex.practicum.grpc.telemetry.user.RecommendedEventProto;
import ru.yandex.practicum.grpc.telemetry.user.SimilarEventsRequestProto;
import ru.yandex.practicum.grpc.telemetry.user.UserPredictionsRequestProto;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.ActionType;
import ru.yandex.practicum.model.Similarity;
import ru.yandex.practicum.reposipory.ActionRepository;
import ru.yandex.practicum.reposipory.SimilarityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzerService {

    private final ActionRepository actionRepository;
    private final SimilarityRepository similarityRepository;
    private Double predictNumerator;
    private Double predictDenominator;

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto requestProto) {
        log.info("UserPredictionsRequestProto - {},{}", requestProto.getUserId(), requestProto.getMaxResults());

        List<Action> actions = actionRepository.findByUserIdOrderByTimestampDesc(requestProto.getUserId(),
                PageRequest.of(0, requestProto.getMaxResults()));
        List<Long> eventIds = actions.stream().map(Action::getEventId).toList();
        List<Similarity> similarities = similarityRepository.findNPairsByEventIds(eventIds);
        List<Long> otherEventIds = getOtherEventIds(similarities, eventIds);
        otherEventIds = otherEventIds.stream().limit(requestProto.getMaxResults()).toList();
        List<RecommendedEventProto> recommendedEvents = new ArrayList<>();
        for (Long otherEventId : otherEventIds) {
            Map<Long, Double> eventSimilarities = similarities.stream()
                    .filter(s -> (s.getKey().getEventId() == otherEventId) || (s.getKey().getOtherEventId() == otherEventId))
                    .collect(Collectors.toMap(s -> s.getKey().getEventId() == otherEventId
                                    ? s.getKey().getOtherEventId()
                                    : s.getKey().getEventId(),
                            Similarity::getScore,
                            (v1, v2) -> v1));
            Map<Long, Double> eventWeights = actions.stream().filter(a -> eventSimilarities.containsKey(a.getEventId()))
                    .collect(Collectors.toMap(Action::getEventId, a -> actionTypeTODouble(a.getActionType())));
            predictNumerator = 0D;
            predictDenominator = 0D;
            eventSimilarities.keySet().forEach(key -> predictNumerator += eventSimilarities.get(key) * eventWeights.get(key));
            eventSimilarities.keySet().forEach(key -> predictDenominator += eventSimilarities.get(key));
            if (predictDenominator != 0D) {
                recommendedEvents.add(RecommendedEventProto.newBuilder().setEventId(otherEventId)
                        .setScore(predictNumerator / predictDenominator).build());
            }
        }


        return recommendedEvents;
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {

        List<Long> eventIds = actionRepository.findByUserId(request.getUserId())
                .stream().map(Action::getEventId).toList();
        List<Similarity> similarities = similarityRepository.findNPairsByEventIds(eventIds);
        return similarities.stream().map(similarity -> {
            if (eventIds.contains(similarity.getKey().getEventId())) {
                return RecommendedEventProto.newBuilder().setEventId(similarity.getKey().getOtherEventId())
                        .setScore(similarity.getScore()).build();
            } else {
                return RecommendedEventProto.newBuilder().setEventId(similarity.getKey().getEventId())
                        .setScore(similarity.getScore()).build();
            }
        }).toList();
    }


    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("request - {}", request);
        List<Action> actions = actionRepository.findByEventIdIn(request.getEventIdList());
        log.info("actions - {}", actions);
        List<RecommendedEventProto> recommendedEvents = new ArrayList<>();
        for (Long eventId : request.getEventIdList()) {
            recommendedEvents.add(RecommendedEventProto.newBuilder()
                    .setEventId(eventId).setScore(actions.stream().filter(a -> Objects.equals(a.getEventId(), eventId))
                            .map(action -> actionTypeTODouble(action.getActionType())).mapToDouble(Double::doubleValue).sum())
                    .build());
        }
        return recommendedEvents;
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


    private static List<Long> getOtherEventIds(List<Similarity> similarities, List<Long> eventIds) {
        List<Long> otherEventIds = new ArrayList<>();
        for (Similarity similarity : similarities) {
            if (eventIds.contains(similarity.getKey().getEventId())) {
                if (!otherEventIds.contains(similarity.getKey().getOtherEventId())) {
                    otherEventIds.add(similarity.getKey().getOtherEventId());
                }
            } else {
                if (!otherEventIds.contains(similarity.getKey().getEventId())) {
                    otherEventIds.add(similarity.getKey().getEventId());
                }
            }
        }
        return otherEventIds;
    }


}
