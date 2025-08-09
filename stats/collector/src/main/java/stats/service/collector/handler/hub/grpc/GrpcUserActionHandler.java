package stats.service.collector.handler.hub.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import stats.service.collector.kafka.KafkaEventProducer;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.grpc.telemetry.user.UserActionProto;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Component
public class GrpcUserActionHandler {

    private String topic = "stats.user-actions.v1";
    private final KafkaEventProducer producer;

    protected UserActionAvro mapToAvro(UserActionProto userActionProto) {
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(ActionTypeAvro.valueOf(userActionProto.getActionType().name().substring(7)))
                .setTimestamp(Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()))
                .build();
    }


    public void handle(UserActionProto userActionProto) {
        UserActionAvro record = mapToAvro(userActionProto);
        String userActionClass = userActionProto.getClass().getSimpleName();
        log.trace("Сохраняю действие пользователя {} связанное с событием {} в топик {}",
                userActionClass, userActionProto.getEventId(), topic);
        producer.send(new ProducerRecord<>(topic, record));


    }
}
