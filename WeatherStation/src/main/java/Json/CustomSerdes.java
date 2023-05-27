package Json;

import Models.Station;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public final class CustomSerdes {

    private CustomSerdes() {}

    public static Serde<Station> Station() {
        JsonSerializer<Station> serializer = new JsonSerializer<>();
        JsonDeserializer<Station> deserializer = new JsonDeserializer<>(Station.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}