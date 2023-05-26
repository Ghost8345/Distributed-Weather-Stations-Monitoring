package Bitcask;

public class EntryPointer {
    private final long fileId;
    private final long offset;
    private final int length;

    public EntryPointer(long fileId, long offset, int size) {
        this.fileId = fileId;
        this.offset = offset;
        this.length = size;
    }

    @Override
    public String toString() {
        return "EntryPointer{" +
                "fileId=" + fileId +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }

    public long getFileId() {
        return fileId;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
}
