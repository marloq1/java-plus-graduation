package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimilarityService {

    private final Map<Long, Map<Long, Double>> weights; //Map<EventId,Map<UserId,Weight>>
    private final Map<Long, Double> partialSum;//Map<EventId,Sum>
    private final Map<Long, Map<Long, Double>> partialSumOfPair; //Map<EventAId,Map<EventBId,SumAB>>


    public List<EventSimilarityAvro> calculateSimilarities(UserActionAvro userActionAvro) {
        log.info("Расчет сходств для события {} для юзера {}", userActionAvro.getEventId(), userActionAvro.getUserId());
        Double weightOld = 0D;
        Double weightNew = 0D;
        List<EventSimilarityAvro> eventSimilarityAvroList = new ArrayList<>();
        if (!weights.containsKey(userActionAvro.getEventId())) {
            Map<Long, Double> eventWeights = new HashMap<>();
            eventWeights.put(userActionAvro.getUserId(),
                    mapActionTypeToWeight(userActionAvro.getActionType()));
            weights.put(userActionAvro.getEventId(), eventWeights);
            partialSum.put(userActionAvro.getEventId(), weights.get(userActionAvro.getEventId())
                    .get(userActionAvro.getUserId()));
        } else {
            weightOld = weights.get(userActionAvro.getEventId()).get(userActionAvro.getUserId());
            log.info("Старый вес действия - {}", weightOld);
            if (weightOld == null) {
                weightOld = 0D;
            }
            if (weights.get(userActionAvro.getEventId()).containsKey(userActionAvro.getUserId())) {
                if (weightOld >= mapActionTypeToWeight(userActionAvro.getActionType())) {
                    return eventSimilarityAvroList;
                }
            }
            Map<Long, Double> eventWeights = weights.get(userActionAvro.getEventId());
            eventWeights.put(userActionAvro.getUserId(), mapActionTypeToWeight(userActionAvro.getActionType()));
            weights.put(userActionAvro.getEventId(), eventWeights);
            weightNew = weights.get(userActionAvro.getEventId()).get(userActionAvro.getUserId());
            log.info("Новый вес действия - {}", weightNew);
            partialSum.put(userActionAvro.getEventId(), partialSum.get(userActionAvro.getEventId()) + weightNew - weightOld);

        }

        log.info("{}", weights);
        for (Map.Entry<Long, Map<Long, Double>> entry : weights.entrySet()) {
            if (!entry.getKey().equals(userActionAvro.getEventId())) {
                for (Map.Entry<Long, Double> weightMap : entry.getValue().entrySet()) {

                    if (weightMap.getKey().equals(userActionAvro.getUserId())) {
                        Double minOld = Math.min(weightMap.getValue(), weightOld);
                        log.info("Старый вклад в общую сумму - {}", minOld);
                        Double minNew = Math.min(weightMap.getValue(), weightNew);
                        log.info("Новый вклад в общую сумму - {}", minNew);
                        log.info("До ветвлений: {}", partialSumOfPair);
                        log.info("Entry.getKey() - {}", entry.getKey());
                        if (partialSumOfPair.get(entry.getKey()) != null &&
                                partialSumOfPair.get(entry.getKey()).containsKey(userActionAvro.getEventId())) {

                            Map<Long, Double> eventPairSum = partialSumOfPair.get(entry.getKey());
                            eventPairSum.put(userActionAvro.getEventId(), eventPairSum.get(userActionAvro.getEventId()) +
                                    minNew - minOld);
                            partialSumOfPair.put(entry.getKey(), eventPairSum);
                            log.info("Ветвление 1: {}", partialSumOfPair);
                        } else if (partialSumOfPair.get(userActionAvro.getEventId()) == null) {
                            Map<Long, Double> eventPairSum = new HashMap<>();
                            eventPairSum.put(entry.getKey(),
                                    Math.min(mapActionTypeToWeight(userActionAvro.getActionType()),
                                            weightMap.getValue()));
                            partialSumOfPair.put(userActionAvro.getEventId(), eventPairSum);
                            log.info("Ветвление 2: {}", partialSumOfPair);
                        } else if (partialSumOfPair.get(userActionAvro.getEventId()).get(entry.getKey()) == null) {
                            Map<Long, Double> eventPairSum = partialSumOfPair.get(userActionAvro.getEventId());
                            eventPairSum.put(entry.getKey(),
                                    Math.min(mapActionTypeToWeight(userActionAvro.getActionType()),
                                            weightMap.getValue()));
                            partialSumOfPair.put(userActionAvro.getEventId(), eventPairSum);
                            log.info("Ветвление 3: {}", partialSumOfPair);
                        } else {
                            Map<Long, Double> eventPairSum = partialSumOfPair.get(userActionAvro.getEventId());

                            eventPairSum.put(entry.getKey(), eventPairSum.get(entry.getKey()) +
                                    minNew - minOld);
                            partialSumOfPair.put(userActionAvro.getEventId(), eventPairSum);
                            log.info("Ветвление 4: {}", partialSumOfPair);

                        }
                        Double sA = partialSum.get(userActionAvro.getEventId());
                        log.info("Sa  - {}", sA);
                        Double sB = partialSum.get(entry.getKey());
                        log.info("Sb  - {}", sB);
                        Double sAB = null;
                        if (partialSumOfPair.get(userActionAvro.getEventId()) != null) {
                            sAB = partialSumOfPair.get(userActionAvro.getEventId()).get(entry.getKey());
                        }
                        if (sAB == null) {
                            sAB = partialSumOfPair.get(entry.getKey()).get(userActionAvro.getEventId());
                        }
                        log.info("Sab  - {}", sAB);
                        if (sA != null && sB != null && sAB != null) {
                            long eventA = userActionAvro.getEventId();
                            long eventB = entry.getKey();
                            if (eventA > eventB) {
                                long c = eventA;
                                eventA = eventB;
                                eventB = c;
                            }
                            eventSimilarityAvroList.add(EventSimilarityAvro.newBuilder()
                                    .setEventA(eventA).setEventB(eventB)
                                    .setScore(sAB / (Math.sqrt(sA) * Math.sqrt(sB)))
                                    .setTimestamp(userActionAvro.getTimestamp()).build());
                        }
                    }
                }
            }

        }
        return eventSimilarityAvroList;
    }

    private Double mapActionTypeToWeight(ActionTypeAvro actionTypeAvro) {
        switch (actionTypeAvro) {
            case VIEW -> {
                return 0.4;
            }
            case REGISTER -> {
                return 0.8;
            }
            case LIKE -> {
                return 1.0;
            }
        }
        return 0.0;

    }


}
