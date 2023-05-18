import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.kafka.common.serialization.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class JsonSerializer implements Serializer {

    private static final Logger logger = (Logger) LogManager.getLogger(JsonSerializer.class);

    @Override
    public void configure(Map map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, Object o) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsBytes(o);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return retVal;
    }

    @Override
    public void close() {

    }
}