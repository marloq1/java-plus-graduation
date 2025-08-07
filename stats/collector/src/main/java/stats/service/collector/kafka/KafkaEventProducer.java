package stats.service.collector.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import serializer.AvroSerializer;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Future;


@Component
public class KafkaEventProducer implements AutoCloseable {

    private KafkaProducer<String, SpecificRecordBase> producer;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @PostConstruct
    public void init() {
        this.producer = createProducer();
    }

    public KafkaProducer<String, SpecificRecordBase> createProducer() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    public Future<RecordMetadata> send(ProducerRecord<String, SpecificRecordBase> record) {
        Future<RecordMetadata> futureResult = producer.send(record);
        producer.flush();
        return futureResult;
    }

    @PreDestroy
    public void shutdown() {
        try {
            close(); // вызовет flush + close с таймаутом
        } catch (Exception e) {
            // логируем, но не бросаем дальше
            System.err.println("Ошибка при закрытии KafkaProducer: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}
