package Processors;

import Json.CustomSerdes;
import Models.Station;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;
import java.util.Properties;

public class HumidityProcessor {
    private static final String INPUT_TOPIC = "station-health-check";
    private static final String OUTPUT_TOPIC = "high-humidity";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.putIfAbsent(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);

        final StreamsBuilder builder = new StreamsBuilder();
        KStream<Long, Station> source = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.Long(), CustomSerdes.Station()));

        Predicate<Long, Station> humidityPredicate =
                (key, station) -> station.getWeather().getHumidity() > 70;

        KStream<Long, Station> filteredStream =
                source.filter(humidityPredicate);

        filteredStream.to(OUTPUT_TOPIC);

        // Start the stream
        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, props);
        System.out.println("Starting");
        streams.start();

        // Close the stream when the thread closes
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
