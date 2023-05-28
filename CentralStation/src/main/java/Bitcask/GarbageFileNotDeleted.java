package Bitcask;

public class GarbageFileNotDeleted extends Exception {
    public GarbageFileNotDeleted(String fileId) {
        super("File " + fileId + "couldn't be deleted");
    }
}
