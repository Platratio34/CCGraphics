package com.peter.ccgraphics.data;

public class uint15 extends BinaryDataType {

    public int value;

    public uint15() {
        value = 0x0;
    }

    public uint15(int v) {
        value = v;
    }

    @Override
    public byte[] toBytes() {
        return encode(value);
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        value = ((bytes[start] & 0x7f) << 8) + bytes[start+1];
    }
    
    @Override
    public int getLength() {
        return 2;
    }

    public static byte[] encode(int value) {
        return new byte[] { (byte) ( ( value >> 8 ) & 0x7f ), (byte) (value & 0xff) };
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

}
