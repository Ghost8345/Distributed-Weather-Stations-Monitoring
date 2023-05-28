package Parquet;


import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ParquetHandler {
    static int counter = 0;

    public void write(JSONArray weatherEntries, String path) throws IOException, JSONException {
        createFolder(path + "/outputs");
        counter++;

        // Create Station Buffers
        ArrayList<JSONArray> jsonArrayArrayList = new ArrayList<JSONArray>(10);
        for(int i = 1; i < 11; i++){
            createFolder(path + "/outputs/Station_" + i);
            jsonArrayArrayList.add(new JSONArray());
        }

        // Partitioning
        for(int i = 0; i < weatherEntries.length(); i++){
            int k = (int) weatherEntries.getJSONObject(i).get("station_id");
            jsonArrayArrayList.get(k).put(weatherEntries.getJSONObject(i));
        }

        LocalDate current = LocalDate.now();
        Schema schema = new Schema.Parser().parse(new File("avro.avsc"));
        //
        for(int i = 0; i < jsonArrayArrayList.size(); i++) {
            if(jsonArrayArrayList.get(i).length() == 0)
                continue;
            createFolder(path + "/outputs/Station_" + i + "/" + current);
            Path file = new Path(path + "/outputs/Station_" + i + "/" + current + "/output_" + i + "_" + counter + ".parquet" );
            List<GenericData.Record> files = new ArrayList<>();
            for (int k = 0; k < jsonArrayArrayList.get(i).length(); k++) {
                JSONObject jsonObject = jsonArrayArrayList.get(i).getJSONObject(k);
                files.add(createParquet(jsonObject, schema));
            }
                try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
                        .<GenericData.Record>builder(file)
                        .withSchema(schema)
                        .withConf(new Configuration())
                        .withCompressionCodec(CompressionCodecName.SNAPPY)
                        .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                        .build()) {
                    for (GenericData.Record record : files) {
                        writer.write(record);
                    }
                }
        }
    }

    private void createFolder(String path){
        File f = new File(path);
        f.mkdir();
    }

    private GenericData.Record createParquet(JSONObject jsonObject, Schema schema) throws JSONException {
        GenericData.Record temp = new GenericData.Record(schema);

        temp.put("station_id", jsonObject.get("station_id"));
        temp.put("s_no", jsonObject.get("s_no"));
        temp.put("battery_status", jsonObject.get("battery_status"));
        temp.put("status_timestamp", jsonObject.get("status_timestamp"));
        temp.put("humidity", jsonObject.getJSONObject("weather").get("humidity"));
        temp.put("temperature", jsonObject.getJSONObject("weather").get("temperature"));
        temp.put("wind_speed", jsonObject.getJSONObject("weather").get("wind_speed"));

        return  temp;
    }
}
