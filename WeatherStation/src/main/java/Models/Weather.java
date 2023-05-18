package Models;

public class Weather {
    private final int humidity;
    private final int temperature;

    private final int wind_speed;

    public Weather(int humidity, int temperature, int windSpeed) {
        this.humidity = humidity;
        this.temperature = temperature;
        wind_speed = windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getWind_speed() {
        return wind_speed;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "humidity=" + humidity +
                ", temperature=" + temperature +
                ", wind_speed=" + wind_speed +
                '}';
    }
}
