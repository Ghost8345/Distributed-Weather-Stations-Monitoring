package Bitcask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Bitcask {
    private final Map<String,EntryPointer> map;
    private final Storage storage;

    public Bitcask() throws IOException {
        this.map = new HashMap<>();
        // TODO: handle recovery

        // create a directory for storage if it doesn't exist
        File directory = new File("storage");
        if (!directory.exists() || !directory.isDirectory()) {
            boolean created = directory.mkdir();
            if (!created)
                throw new IOException("Couldn't create directory for storage.");
        }
        this.storage = new Storage();
    }

    public void put(String key, byte[] value) throws IOException {
        Entry entry = new Entry(key, value);
        EntryPointer pointer = storage.write(entry);
        map.put(key, pointer);
    }

    public byte[] get(String key) throws EntryNotFoundException, IOException {
        if (!map.containsKey(key))
            throw new EntryNotFoundException("No items with the given key");

        EntryPointer pointer = map.get(key);
        Entry entry = storage.read(pointer);
        return entry.getValue();
    }


}
