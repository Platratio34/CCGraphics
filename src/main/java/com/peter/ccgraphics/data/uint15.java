package com.peter.ccgraphics.data;

public class uint15 extends BinaryDataType {

    public static final int MAX = 0x7fff;

    public int value;

    public uint15() {
        value = 0x0;
    }

    public uint15(int v) {
        if (v < 0)
            throw new IllegalArgumentException("Value of uint15 must be positive");
        if (v > MAX)
            throw new IllegalArgumentException("Value of uint15 must be less than or equal to 0x7fff");
        value = v;
    }

    @Override
    public byte[] toBytes() {
        return encode(value);
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        value = ((bytes[start] & 0x7f) << 8) | (bytes[start+1] & 0xff);
    }
    
    @Override
    public int getLength() {
        return 2;
    }

    public static byte[] encode(int value) {
        return new byte[] { (byte) ( ( (value & 0x7fff) >> 8 )  ), (byte) value };
    }

    public static uint15 from(byte[] bytes, int start) {
        uint15 v = new uint15();
        v.fromByte(bytes, start);
        return v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof uint15) {
            return value == ((uint15) obj).value;
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
