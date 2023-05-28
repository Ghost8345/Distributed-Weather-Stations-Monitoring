package Bitcask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Bitcask {
    private final static String PATH = "storage/bitcask/";
    private final static String HINT_PATH = PATH + "hint";
    private final static int MAX_FILE_BYTES = 1000;
    private final static int COMPACTION_FILE_LIMIT = 3;
    private final Map<String, EntryPointer> map;
    private final ExecutorService compactionThread;
    private RandomAccessFile activeFile = null;
    private long activeFileId = 0;
    private int nonCompactedFiles = 0;


    public Bitcask() throws IOException {
        this.map = new HashMap<>();
        createDirectory();

        File hintFile = new File(HINT_PATH);
        if (hintFile.exists()) {
            recoverFromHint();
            System.out.println("hint file successfully read");
            recoverFromRecentlyActive();
        }
        openNewFile();
        compactionThread = Executors.newSingleThreadExecutor();
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

    private void recoverFromRecentlyActive() throws IOException {
        File directory = new File(PATH);
        List<File> postCompactionFiles = Arrays.stream(Objects.requireNonNull(
                        directory
                .listFiles((file) -> !isHintFile(file))))
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    if (!list.isEmpty()) list.subList(0, 1).clear();
                    return list;
                }));
        for (File file : postCompactionFiles) {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            while (f.getFilePointer() < f.length() - 1) {
                long offset = f.getFilePointer();

                Entry e = new Entry(f);
                String key = e.getKey();

                long id = Long.parseLong(file.getName());
                int size = e.size();

                EntryPointer entryPointer = new EntryPointer(id, offset, size);
                map.put(key,entryPointer);
            }
            f.close();
        }

    }

    public void put(String key, byte[] value) throws IOException {
        Entry entry = new Entry(key, value);
        EntryPointer pointer = write(entry);

        // obtain a lock on the map object
        synchronized (map) {
            map.put(key, pointer);
        }
    }

    public byte[] get(String key) throws  IOException {
        try{
            if (!map.containsKey(key))
                throw new EntryNotFoundException("No items with the given key");

            EntryPointer pointer = map.get(key);
            Entry entry = read(pointer);
            return entry.getValue();
        }catch (EntryNotFoundException e){
            System.out.println(e.getMessage());
        }
        return null;
    }




    private void createDirectory() throws IOException {
        File directory = new File(PATH);
        if (!directory.exists() || !directory.isDirectory()) {
            boolean created = directory.mkdir();
            if (!created)
                throw new IOException("Couldn't create directory for storage.");
        }
    }

    private void openNewFile() throws IOException {
        if (activeFile != null) {
            activeFile.close();
            nonCompactedFiles++;
        }

        long compactionFileId = 0;
        if (nonCompactedFiles >= COMPACTION_FILE_LIMIT) {
            compactionFileId = System.currentTimeMillis();
            compact(compactionFileId);
            nonCompactedFiles = 0;
        }

        activeFileId = System.currentTimeMillis(); // generate unique id
        if (activeFileId == compactionFileId)
            activeFileId++;
        activeFile = new RandomAccessFile(getFilePath(activeFileId), "rwd");
        System.out.println(activeFileId + " created");
    }

    private String getFilePath(long fileId) {
        return PATH + fileId;
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

    private void compact(long compactionFileId) {
        System.out.println("Compaction Thread Up");
        compactionThread.execute(() -> {
            try {
                long activeFileId = this.activeFileId;
                File directory = new File(PATH);
                List<File> immutableFiles = Arrays.stream(Objects.requireNonNull(
                    directory
                        .listFiles((file) -> !isActiveFile(file, activeFileId) && !isHintFile(file))))
                        .sorted(Comparator.comparing(File::getName))
                        .collect(Collectors.toList());

                HashMap<String, CompactValueNode> compactData = new HashMap<>();
                for (File file : immutableFiles) {
                    RandomAccessFile f = new RandomAccessFile(file, "rwd");
                    while (f.getFilePointer() < f.length() - 1) {
                        long offset = f.getFilePointer();
                        long id = Long.parseLong(file.getName());
                        Entry e = new Entry(f);
                        EntryPointer ep = new EntryPointer(id, offset, e.size());
                        compactData.put(e.getKey(), new CompactValueNode(e, ep));
                    }
                    f.close();
                }

                // Write compacted file
                writeCompactionData(compactData, compactionFileId);

                // Delete garbage files
                immutableFiles.forEach(file -> {
                    boolean deleted = file.delete();
                    if (!deleted)
                        try {
                            throw new GarbageFileNotDeleted(file.getName());
                        } catch (GarbageFileNotDeleted e) {
                            e.printStackTrace();
                        }
                });

                System.out.println("Compaction done and hint file generated");

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isHintFile(File file) {
        return file.getName().equals("hint");
    }

    private boolean isActiveFile(File file, long activeFileId) {
        return file.getName().equals(String.valueOf(activeFileId));
    }

    private void writeCompactionData(HashMap<String, CompactValueNode> compactData, long compactionFileId) throws IOException {
        String compactFilePath = getFilePath(compactionFileId);
        RandomAccessFile compactFile = new RandomAccessFile(compactFilePath, "rwd");


        RandomAccessFile hintFile = new RandomAccessFile(HINT_PATH, "rwd");
        //Clear hint file
        hintFile.setLength(0);
        for (var compactPair : compactData.entrySet()) {
            String key = compactPair.getKey();
            Entry entry = compactPair.getValue().getEntry();

            long offset = compactFile.getFilePointer();
            int size = entry.size();

            EntryPointer nonCompactedPointer = compactPair.getValue().getEntryPointer();
            EntryPointer compactedPointer = new EntryPointer(compactionFileId, offset, size);

            entry.writeExternal(compactFile);
            writeHintEntry(key, compactedPointer, hintFile);

            synchronized (map) {
                /*  only update the map with new pointer if
                    the old pointer hasn't been modified   */
                if (map.get(key).equals(nonCompactedPointer))
                    map.put(key, compactedPointer);
            }
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