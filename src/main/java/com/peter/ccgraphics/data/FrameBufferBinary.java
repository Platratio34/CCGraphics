package com.peter.ccgraphics.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import com.peter.ccgraphics.ColorHelper;
import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;

public class FrameBufferBinary {

    protected static final uint32 FBB_TYPE_STRING = new uint32(0x66626220);
    protected static final uint16 HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE = new uint16(0x0001);
    protected static final uint16 HEADER_TABLE_ENTRY_LAST = new uint16(0x00);

    public class Encoder {

        protected final FrameBuffer frameBuffer;
        protected final ArrayList<Byte> binary = new ArrayList<Byte>();

        protected boolean rle8 = false;
        protected boolean rle16 = false;
        protected boolean indexed = false;
        protected boolean noAlpha = false;

        protected final HashMap<Integer, uint8> colorIndex = new HashMap<Integer, uint8>();

        public Encoder(FrameBuffer frameBuffer) {
            this.frameBuffer = frameBuffer;
        }

        public void setRle8(boolean v) {
            rle8 = v;
            if (v)
                rle16 = false;
        }

        public void setRle16(boolean v) {
            rle16 = v;
            if (v)
                rle8 = false;
        }

        public void setNoAlpha(boolean v) {
            if (v && indexed)
                throw new RuntimeException("Encoding options `noAlpha` and `indexed` are incompatible");
            noAlpha = v;
        }

        public void setIndexed(boolean v) {
            if (v && noAlpha)
                throw new RuntimeException("Encoding options `noAlpha` and `indexed` are incompatible");
            noAlpha = v;
        }

        public void indexColor(int index, int color) {
            colorIndex.put(color & 0x00ffffff, new uint8(index));
        }

        protected void write(byte b) {
            binary.add(b);
        }

        protected void write(int b) {
            binary.add((byte) b);
        }

        protected void write(byte[] bytes) {
            for (int i = 0; i < bytes.length; i++) {
                write(bytes[i]);
            }
        }

        protected void write(BinaryDataType data) {
            write(data.toBytes());
        }

        protected void write(byte b, int index) {
            binary.set(index, b);
        }

        protected void write(byte[] bytes, int index) {
            for (int i = 0; i < bytes.length; i++) {
                write(bytes[i], index + i);
            }
        }

        protected void write(BinaryDataType data, int index) {
            write(data.toBytes(), index);
        }

        protected void writePixel(int color) {
            if (noAlpha) {
                write(new byte[] { (byte) ((color & 0xff0000) << 16), (byte) ((color & 0x00ff00) << 8),
                        (byte) (color & 0x0000ff) });
            } else if (indexed) {
                write(colorIndex.get(color));
            } else {
                write(uint32.encode(color));
            }
        }

        public byte[] encode() {
            binary.clear();

            write(FBB_TYPE_STRING);

            write(new byte[4]); // leaving space for pointer to data section

            write(uint16.encode(frameBuffer.getWidth()));
            write(uint16.encode(frameBuffer.getHeight()));

            ByteFlags headerFlags = new ByteFlags();
            headerFlags.flags[0] = rle8;
            headerFlags.flags[1] = rle16;
            headerFlags.flags[2] = noAlpha;
            headerFlags.flags[3] = indexed;
            write(headerFlags);

            write(new byte[3]); // padding

            // Start of header tables
            if (indexed) {
                // throw new RuntimeException("Indexed color is currently un-implemented");
                int startPos = binary.size();
                write(HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE);
                write(new byte[2]); // leaving space for entry length;

                for (Entry<Integer, uint8> entry : colorIndex.entrySet()) {
                    write(entry.getValue());
                    int color = entry.getKey();
                    write((color & 0xff0000) >> 16);
                    write((color & 0x00ff00) >> 8);
                    write((color & 0x0000ff));
                }
                int length = binary.size() - startPos;
                write(uint16.encode(length), startPos + 2); // write the entry length
            }

            // start of data section
            write(uint32.encode(binary.size()), 0x4); // write data section pointer in header

            int dataStart = binary.size();
            write(new byte[4]); // leaving space for data length value;

            long lastPixel = 0x100000000l;
            int length = 0;
            for (int i = 0; i < frameBuffer.length(); i++) {
                int color = frameBuffer.getColorIndexed(i);
                if (noAlpha)
                    color &= 0xffffff;

                if (rle8 || rle16) {
                    if (i == 0) {
                        lastPixel = color;
                        continue;
                    }

                    boolean write = lastPixel != color;
                    write = write || (rle8 && (length == 0xff));
                    write = write || (rle16 && (length == 0xffff));

                    if (write) {
                        if (rle8)
                            write(uint8.encode(length));
                        if (rle16)
                            write(uint16.encode(length));

                        writePixel(color);

                        length = -1; // set to `-1` to not count first instance of a color
                        lastPixel = color;
                    } else {
                        length++;
                    }
                } else {
                    writePixel(color);
                }
            }

            int dataLength = binary.size() - dataStart;
            write(uint32.encode(dataLength), dataStart); // write the data section length

            return ArrayUtils.toPrimitive(binary.toArray(new Byte[0]));
        }
    }
    
    public class Decoder {

        protected FrameBuffer frameBuffer;
        protected byte[] binary;
        protected int pointer;

        protected boolean rle8 = false;
        protected boolean rle16 = false;
        protected boolean indexed = false;
        protected boolean noAlpha = false;

        protected final HashMap<uint8, Integer> colorIndex = new HashMap<uint8, Integer>();

        public Decoder() {
        };

        protected uint16 readUint16(int start) {
            return read(start, new uint16());
        }

        protected uint16 readUint16() {
            return read(new uint16());
        }

        protected uint32 readUint32(int start) {
            return read(start, new uint32());
        }

        protected uint32 readUint32() {
            return read(new uint32());
        }

        protected uint8 readUint8(int start) {
            return read(start, new uint8());
        }

        protected uint8 readUint8() {
            return read(new uint8());
        }
        

        protected <T extends BinaryDataType> T read(int start, T type) {
            type.fromByte(binary, start);
            return type;
        }

        protected <T extends BinaryDataType> T read(T type) {
            type.fromByte(binary, pointer);
            pointer += type.getLength();
            return type;
        }

        protected int readPixel(int start) {
            if (noAlpha) {
                int r = readUint8(start).value;
                int g = readUint8(start+1).value;
                int b = readUint8(start+2).value;
                return ColorHelper.pack(r, g, b);
            } else if (indexed) {
                uint8 index = readUint8(start);
                return colorIndex.get(index);
            }
            return (int)readUint32(start).value;
        }

        protected int readPixel() {
            if (noAlpha) {
                int r = readUint8().value;
                int g = readUint8().value;
                int b = readUint8().value;
                return ColorHelper.pack(r, g, b);
            } else if (indexed) {
                uint8 index = readUint8();
                return colorIndex.get(index);
            }
            return (int)readUint32().value;
        }


        public FrameBuffer decode(byte[] bytes) throws IOException {
            this.binary = bytes;

            pointer = 0;

            if (readUint32().equals(FBB_TYPE_STRING))
                throw new IOException("Invalid file type");
            
            int dataPointer = (int)readUint32().value;

            int width = readUint16().value;
            int height = readUint16().value;

            ByteFlags flags = read(new ByteFlags());
            rle8 = flags.flags[0];
            rle16 = flags.flags[1];
            noAlpha = flags.flags[2];
            indexed = flags.flags[3];

            pointer += 3; // skip padding

            // handle header table entries
            int entryStart = pointer;
            uint16 entryType = readUint16();
            while (entryType.equals(HEADER_TABLE_ENTRY_LAST)) {
                int entryLength = readUint16().value;
                if (entryType.equals(HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE)) {
                    while (pointer < entryStart + entryLength) {
                        uint8 index = readUint8();
                        int r = readUint8().value;
                        int g = readUint8().value;
                        int b = readUint8().value;
                        int color = ColorHelper.pack(r, g, b);
                        colorIndex.put(index, color);
                    }
                } else {
                    // unknown entry type
                }
                pointer = entryStart + entryLength;
            }

            // data section
            pointer = dataPointer;
            int dataLength = (int) readUint32().value;
            
            frameBuffer = new ArrayFrameBuffer(width, height);

            int pixelIndex = 0;
            while (pointer < dataPointer + dataLength) {
                if (rle8 || rle16) {
                    int l = 0;
                    if (rle8)
                        l = readUint8().value;
                    if (rle16)
                        l = readUint16().value;
                    int color = readPixel();
                    for (int i = 0; i <= l; i++) {
                        frameBuffer.setColorIndexed(pixelIndex, color);
                        pixelIndex++;
                    }
                } else {
                    frameBuffer.setColorIndexed(pixelIndex, readPixel());
                }
            }

            return frameBuffer;
        }

    }
}
