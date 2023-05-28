import Models.Station;
import org.apache.kafka.clients.producer.*;

import java.util.Optional;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Producer {
    //Initializing logger
    private static final Logger logger = LogManager.getLogger(Producer.class);


    public static void main(String[] args) throws InterruptedException {
        long stationId = Long.parseLong(args[0]);

        // Setting Kafka Producer Properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-service:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "Json.JsonSerializer");

        // Create the Kafka producer
        KafkaProducer<Long, Station> producer = new KafkaProducer<>(properties);

        MessageFactory messageFactory = new MessageFactory();

        // Send a message every second
        while (true) {

            // Create new message which can be dropped
            Optional<Station> message = messageFactory.createMessage(stationId);
            if (message.isPresent()){
                // Create a ProducerRecord
                ProducerRecord<Long, Station> record = new ProducerRecord<>("station-health-check",stationId, message.get());

                // Send the record
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.info("Error sending producer");
                    } else {
                        logger.info("Record sent successfully. \n "+ "Topic : "+metadata.topic() +"\n"+
                                "Offset : " +metadata.offset() +"\n"+
                                "Timestamp: " +metadata.timestamp() +"\n");
                    }
                });
            }
            else
                logger.info("Message was dropped");

            // Delay for 1 second
            Thread.sleep(1000);
        }

    }
}
