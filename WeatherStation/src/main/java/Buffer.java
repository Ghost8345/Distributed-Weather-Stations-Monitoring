import Parquet.ParquetHandler;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class Buffer {
    private static final int MAX_LENGTH = 10;
    private ParquetHandler parquetHandler;
    private JSONArray array;

    public Buffer() {
        parquetHandler = new ParquetHandler();
        array = new JSONArray();
    }

    public void add(String jsonString) throws JSONException, IOException {
        if (array.length() == MAX_LENGTH) {
            parquetHandler.write(array, "/parquet");
            array = new JSONArray();
        }

        array.put(new JSONObject(jsonString));
    }
}
