package Bitcask;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

public class EntryPointer {
    private long fileId;
    private long offset;
    private int length;

    public EntryPointer(long fileId, long offset, int length) {
        this.fileId = fileId;
        this.offset = offset;
        this.length = length;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryPointer that = (EntryPointer) o;
        return fileId == that.fileId && offset == that.offset && length == that.length;
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

}
