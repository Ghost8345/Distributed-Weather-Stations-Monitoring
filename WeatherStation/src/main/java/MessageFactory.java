import Models.Station;
import Models.Weather;

import java.util.Optional;

public class MessageFactory {
    private Long messageCount = 1L;

    public Optional<Station> createMessage(Long stationId){
        double dropMessageNumber = Math.random();
        boolean dropped = dropMessageNumber < 0.1;
        if (dropped)
            return Optional.empty();
        double batteryStatusNumber = Math.random();
        String batteryStatus;
        if (batteryStatusNumber < 0.3)
            batteryStatus = "low";
        else if (batteryStatusNumber < 0.7) {
            batteryStatus = "medium";
        }
        else
            batteryStatus = "high";

        Station station = new Station(stationId, messageCount, batteryStatus, System.currentTimeMillis(), new Weather(35, 100, 13));
        this.messageCount++;
        return Optional.of(station);
    }
}
