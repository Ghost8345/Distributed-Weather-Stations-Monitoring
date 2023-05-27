package Bitcask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Bitcask {
    private final Map<String,EntryPointer> map;
    private final Storage storage;
    private final static String HINT_PATH = "storage/hint";

    public Bitcask() throws IOException {
        this.map = new HashMap<>();

        File hintFile = new File(HINT_PATH);
        if (hintFile.exists())
            recoverFromHint();

        File directory = new File("storage");
        if (!directory.exists() || !directory.isDirectory()) {
            boolean created = directory.mkdir();
            if (!created)
                throw new IOException("Couldn't create directory for storage.");
        }
        this.storage = new Storage();
    }

    private void recoverFromHint() throws IOException {
        RandomAccessFile hintFile = new RandomAccessFile(HINT_PATH,"r");
        while (hintFile.getFilePointer() < hintFile.length()){
            int keySize = hintFile.readInt();
            byte[] keyBytes = new byte[keySize];
            hintFile.read(keyBytes);
            String key = new String(keyBytes, 0, keySize, StandardCharsets.UTF_8);
            EntryPointer entryPointer = new EntryPointer(hintFile);
            map.put(key,entryPointer);
        }
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
