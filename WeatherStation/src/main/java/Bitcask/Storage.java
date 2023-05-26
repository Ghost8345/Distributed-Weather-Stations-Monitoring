package Bitcask;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Storage {
    private final static int MAX_FILE_BYTES = 50;
    private final static int COMPACTION_FILE_LIMIT = 2;
    private final static int COMPACT_FILE_ID = -1;
    private RandomAccessFile activeFile = null;
    private long fileId = 0;

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
        HashMap<String,EntryPointer> compactBlock = new HashMap<>();
        for ( int currentId=1 ; currentId<=filesCount ; currentId++){
            String filePath = getFilePath(currentId);
            RandomAccessFile file = new RandomAccessFile(filePath, "r");
            while (file.getFilePointer()<file.length()) {
                long offset = file.getFilePointer();
                Entry e = new Entry(file);
                EntryPointer ep = new EntryPointer(COMPACT_FILE_ID,offset,e.size());
                if (compactBlock.containsKey(e.getKey())){
                    compactBlock.replace(e.getKey(),ep);
                }else{
                    compactBlock.put(e.getKey(),ep);
                }
                file.seek(file.getFilePointer() + e.size());
            }
        }
    }
}
