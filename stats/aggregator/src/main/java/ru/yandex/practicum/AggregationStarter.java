package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.kafka.KafkaEventConsumer;
import ru.yandex.practicum.kafka.KafkaEventProducer;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaEventConsumer consumer;
    private final KafkaEventProducer producer;
    private final SimilarityService service;
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final List<String> TOPICS = List.of("stats.user-actions.v1");
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        try {
            consumer.subscribe(TOPICS);
            while (true) {

                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                int count = 0;
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {

                   List<EventSimilarityAvro> snap = service.calculateSimilarities((UserActionAvro) record.value());
                    if (!snap.isEmpty()) {
                        for (EventSimilarityAvro eventSim:snap) {
                            log.info("Отправляю сообщение с параметрами eventA: {}, eventB: {}, sim: {}",
                                    eventSim.getEventA(),eventSim.getEventB(),eventSim.getScore());
                            producer.send(new ProducerRecord<>("stats.events-similarity.v1",eventSim));
                        }
                        manageOffsets(record, count, consumer);
                        count++;
                    }

                }
                try {
                    consumer.commitAsync();
                    log.debug("Коммит смещений выполнен успешно");
                } catch (Exception e) {
                    log.error("Ошибка при коммите смещений", e);
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                producer.flush();

                consumer.commitAsync();


            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SpecificRecordBase> record, int count,
                                      KafkaEventConsumer consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}



