package com.peter.ccgraphics.data;

public class uint16 extends BinaryDataType {

    public static final int MAX = 0xffff;

    public int value;

    public uint16() {
        value = 0x0;
    }

    public uint16(int v) {
        if (v < 0)
            throw new IllegalArgumentException("Value of uint16 must be positive");
        if (v > MAX)
            throw new IllegalArgumentException("Value of uint16 must be less than or equal to 0xffff");
        value = v;
    }

    @Override
    public byte[] toBytes() {
        return encode(value);
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        value = ((bytes[start] & 0xff) << 8) | (bytes[start+1] & 0xff);
    }
    
    @Override
    public int getLength() {
        return 2;
    }

    public static byte[] encode(int value) {
        return new byte[] { (byte) ( value >> 8 ), (byte) (value) };
    }

    public static uint16 from(byte[] bytes, int start) {
        uint16 v = new uint16();
        v.fromByte(bytes, start);
        return v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof uint16) {
            return value == ((uint16) obj).value;
        } else if (obj instanceof Integer) {
            return value == (int) obj;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String hex() {
        return toHex(value, 4);
    }
}
