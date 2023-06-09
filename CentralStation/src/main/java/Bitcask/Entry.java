package Bitcask;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Entry {
    private String key;
    private byte[] value;

    public Entry(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public Entry(ByteArrayInputStream in) throws IOException {
        readExternal(in);
    }

    public Entry(RandomAccessFile file) throws IOException {
        readExternal(file);
    }

    public byte[] getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public void setValue(byte[] newValue) {
        value = newValue;
    }

    public int size() { return 2 * Integer.BYTES + key.length() + value.length; }

    public void writeExternal(RandomAccessFile out) throws IOException {
        out.writeInt(key.length());
        out.writeInt(value.length);
        out.write(key.getBytes(StandardCharsets.UTF_8));
        out.write(value);
    }

    private void readExternal(RandomAccessFile file) throws IOException {
        int keySize = file.readInt();
        int valueSize = file.readInt();

        byte[] keyBytes = new byte[keySize];
        file.read(keyBytes);
        key = new String(keyBytes, 0, keySize, StandardCharsets.UTF_8);

        byte[] valueBytes = new byte[valueSize];
        file.read(valueBytes);
        value = valueBytes;
    }

    private void readExternal(ByteArrayInputStream buffer) throws IOException {
        int keySize = readInt(buffer);
        int valueSize = readInt(buffer);

        byte[] keyBytes = buffer.readNBytes(keySize);
        key = new String(keyBytes, 0, keySize, StandardCharsets.UTF_8);
        value = buffer.readNBytes(valueSize);
    }

    private int readInt(ByteArrayInputStream buffer) throws IOException {
        return ByteBuffer.wrap(buffer.readNBytes(Integer.BYTES)).getInt();
    }

}
