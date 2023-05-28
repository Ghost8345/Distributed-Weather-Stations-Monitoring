import Bitcask.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class CentralStation {
    public static void main(String[] args) throws JSONException, IOException {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "central-station");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongDeserializer");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList("station-health-check"));

        Buffer buffer = new Buffer();
        Bitcask bitcask = new Bitcask();

        try {
            while (true) {
                ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<Long, String> record : records) {
                    Long key = record.key();
                    String value = record.value();
                    System.out.println("Received message: Key = " + key + ", Value = " + value);
//                    buffer.add(value);
                    bitcask.put(key.toString(), value.getBytes(StandardCharsets.UTF_8));
                    String message = new String(bitcask.get(key.toString()));
                    System.out.println("Bitcask: " + message);
                }
            }
        } catch (EntryNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            consumer.close();
        }
    }
}
