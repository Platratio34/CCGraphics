package com.peter.ccgraphics.data;

public class Utf8String extends BinaryDataType {

    protected int length;
    protected String string;
    public boolean nullTerminated = false;

    public Utf8String() {
        length = -1;
        string = "";
        nullTerminated = true;
    }

    public Utf8String(int length) {
        this.length = length;
        string = "";
        if (length < 0) {
            length = -1;
            nullTerminated = true;
        }
    }

    public Utf8String(String string) {
        this(string, false);
    }

    public Utf8String(String string, boolean nullTerminated) {
        length = string.length();
        this.string = string;
        this.nullTerminated = nullTerminated;
        if (nullTerminated)
            length++;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c > 127) {
                throw new IllegalArgumentException("Utf8 String can not contain non-ASCII characters");
            }
        }
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[length];
        for (int i = 0; i < string.length(); i++) {
            bytes[i] = (byte) string.charAt(i);
        }
        if (nullTerminated)
            bytes[bytes.length - 1] = 0x00;
        return bytes;
    }

    @Override
    public void fromByte(byte[] bytes, int start) {
        string = "";
        if (length < 0) {
            byte b = bytes[start];
            int i = 1;
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
            return 0;
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
            if (i > 0)
                hex += "_";
            hex += toHex(string.charAt(i), 2);
        }
        if (nullTerminated) {
            if (string.length() > 0)
                hex += "_";
            hex += "00";
        }
        return hex;
    }

    public String getString() {
        return string;
    }

}
