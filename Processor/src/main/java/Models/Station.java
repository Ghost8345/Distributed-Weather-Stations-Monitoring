package Models;

public class Station {
    private final Long station_id;
    private final Long s_no;
    private final String battery_status;
    private final Long status_timestamp;
    private final Weather weather;

    public Station(Long stationId, Long sNo, String batteryStatus, Long statusTimestamp, Weather weather) {
        station_id = stationId;
        s_no = sNo;
        battery_status = batteryStatus;
        status_timestamp = statusTimestamp;
        this.weather = weather;
    }

    public Long getStation_id() {
        return station_id;
    }

    public Long getS_no() {
        return s_no;
    }

    public String getBattery_status() {
        return battery_status;
    }

    public Long getStatus_timestamp() {
        return status_timestamp;
    }

    public Weather getWeather() {
        return weather;
    }

    @Override
    public String toString() {
        return "Station{" +
                "station_id=" + station_id +
                ", s_no=" + s_no +
                ", battery_status='" + battery_status + '\'' +
                ", status_timestamp=" + status_timestamp +
                ", weather=" + weather +
                '}';
    }
}
