import Models.Station;
import Models.Weather;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;

public class MessageFactory {
    private Long messageCount = 0L;

    public Optional<Station> createMessage(Long stationId){
        this.messageCount++;
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

        Optional<Weather> weather = getWeatherDataFromAPI();

        Station station = new Station(stationId, messageCount, batteryStatus, System.currentTimeMillis(), weather.orElse(new Weather(35, 100, 13)));
        return Optional.of(station);
    }

    private Optional<Weather> getWeatherDataFromAPI(){
        try{
            URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=31.20&longitude=29.92&temperature_unit=fahrenheit&current_weather=true&hourly=relativehumidity_2m");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responsecode = conn.getResponseCode();

            if (responsecode != 200) {
                return Optional.empty();
            }
            else {
                StringBuilder inline = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }

                //Close the scanner
                scanner.close();
                JSONParser parse = new JSONParser();
                JSONObject res = (JSONObject) parse.parse(inline.toString());
                JSONObject weatherData = (JSONObject)res.get("current_weather");
                int temperature = (int) Math.round( (double) weatherData.get("temperature") );
                int windSpeed = (int) Math.round( (double) weatherData.get("windspeed") );
                JSONObject hourlyData = (JSONObject)res.get("hourly");
                JSONArray humidityArray = (JSONArray) hourlyData.get("relativehumidity_2m");
                Long averageHumidity = 0L;
                for (int i = 0; i < humidityArray.size(); i++) {
                    averageHumidity += (Long) humidityArray.get(i);
                }
                averageHumidity /= humidityArray.size();
                return Optional.of(new Weather(averageHumidity.intValue(), temperature, windSpeed));
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return Optional.empty();
        }

    }

}
