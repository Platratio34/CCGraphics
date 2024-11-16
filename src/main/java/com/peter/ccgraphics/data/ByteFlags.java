package com.peter.ccgraphics.data;

public class ByteFlags extends BinaryDataType {

    public boolean[] flags = new boolean[8];

    @Override
    public byte[] toBytes() {
        int b = 0x00;
        for (int i = 0; i < 8; i++) {
            b = b << 1;
            if (flags[i])
                b |= 0b1;
        }
        return new byte[] { (byte) b };
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        int m = 0b10000000;
        for (int i = 0; i < 8; i++) {
            flags[i] = (bytes[start] & m) == m;
            m = m >> 1;
        }
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof ByteFlags))
            return false;
        ByteFlags other = (ByteFlags)obj;
        for (int i = 0; i < 8; i++) {
            if (flags[i] != other.flags[i])
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        int m = 0b1;
        for (int i = 0; i < 8; i++) {
            if (flags[i])
                hash |= m;
            m = m << 1;
        }
        return hash;
    }

}
