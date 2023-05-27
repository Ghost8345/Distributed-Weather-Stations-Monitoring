package Bitcask;

import java.io.IOException;
import java.io.RandomAccessFile;

public class EntryPointer {
    private long fileId;
    private long offset;
    private int length;

    public EntryPointer(long fileId, long offset, int size) {
        this.fileId = fileId;
        this.offset = offset;
        this.length = size;
    }

    public EntryPointer(RandomAccessFile in) throws IOException {
        read(in);
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

    public void serialize(RandomAccessFile out) throws IOException {
        out.writeLong(fileId);
        out.writeLong(offset);
        out.writeInt(length);
    }

    private void read(RandomAccessFile in) throws IOException {
        fileId = in.readLong();
        offset = in.readLong();
        length = in.readInt();
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

}
