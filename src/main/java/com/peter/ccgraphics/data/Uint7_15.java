package com.peter.ccgraphics.data;

public class Uint7_15 extends BinaryDataType {

    public static final int MAX_7 = 0x7f;
    public static final int MAX_15 = 0x7fff;

    public static final int MAX = MAX_15;

    protected static final int MASK = 0x80;

    public int value;

    public Uint7_15() {
        value = 0;
    }

    public Uint7_15(int value) {
        this.value = value;
        if (value > MAX_15) {
            throw new IllegalArgumentException("Value of Uint7_15 must be less than or equal to 0x7fff");
        }
    }

    @Override
    public byte[] toBytes() {
        if (value <= MAX_7) {
            return new byte[] { (byte) value };
        }
        return new byte[] {
            (byte) (MASK | (value >> 8)),
            (byte) value
        };
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        if ((bytes[start] & MASK) == MASK) {
            value = ((bytes[start] & 0x7f) << 8) | (bytes[start+1] & 0xff);
        } else {
            value = bytes[start] & MAX_7;
        }
    }

    @Override
    public int getLength() {
        return value <= MAX_7 ? 1 : 2;
    }

    @Override
    public boolean equals(Object obj) {
        switch (obj) {
            case Uint7_15 uint7_15 -> {
                return value == uint7_15.value;
            }
            case Integer integer -> {
                return value == integer;
            }
            default -> {
            }
        }
        return false;
    }

    public static byte[] encode(int value) {
        if (value <= MAX_7) {
            return new byte[] { (byte) value };
        }
        return new byte[] {
            (byte) (MASK | (value >> 8)),
            (byte) value
        };
    }

    public static Uint7_15 from(byte[] bytes, int start) {
        Uint7_15 v = new Uint7_15();
        v.fromByte(bytes, start);
        return v;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String hex() {
        if (value <= MAX_7) {
            return toHex(value, 2);
        }
        return toHex(value, 4);
    }

}
