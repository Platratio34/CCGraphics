package com.peter.ccgraphics.data;

public class uint8 extends BinaryDataType {

    public int value;

    public uint8() {
        value = 0x0;
    }
    
    public uint8(int v) {
        value = v;
    }

    public uint8(byte v) {
        value = v;
    }

    @Override
    public byte[] toBytes() {
        return encode(value);
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        value = bytes[start];
    }
    
    @Override
    public int getLength() {
        return 1;
    }

    public static byte[] encode(int value) {
        return new byte[] { (byte) value };
    }

    public static uint8 from(byte[] bytes, int start) {
        uint8 v = new uint8();
        v.fromByte(bytes, start);
        return v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof uint8) {
            return value == ((uint8) obj).value;
        } else if (obj instanceof Integer) {
            return value == (int) obj;
        }
        return false;
    }
}
