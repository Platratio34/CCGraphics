package com.peter.ccgraphics.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peter.ccgraphics.ColorHelper;
import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;

public class FrameBufferBinary {

    protected static final Utf8String FBB_TYPE_STRING = new Utf8String("fbb ");
    protected static final uint16 HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE = new uint16(0x0001);
    protected static final uint16 HEADER_TABLE_ENTRY_LAST = new uint16(0x00);

    protected static final String[] HEADER_FLAG_NAMES = new String[] { "rle8", "rle16", "opaque", "indexed" };

    protected static final Logger LOGGER = LoggerFactory.getLogger(FrameBufferBinary.class);

    public static class Encoder {

        protected final FrameBuffer frameBuffer;
        protected final ArrayList<Byte> binary = new ArrayList<Byte>();

        protected boolean rle8 = false;
        protected boolean rle16 = false;
        protected boolean indexed = false;
        protected boolean opaque = false;

        protected final HashMap<Integer, uint8> colorIndex = new HashMap<Integer, uint8>();
        protected int maxColorIndex = -1;

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

        public void setOpaque(boolean v) {
            opaque = v;
        }

        public void setIndexed(boolean v) {
            opaque = v;
        }

        public void indexColor(int index, int color) {
            if (opaque)
                color &= 0x00ffffff;
            colorIndex.put(color, new uint8(index));
            maxColorIndex = (index > maxColorIndex) ? index : maxColorIndex;
        }

        public void clearColorIndex() {
            colorIndex.clear();
        }

        protected int lastColorIndex = 0;

        public boolean tryIndexed() {
            lastColorIndex = 0;
            boolean suc = tryIndexed(frameBuffer);
            if (suc) {
                // LOGGER.info("Color indexing succeeded");
                indexed = true;
            }
            return suc;
        }

        protected boolean tryIndexed(FrameBuffer frameBuffer) {
            for (int i = 0; i < frameBuffer.getLength(); i++) {
                int color = frameBuffer.getColorIndexed(i);
                if (opaque)
                    color &= 0x00ffffff;
                if (colorIndex.containsKey(color)) {
                    continue;
                }
                if (lastColorIndex > 0xff) {
                    LOGGER.info("Color indexing failed, too many colors");
                    return false;
                }
                indexColor(lastColorIndex, color);
                lastColorIndex++;
            }
            return true;
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
            if (opaque) {
                write(new byte[] { (byte) ((color & 0xff0000) >> 16), (byte) ((color & 0x00ff00) >> 8),
                        (byte) (color & 0x0000ff) });
            } else if (indexed) {
                if (!colorIndex.containsKey(color))
                    throw new RuntimeException("Color mode was set to indexed, but color 0x"
                            + Integer.toHexString(color) + " could not be found in index;");
                write(colorIndex.get(color));
            } else {
                write(uint32.encode(color));
            }
        }

        protected void writeColorIndex() {
            // LOGGER.info("- Writing color index table at 0x{}", pointerHex());
            int startPos = pointer();
            write(HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE);
            write(new byte[2]); // leaving space for entry length;

            int[] colors = new int[colorIndex.size()];
            for (Entry<Integer, uint8> entry : colorIndex.entrySet()) {
                colors[entry.getValue().value] = entry.getKey();
            }
            for (int i = 0; i < colors.length; i++) {
                int[] comp = ColorHelper.unpackRGBA(colors[i]);
                if (!opaque)
                    write(uint8.encode(comp[3])); // alpha
                write(uint8.encode(comp[0])); // red
                write(uint8.encode(comp[1])); // green
                write(uint8.encode(comp[2])); // blue
            }
            int length = pointer() - startPos;
            write(uint16.encode(length), startPos + 2); // write the entry length
        }

        protected int pointer() {
            return binary.size();
        }

        protected String pointerHex() {
            return Integer.toHexString(binary.size());
        }

        public byte[] encode() {
            binary.clear();

            // LOGGER.info("Starting encode of frame buffer ...");

            write(FBB_TYPE_STRING);

            write(uint32.encode(0)); // leaving space for pointer to data section

            // LOGGER.info("- 0x{} Width: {}", pointerHex(), frameBuffer.getWidth());
            write(uint16.encode(frameBuffer.getWidth()));
            // LOGGER.info("- 0x{} Height: {}", pointerHex(), frameBuffer.getHeight());
            write(uint16.encode(frameBuffer.getHeight()));

            ByteFlags headerFlags = new ByteFlags();
            headerFlags.flags[0] = rle8;
            headerFlags.flags[1] = rle16;
            headerFlags.flags[2] = opaque;
            headerFlags.flags[3] = indexed;
            write(headerFlags);

            write(new byte[3]); // padding

            // Start of header tables
            if (indexed) {
                writeColorIndex();
            }
            write(HEADER_TABLE_ENTRY_LAST);

            // LOGGER.info("- Starting encode of data section at 0x{}", pointerHex());

            // start of data section
            int dataSectionPointer = pointer();
            write(uint32.encode(dataSectionPointer), 0x4); // write data section pointer in header
            // LOGGER.info("- Data section pointer: {}", dataSectionPointer);

            int dataStart = pointer();
            write(uint32.encode(0)); // leaving space for data length value;

            int lastPixel = frameBuffer.getColorIndexed(0);
            if (opaque)
                lastPixel &= 0x00ffffff;
            int length = -1; // set to `-1` to not count first instance of a color
            for (int i = 0; i < frameBuffer.getLength(); i++) {
                int color = frameBuffer.getColorIndexed(i);
                if (opaque)
                    color &= 0x00ffffff;

                if (!(rle8 || rle16)) {
                    writePixel(color);
                    continue;
                }

                if (rle8 && length >= uint8.MAX) {
                    write(uint8.encode(uint8.MAX));
                    length -= uint8.MAX;
                    // LOGGER.info("Writing extra byte for length");
                } else if (rle16 && length >= uint16.MAX) {
                    write(uint16.encode(uint16.MAX));
                    length -= uint16.MAX;
                    // LOGGER.info("Writing extra byte for length");
                }

                if (lastPixel != color) {
                    if (rle8)
                        write(uint8.encode(length));
                    else if (rle16)
                        write(uint16.encode(length));
                    // if (pointer() < 128)
                    //     LOGGER.info("- [{}] {}, 0x{}", pointerHex(), length, Integer.toHexString(length));
                    writePixel(lastPixel);

                    length = 0;
                    lastPixel = color;
                } else {
                    length++;
                }
            }
            if (rle8 || rle16) {
                if (rle8)
                    write(uint8.encode(length));
                else if (rle16)
                    write(uint16.encode(length));

                writePixel(lastPixel);
            }

            int dataLength = pointer() - dataStart;
            write(uint32.encode(dataLength), dataStart); // write the data section length

            // LOGGER.info("Done encoding frame buffer: Data section was {} bytes", dataLength);
            float compressionRatio = ((float) pointer()) / ((float) frameBuffer.getLength());
            // LOGGER.info("Compression Ratio: {} bytes per pixel", compressionRatio);

            return ArrayUtils.toPrimitive(binary.toArray(new Byte[0]));
        }
    }
    
    public static class Decoder {

        protected FrameBuffer frameBuffer;
        protected byte[] binary;
        protected int pointer;

        protected boolean rle8 = false;
        protected boolean rle16 = false;
        protected boolean indexed = false;
        protected boolean opaque = false;

        protected final HashMap<Integer, Integer> colorIndex = new HashMap<Integer, Integer>();

        public Decoder() {
        };

        protected uint16 readUint16() {
            return read(new uint16());
        }

        protected uint32 readUint32() {
            return read(new uint32());
        }

        protected uint8 readUint8() {
            return read(new uint8());
        }

        protected <T extends BinaryDataType> T read(T type) {
            if (binary.length < pointer + type.getLength()) {
                IndexOutOfBoundsException e = new IndexOutOfBoundsException("Tried to read outside array");
                LOGGER.error("Tried to read outside array", e);
                throw e;
            }
            type.fromByte(binary, pointer);
            pointer += type.getLength();
            return type;
        }

        protected int readPixel() {
            if (opaque) {
                int r = readUint8().value;
                int g = readUint8().value;
                int b = readUint8().value;
                return ColorHelper.pack(r, g, b);
            } else if (indexed) {
                uint8 index = readUint8();
                return colorIndex.get(index.value);
            }
            return (int) readUint32().value;
        }
        
        protected void readColorIndex(int entryEnd) {
            int index = 0;
            while (pointer < entryEnd) {
                int a = 0xff;
                if(!opaque)
                    a = readUint8().value;
                int r = readUint8().value;
                int g = readUint8().value;
                int b = readUint8().value;
                int color = ColorHelper.pack(r, g, b, a);
                colorIndex.put(index, color);
                index++;
            }
        }


        public FrameBuffer decode(byte[] bytes) throws IOException {
            this.binary = bytes;

            // LOGGER.info("Starting decode of frame buffer ...");

            pointer = 0;

            Utf8String fileType = read(new Utf8String(4));
            if (!fileType.equals(FBB_TYPE_STRING)) {
                IOException e = new IOException("Invalid file type, was " + fileType.getString());
                throw e;
            }
            
            int dataPointer = (int) readUint32().value;
            // LOGGER.info("- Data pointer: 0x{}", Integer.toHexString(dataPointer));

            int width = readUint16().value;
            // LOGGER.info("- 0x{} Width: {}", Integer.toHexString(pointer-2), width);
            int height = readUint16().value;
            // LOGGER.info("- 0x{} Height: {}", Integer.toHexString(pointer-2), height);

            ByteFlags flags = read(new ByteFlags());
            flags.flagNames = HEADER_FLAG_NAMES;
            rle8 = flags.flags[0];
            rle16 = flags.flags[1];
            opaque = flags.flags[2];
            indexed = flags.flags[3];

            // LOGGER.info("- Header Flags: {}", flags);

            pointer += 3; // skip padding

            int numHeaderEntries = 0;

            // handle header table entries
            HashMap<uint16, Boolean> headersPresent = new HashMap<uint16, Boolean>();
            int entryStart = pointer;
            uint16 entryType = readUint16();
            while (!entryType.equals(HEADER_TABLE_ENTRY_LAST)) {
                if (headersPresent.containsKey(entryType)) {
                    throw new IOException("Duplicate header: type 0x" + entryType.hex() + " at 0x"
                            + Integer.toHexString(pointer - 2));
                }
                headersPresent.put(entryType, true);
                // LOGGER.info("- Header Table Entry: {}", entryType.hex());
                numHeaderEntries++;
                if (numHeaderEntries > 0xf) {
                    throw new IOException("Too many header table entries");
                }
                int entryLength = readUint16().value;
                if (entryType.equals(HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE)) {
                    // LOGGER.info("- Header contained color index table");
                    readColorIndex(entryStart + entryLength);
                } else {
                    LOGGER.warn("- Unknown header table entry type: 0x{}", entryType.hex());
                    // unknown entry type
                }
                pointer = entryStart + entryLength;
                entryType = readUint16();
            }
            // LOGGER.info("- - Reached end of header table");

            // LOGGER.info("- Starting decode of data section");

            // data section
            pointer = dataPointer;
            long dataLengthLong = readUint32().value;
            int dataLength = (int) dataLengthLong;
            // LOGGER.info("- - Length of data section: {}", dataLengthLong);
            
            frameBuffer = new ArrayFrameBuffer(width, height);
            // LOGGER.info("- - For frame of {} pixels", frameBuffer.getLength());

            int pixelIndex = 0;
            while (pointer < dataPointer + dataLength && pixelIndex < frameBuffer.getLength()/* && pixelIndex < 1000 */) {
                // LOGGER.info("- - {}: Starting decode", pixelIndex);
                if (rle8 || rle16) {
                    int repetitions = 0;
                    if (rle8) {
                        int v = readUint8().value;
                        while (v == uint8.MAX) {
                            // LOGGER.info("- - {} - {}", pixelIndex, repetitions);
                            repetitions += v;
                            v = readUint8().value;
                        }
                        repetitions += v;
                    } else if (rle16) {
                        int v = readUint16().value;
                        while (v == uint16.MAX) {
                            // LOGGER.info("- - {} - {}", pixelIndex, repetitions);
                            repetitions += v;
                            v = readUint16().value;
                        }
                        repetitions += v;
                    }
                    int color = readPixel();
                    // int sPixel = pixelIndex;
                    for (int i = 0; i <= repetitions; i++) {
                        if (pixelIndex >= frameBuffer.getLength()) {
                            LOGGER.error("Pixel run extended outside frame:");
                            break;
                        }
                        frameBuffer.setColorIndexed(pixelIndex, color);
                        pixelIndex++;
                    }
                    // if(repetitions > 0)
                    //     LOGGER.info("- - {}-{} Decoded to 0x{}", sPixel, pixelIndex, Integer.toHexString(color));
                    // else
                    //     LOGGER.info("- - {} Decoded to 0x{}", pixelIndex, Integer.toHexString(color));
                } else {
                    frameBuffer.setColorIndexed(pixelIndex, readPixel());
                    // LOGGER.info("- - {} Decoded", pixelIndex);
                    pixelIndex++;
                }
            }
            if (pointer != dataPointer + dataLength) {
                LOGGER.warn("Data ended early");
                pointer = dataPointer + dataLength;
            }

            // LOGGER.info("Finished decoding frame buffer");

            return frameBuffer;
        }

        public int usedBytes() {
            return pointer;
        }

    }
}
