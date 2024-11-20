package com.peter.ccgraphics.data;

public class uint32 extends BinaryDataType {

    public static final int MAX = 0xffffffff;

    public long value;

    public uint32() {
        value = 0x0;
    }

    public uint32(long v) {
        if (v < 0)
            throw new IllegalArgumentException("Value of uint32 must be positive");
        if (v > MAX)
            throw new IllegalArgumentException("Value of uint32 must be less than or equal to 0xffffffff");
        value = v;
    }

    @Override
    public byte[] toBytes() {
        return encode(value);
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        value = ((bytes[start] & 0xff) << 24) | ((bytes[start + 1] & 0xff) << 16) | ((bytes[start + 2] & 0xff) << 8) | (bytes[start + 3] & 0xff);
    }
    
    @Override
    public int getLength() {
        return 4;
    }

    public static byte[] encode(long value) {
        return new byte[] {
                (byte) ((value >> 24)),
                (byte) ((value >> 16)),
                (byte) ((value >> 8)),
                (byte) (value)
        };
    }

    public static uint32 from(byte[] bytes, int start) {
        uint32 v = new uint32();
        v.fromByte(bytes, start);
        return v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof uint32) {
            return value == ((uint32) obj).value;
        } else if (obj instanceof Integer) {
            return value == (int) obj;
        } else if (obj instanceof Long) {
            return value == (long) obj;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int)value;
    }

    @Override
    public String hex() {
        return toHex(value, 8);
    }
}
