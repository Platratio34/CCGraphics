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

    protected static final Utf8String FBB_TYPE_STRING = new Utf8String("fbb", true);
    protected static final uint16 HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE = new uint16(0x0001);
    protected static final uint16 HEADER_TABLE_ENTRY_LAST = new uint16(0x0000);

    protected static final String[] HEADER_FLAG_NAMES = new String[] { "rle8", "rle16", "opaque", "indexed", "[4]", "[5]", "[6]", "indexed15" };

    protected static final Logger LOGGER = LoggerFactory.getLogger(FrameBufferBinary.class);

    public static class Encoder {

        protected final FrameBuffer frameBuffer;
        protected final ArrayList<Byte> binary = new ArrayList<Byte>();

        protected boolean rle8 = false;
        protected boolean rle16 = false;
        protected boolean indexed = false;
        protected boolean opaque = false;

        protected boolean rle15 = false;

        protected boolean rle = false;

        protected boolean indexed15 = false;

        protected final HashMap<Integer, Integer> colorIndex = new HashMap<Integer, Integer>();
        protected int maxColorIndex = -1;
        protected final CountingMap<Integer> colorCount = new CountingMap<Integer>();

        public Encoder(FrameBuffer frameBuffer) {
            this.frameBuffer = frameBuffer;
        }

        public void setRle8(boolean v) {
            rle8 = v;
            rle = v;
            if (v) {
                rle16 = false;
                rle15 = false;
                rle = true;
            }
        }

        public void setRle16(boolean v) {
            rle16 = v;
            rle = v;
            if (v) {
                rle8 = false;
                rle15 = false;
            }
        }

        public void setRle15(boolean v) {
            rle15 = v;
            rle = v;
            if (v) {
                rle8 = false;
                rle16 = false;
            }
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
            if (indexed15) {
                if (index > Uint7_15.MAX_15) {
                    throw new IllegalArgumentException("Index can not be larger than 0x7fff in indexed15 mode");
                }
            } else {
                if (index > uint8.MAX) {
                    throw new IllegalArgumentException("Index can not be larger than 0xff in indexed8 mode");
                }
            }
            colorIndex.put(color, index);
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
                indexed = true;
                // LOGGER.info("Indexing succeeded with {} colors", lastColorIndex);
            } else {
                indexed = false;
                indexed15 = false;
            }
            return suc;
        }

        protected boolean tryIndexed(FrameBuffer frameBuffer) {
            for (int i = 0; i < frameBuffer.getLength(); i++) {
                int color = frameBuffer.getColorIndexed(i);
                if (opaque)
                    color &= 0x00ffffff;
                colorCount.count(color);
                if (colorIndex.containsKey(color)) {
                    continue;
                }
                if (lastColorIndex == uint8.MAX + 1) {
                    indexed15 = true;
                    // LOGGER.info("Switching to indexed 15");
                    // return false;
                } else if (lastColorIndex > Uint7_15.MAX_15) {
                    // LOGGER.info("Indexing failed");
                    return false;
                }
                indexColor(lastColorIndex, color);
                lastColorIndex++;
            }
            return true;
        }

        public void tryRLE() {
            int[] totals = tryRLE(frameBuffer);
            int ns = totals[0];
            int b8 = totals[1];
            int b16 = totals[2];
            int b15 = totals[3];

            int min = b8;
            if (b15 < min) {
                min = b15;
            }

            if (b16 < min) {
                if (b16 >= ns) {
                    // LOGGER.info("Not using RLE");
                    return;
                }
                // LOGGER.info("Using RLE16");
                setRle16(true);
            } else {
                if (min >= ns) {
                    // LOGGER.info("Not using RLE");
                    return;
                }
                if (min == b8) {
                    // LOGGER.info("Using RLE8");
                    setRle8(true);
                } else {
                    // LOGGER.info("Using RLE5");
                    setRle15(true);
                }
            }

        }

        protected int[] tryRLE(FrameBuffer frameBuffer) {
            int ns = 0;
            int b8 = 0;
            int b16 = 0;
            int b15 = 0;
            int run = -1;
            int lastColor = frameBuffer.getColorIndexed(0);
            for (int i = 0; i < frameBuffer.getLength(); i++) {
                int color = frameBuffer.getColorIndexed(i);
                if (color == lastColor) {
                    run++;
                    ns++;
                    continue;
                }

                b8 += Math.ceilDiv(run, uint8.MAX + 1);
                b16 += Math.ceilDiv(run, uint16.MAX + 1) * 2;

                while (run >= Uint7_15.MAX) {
                    b15 += 2;
                    run -= Uint7_15.MAX;
                }

                if (run < Uint7_15.MAX_7) {
                    b15++;
                } else {
                    b15 += 2;
                }

                lastColor = color;
                run = 0;
            }

            b8 += Math.ceilDiv(run, uint8.MAX + 1);
            b16 += Math.ceilDiv(run, uint16.MAX + 1);

            while (run >= Uint7_15.MAX) {
                b15 += 2;
                run -= Uint7_15.MAX;
            }

            if (run < Uint7_15.MAX_7) {
                b15++;
            } else {
                b15 += 2;
            }
            return new int [] { ns, b8, b16, b15 };
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
            if (indexed) {
                if (!colorIndex.containsKey(color))
                    throw new RuntimeException("Color mode was set to indexed, but color 0x"
                            + Integer.toHexString(color) + " could not be found in index;");
                int index = colorIndex.get(color);
                if (indexed15) {
                    write(Uint7_15.encode(index));
                } else {
                    write(uint8.encode(index));
                }
            } else if (opaque) {
                write(new byte[] { (byte) ((color & 0xff0000) >> 16), (byte) ((color & 0x00ff00) >> 8),
                        (byte) (color & 0x0000ff) });
            } else {
                write(uint32.encode(color));
            }
        }

        protected void writeRLE(int length) {
            if (rle15) {
                while (length >= Uint7_15.MAX) {
                    write(Uint7_15.encode(Uint7_15.MAX));
                    length -= Uint7_15.MAX;
                }
                write(Uint7_15.encode(length));
            } else if (rle8) {
                while (length >= uint8.MAX) {
                    write(uint8.encode(uint8.MAX));
                    length -= uint8.MAX;
                }
                write(uint8.encode(length));
            } else if (rle16) {
                while (length >= uint16.MAX) {
                    write(uint16.encode(uint16.MAX));
                    length -= uint16.MAX;
                }
                write(uint16.encode(length));
            }
        }

        protected void writeColorIndex() {
            // LOGGER.info("- Writing color index table at 0x{}", pointerHex());
            int startPos = pointer();
            write(HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE);
            write(new byte[2]); // leaving space for entry length;

            int[] colors = new int[colorIndex.size()];
            if (indexed15) {
                Integer[] arr = colorCount.sort(new Integer[colorIndex.size()]);
                for (int i = 0; i < arr.length; i++) {
                    colors[i] = arr[i];
                    colorIndex.put(arr[i], i);
                }
            } else {
                for (Entry<Integer, Integer> entry : colorIndex.entrySet()) {
                    colors[entry.getValue()] = entry.getKey();
                }
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
        
        protected void writeHeaderFlags(ByteFlags flags) {
            flags.flags[0] = rle8 || rle15;
            flags.flags[1] = rle16 || rle15;
            flags.flags[2] = opaque;
            flags.flags[3] = indexed;

            flags.flags[7] = indexed15;

            write(flags);
        }

        protected void writeHeader(Utf8String fileType) {
            write(fileType);

            write(uint32.encode(0)); // leaving space for pointer to data section

            // LOGGER.info("- 0x{} Width: {}", pointerHex(), frameBuffer.getWidth());
            write(uint16.encode(frameBuffer.getWidth()));
            // LOGGER.info("- 0x{} Height: {}", pointerHex(), frameBuffer.getHeight());
            write(uint16.encode(frameBuffer.getHeight()));

            writeHeaderFlags(new ByteFlags());

            write(new byte[3]); // padding
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

            writeHeader(FBB_TYPE_STRING);

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

                if (!(rle)) {
                    writePixel(color);
                    continue;
                }

                if (lastPixel != color) {
                    writeRLE(length);

                    writePixel(lastPixel);

                    length = 0;
                    lastPixel = color;
                } else {
                    length++;
                }
            }
            
            if (rle) {
                writeRLE(length);

                writePixel(lastPixel);
            }

            int dataLength = pointer() - dataStart;
            write(uint32.encode(dataLength), dataStart); // write the data section length

            // LOGGER.info("Done encoding frame buffer: Data section was {} bytes", dataLength);
            // float compressionRatio = ((float) pointer()) / ((float) frameBuffer.getLength());
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

        protected boolean rle15 = false;
        protected boolean rle = false;

        protected boolean indexed15 = false;

        protected int width;
        protected int height;

        protected int dataPointer;

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
            if (indexed) {
                int index = 0;
                if (indexed15) {
                    index = read(new Uint7_15()).value;
                } else {
                    index = readUint8().value;
                }
                if (!colorIndex.containsKey(index)) {
                    throw new RuntimeException("Could not find color in index: " + index);
                }
                return colorIndex.get(index);
            } else if (opaque) {
                int r = readUint8().value;
                int g = readUint8().value;
                int b = readUint8().value;
                return ColorHelper.pack(r, g, b);
            }
            return (int) readUint32().value;
        }

        protected void readHeaderEntries() throws IOException {
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
                int entryLength = readUint16().value;

                readHeaderEntry(entryType, entryStart + entryLength);

                pointer = entryStart + entryLength;
                entryType = readUint16();
            }
        }

        /**
         * Read a specific header entry. <b>OVERRIDE THIS FOR CUSTOM ENTRY TYPES</b>
         * 
         * @param entryType
         * @param entryEnd
         */
        protected void readHeaderEntry(uint16 entryType, int entryEnd) {
            if (entryType.equals(HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE)) {
                // LOGGER.info("- Header contained color index table");
                readColorIndex(entryEnd);
            } else {
                LOGGER.warn("- Unknown header table entry type: 0x{}", entryType.hex());
            }
        }

        protected void readColorIndex(int entryEnd) {
            int index = 0;
            while (pointer < entryEnd) {
                int a = 0xff;
                if (!opaque)
                    a = readUint8().value;
                int r = readUint8().value;
                int g = readUint8().value;
                int b = readUint8().value;
                int color = ColorHelper.pack(r, g, b, a);
                colorIndex.put(index, color);
                index++;
            }
        }

        protected void readHeader(Utf8String fileType) throws IOException {
            checkFileType(fileType);
            
            dataPointer = (int) readUint32().value;
            // LOGGER.info("- Data pointer: 0x{}", Integer.toHexString(dataPointer));

            width = readUint16().value;
            // LOGGER.info("- 0x{} Width: {}", Integer.toHexString(pointer-2), width);
            height = readUint16().value;
            // LOGGER.info("- 0x{} Height: {}", Integer.toHexString(pointer-2), height);

            ByteFlags flags = read(new ByteFlags());
            updateFlags(flags);

            // LOGGER.info("- Header Flags: {}", flags);

            pointer += 3; // skip padding
        }

        protected void updateFlags(ByteFlags flags) {
            flags.flagNames = HEADER_FLAG_NAMES;
            rle8 = flags.flags[0];
            rle16 = flags.flags[1];
            opaque = flags.flags[2];
            indexed = flags.flags[3];

            indexed15 = flags.flags[7];

            rle15 = rle8 && rle16;
            if (rle15) {
                rle8 = false;
                rle16 = false;
                rle = true;
            } else if (rle8 || rle16) {
                rle = true;
            }
        }

        protected int readRLE() {
            if (!rle)
                return 0;

            int repetitions = 0;
            if (rle15) {
                int v = read(new Uint7_15()).value;
                while (v == Uint7_15.MAX_15) {
                    repetitions += v;
                    v = read(new Uint7_15()).value;
                }
                repetitions += v;
            } else if (rle8) {
                int v = readUint8().value;
                while (v == uint8.MAX) {
                    repetitions += v;
                    v = readUint8().value;
                }
                repetitions += v;
            } else if (rle16) {
                int v = readUint16().value;
                while (v == uint16.MAX) {
                    repetitions += v;
                    v = readUint16().value;
                }
                repetitions += v;
            }
            return repetitions;
        }
        
        protected void checkFileType(Utf8String expected) throws IOException {
            Utf8String fileType = read(new Utf8String());
            if (!fileType.equals(expected)) {
                throw new IOException("Invalid file type, was \"" + fileType.getString() + "\", expected \""+expected.string+"\"");
            }
        }

        public FrameBuffer decode(byte[] bytes) throws IOException {
            this.binary = bytes;

            // LOGGER.info("Starting decode of frame buffer ...");

            pointer = 0;

            readHeader(FBB_TYPE_STRING);

            readHeaderEntries();

            // LOGGER.info("- - Reached end of header table");

            // LOGGER.info("- Starting decode of data section");

            // +--------------+
            // | DATA SECTION |
            // +--------------+
            pointer = dataPointer;

            long dataLengthLong = readUint32().value;
            int dataLength = (int) dataLengthLong;

            frameBuffer = new ArrayFrameBuffer(width, height);

            int pixelIndex = 0;
            while (pointer < dataPointer + dataLength
                    && pixelIndex < frameBuffer.getLength()) {
                        
                if (rle) {
                    int repetitions = readRLE();
                    int color = readPixel();
                    for (int i = 0; i <= repetitions; i++) {
                        if (pixelIndex >= frameBuffer.getLength()) {
                            LOGGER.error("Pixel run extended outside frame:");
                            break;
                        }
                        frameBuffer.setColorIndexed(pixelIndex, color);
                        pixelIndex++;
                    }
                } else {
                    frameBuffer.setColorIndexed(pixelIndex, readPixel());
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
