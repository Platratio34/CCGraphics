package com.peter.ccgraphics.data;

public class uint32 extends BinaryDataType {

    public long value;

    public uint32() {
        value = 0x0;
    }

    public uint32(long v) {
        value = v;
    }

    @Override
    public byte[] toBytes() {
        return encode(value);
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        value = (bytes[start] << 24) + (bytes[start+1] << 16) + (bytes[start+2] << 8) + bytes[start+3];
    }
    
    @Override
    public int getLength() {
        return 4;
    }

    public static byte[] encode(long value) {
        return new byte[] { (byte) ( ( value >> 24 ) & 0xff ), (byte) ( ( value >> 16 ) & 0xff ), (byte) ( ( value >> 8 ) & 0xff ), (byte) (value & 0xff) };
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
}
