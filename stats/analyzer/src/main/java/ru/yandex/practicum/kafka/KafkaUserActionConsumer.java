package ru.yandex.practicum.kafka;

import deserializer.impl.UserActionDeserializer;
import jakarta.annotation.PostConstruct;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class KafkaUserActionConsumer {

    private KafkaConsumer<String, SpecificRecordBase> consumer;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @PostConstruct
    public void init() {
        this.consumer = getConsumer();
    }


    public void subscribe(List<String> topics) {
        consumer.subscribe(topics);
    }

    public ConsumerRecords<String, SpecificRecordBase> poll(Duration duration) {
        return consumer.poll(duration);
    }

    public void commitAsync() {
        consumer.commitAsync();
    }

    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> currentOffsets, OffsetCommitCallback callback) {
        consumer.commitAsync(currentOffsets, callback);
    }


    public void close() {
        consumer.close(Duration.ofSeconds(10));
    }


    private KafkaConsumer<String, SpecificRecordBase> getConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "UserActionsAnalyzer");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new KafkaConsumer<>(properties);
    }
}
