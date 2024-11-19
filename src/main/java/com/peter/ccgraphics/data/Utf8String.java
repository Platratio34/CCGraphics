package com.peter.ccgraphics.data;

public class Utf8String extends BinaryDataType {

    protected int length;
    protected String string;

    public Utf8String(int length) {
        this.length = length;
        string = "";
    }

    public Utf8String(String string) {
        length = string.length();
        this.string = string;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[string.length()];
        for (int i = 0; i < string.length(); i++) {
            bytes[i] = (byte) string.charAt(i);
        }
        return bytes;
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        string = "";
        if (length < 0) {
            byte b = bytes[start];
            int i = 0;
            while (b != 0x00) {
                string += (char) b;
                b = bytes[start + i];
                i++;
            }
            length = i;
        } else {
            for (int i = 0; i < length; i++) {
                string += (char) bytes[start + i];
            }
        }
    }

    @Override
    public int getLength() {
        if (length < 0)
            throw new RuntimeException("Length of string was not defined");
        return length;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Utf8String)) {
            return false;
        }
        Utf8String other = (Utf8String)obj;
        return other.length == length && other.string.equals(string);
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public String hex() {
        String hex = "";
        for (int i = 0; i < string.length(); i++) {
            hex += Integer.toHexString(string.charAt(i));
        }
        return hex;
    }

    public String getString() {
        return string;
    }

}
