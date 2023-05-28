package Bitcask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Storage {
    private final static int MAX_FILE_BYTES = 300;
    private final static int COMPACTION_FILE_LIMIT = 2;
    private final static int COMPACT_FILE_ID = 0;
    private RandomAccessFile activeFile = null;
    private long activeFileId;
    private int nonCompactedFiles = 0;
    private final ExecutorService compactionThread;

    public Storage() throws IOException {
        openNewFile();
        compactionThread = Executors.newSingleThreadExecutor();
    }

    public Storage(long fileId) throws IOException {
        this.activeFileId = fileId;
        openNewFile();
        compactionThread = Executors.newSingleThreadExecutor();
    }

    private void openNewFile() throws IOException {
        if (activeFile != null) {
            nonCompactedFiles++;
            activeFile.close();
        }

        if (nonCompactedFiles >= COMPACTION_FILE_LIMIT)
            compact();

        activeFileId = System.currentTimeMillis(); // generate unique id
        String filePath = getFilePath(activeFileId);
        activeFile = new RandomAccessFile(filePath, "rwd");

    }

    private String getFilePath(long fileId) {
        return "storage/" + fileId;
    }

    private String getFilePath(String fileName) {
        return "storage/" + fileName;
    }

    public EntryPointer write(Entry entry) throws IOException {
        if (activeFile.getFilePointer() + entry.size() > MAX_FILE_BYTES)
            openNewFile();

        long offset = activeFile.getFilePointer();
        entry.writeExternal(activeFile);
        int length = (int) (activeFile.getFilePointer() - offset);
        return new EntryPointer(activeFileId, offset, length);
    }

    public Entry read(EntryPointer entryPointer) throws IOException {
        String filePath = getFilePath(entryPointer.getFileId());
        int length = entryPointer.getLength();
        int offset = (int) entryPointer.getOffset();

        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        byte[] buffer = new byte[length];
        file.seek(offset);
        file.read(buffer);
        file.close();

        ByteArrayInputStream bytesStream = new ByteArrayInputStream(buffer);
        return new Entry(bytesStream);
    }

    private void compact() {
        compactionThread.execute(() -> {
            try {
                long filesCount = activeFileId;
                HashMap<String, Entry> compactData = new HashMap<>();
                File compactFile = new File(getFilePath(COMPACT_FILE_ID));
                int currentId = compactFile.exists() ? 0 : 1;
                while (currentId <= filesCount) {
                    String filePath = getFilePath(currentId++);
                    RandomAccessFile file = new RandomAccessFile(filePath, "r");
                    System.out.println(filePath);

                    while (file.getFilePointer() < file.length()) {
                        Entry entry = new Entry(file);
                        compactData.put(entry.getKey(), entry);
                    }
                }
                writeCompactionData(compactData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private void writeCompactionData(HashMap<String,Entry> compactData) throws IOException {
        String compactFilePath = getFilePath(COMPACT_FILE_ID);
        RandomAccessFile compactFile = new RandomAccessFile(compactFilePath, "rwd");

        String hintFilePath = getFilePath("hint");
        RandomAccessFile hintFile = new RandomAccessFile(hintFilePath, "rwd");

        for (Map.Entry<String, Entry> compactPair : compactData.entrySet()) {
            String key = compactPair.getKey();
            Entry entry = compactPair.getValue();

            long offset = compactFile.getFilePointer();
            int size = entry.size();
            EntryPointer entryPointer = new EntryPointer(COMPACT_FILE_ID, offset, size);

            entry.writeExternal(compactFile);
            writeHintEntry(key,entryPointer,hintFile);
        }

    }

    private void writeHintEntry(String key,
                                EntryPointer entryPointer,
                                RandomAccessFile hintFile) throws IOException {
        int keySize = key.length();
        hintFile.writeInt(keySize);
        hintFile.write(key.getBytes(StandardCharsets.UTF_8));
        entryPointer.serialize(hintFile);
    }

}