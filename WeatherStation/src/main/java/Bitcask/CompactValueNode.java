package Bitcask;

public class CompactValueNode {
    private final Entry entry;
    private final EntryPointer entryPointer;

    public Entry getEntry() {
        return entry;
    }

    public EntryPointer getEntryPointer() {
        return entryPointer;
    }

    public CompactValueNode(Entry entry, EntryPointer entryPointer) {
        this.entry = entry;
        this.entryPointer = entryPointer;
    }
}
