package com.peter.ccgraphics.data;

public abstract class BinaryDataType {

    public abstract byte[] toBytes();

    public void fromByte(byte[] bytes) {
        fromByte(bytes, 0);
    }

    public abstract void fromByte(byte[] bytes, int start);

    public abstract int getLength();

    @Override
    public abstract boolean equals(Object obj);
}
