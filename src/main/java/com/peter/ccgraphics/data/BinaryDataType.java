package com.peter.ccgraphics.data;

public abstract class BinaryDataType {

    /**
     * Convert the data to a byte array. Must be decodable with {@link #fromByte} to the same value
     * @return Byte array of the data
     */
    public abstract byte[] toBytes();

    /**
     * Convert a byte array back into a value of this type. Should decode the output of {@link #toBytes}
     * @param bytes Byte array to decode from
     */
    public void fromByte(byte[] bytes) {
        fromByte(bytes, 0);
    }

    /**
     * Convert a byte array back into a value of this type. Should decode the output of {@link #toBytes}
     * @param bytes Byte array to decode from
     * @param start Index to start at in the byte array
     */
    public abstract void fromByte(byte[] bytes, int start);

    /**
     * Get the number of bytes the data would be if encoded
     * @return Number of bytes the data uses
     */
    public abstract int getLength();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    /**
     * Get a hex representation of this data
     * @return Hex string (without leading <code>0x</code>)
     */
    public abstract String hex();

    public static String toHex(long v, int d) {
        return String.format("%0" + d + "X", v);
    } 
}
