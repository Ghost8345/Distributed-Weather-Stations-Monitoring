package Bitcask;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final static int MAX_FILE_BYTES = 50;
    private final static int COMPACTION_FILE_LIMIT = 2;
    private final static int COMPACT_FILE_ID = 0;
    private RandomAccessFile activeFile = null;
    private long fileId = 0;
    private boolean hasCompactedBefore = false;

    public Storage() throws IOException {
        openNewFile();
    }

    public Storage(long fileId) throws IOException {
        this.fileId = fileId;
        openNewFile();
    }

    private void openNewFile() throws IOException {
        if (activeFile != null)
            activeFile.close();
        if (fileId>=COMPACTION_FILE_LIMIT)
            compact();
        String filePath = getFilePath(++fileId);
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
        return new EntryPointer(fileId, offset, length);
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

    private void compact() throws IOException {
        long filesCount = fileId;
        HashMap<String,CompactValueNode> compactData = new HashMap<>();
        int currentId = hasCompactedBefore ? 0 : 1;
        while (currentId<=filesCount){
            String filePath = getFilePath(currentId++);
            RandomAccessFile file = new RandomAccessFile(filePath, "r");
            while (file.getFilePointer()<file.length()) {
                long offset = file.getFilePointer();
                Entry e = new Entry(file);
                EntryPointer ep = new EntryPointer(COMPACT_FILE_ID,offset,e.size());
                if (compactData.containsKey(e.getKey())){
                    compactData.replace(e.getKey(),new CompactValueNode(e,ep));
                }else{
                    compactData.put(e.getKey(),new CompactValueNode(e,ep));
                }
            }
        }
        writeCompactionData(compactData);
    }

    private void writeCompactionData(HashMap<String,CompactValueNode> compactData) throws FileNotFoundException {
        String compactFilePath = getFilePath(COMPACT_FILE_ID);
        RandomAccessFile compactFile = new RandomAccessFile(compactFilePath, "rwd");

        String hintFilePath = getFilePath("hint");
        RandomAccessFile hintFile = new RandomAccessFile(hintFilePath, "rwd");

        for (Map.Entry<String, CompactValueNode> compactPair : compactData.entrySet()) {
            String key = compactPair.getKey();
            CompactValueNode value = compactPair.getValue();
            writeHintEntry(key,value.getEntryPointer(),hintFile);
            writeCompactEntry(key,value.getEntry(),compactFile);
        }

    }

    private void writeHintEntry(String key, EntryPointer entryPointer, RandomAccessFile hintFile) {
    }

    private void writeCompactEntry(String key, Entry entry, RandomAccessFile compactFile) {
    }


}
