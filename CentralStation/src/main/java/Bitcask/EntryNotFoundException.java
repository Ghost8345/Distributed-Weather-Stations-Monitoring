package Bitcask;

public class EntryNotFoundException extends Exception {
    public EntryNotFoundException(String key) {
        super("An entry with the key `"+ key + "` couldn't be found");
    }
}
