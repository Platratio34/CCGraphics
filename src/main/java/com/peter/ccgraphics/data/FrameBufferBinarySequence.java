package com.peter.ccgraphics.data;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import com.peter.ccgraphics.ColorHelper;
import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;

public class FrameBufferBinarySequence extends FrameBufferBinary {

    public static final uint32 FBS_TYPE_STRING = new uint32(0x66627320);

    public class Encoder extends FrameBufferBinary.Encoder {

        protected final FrameBuffer[] frameBuffers;

        protected boolean co8 = false;
        protected boolean co16 = false;
        protected boolean co15 = false;
        protected boolean coVar = false;

        protected boolean changeOnly = false;

        public Encoder(FrameBuffer[] frameBuffers) {
            super(frameBuffers[0]);
            this.frameBuffers = frameBuffers;
        }

        public void setChangeOnly8(boolean v) {
            co8 = v;
            changeOnly = v;
            if (v) {
                co16 = false;
                co15 = false;
                coVar = false;
            }
        }

        public void setChangeOnly16(boolean v) {
            co16 = v;
            changeOnly = v;
            if (v) {
                co8 = false;
                co15 = false;
                coVar = false;
            }
        }

        public void setChangeOnly15(boolean v) {
            co15 = v;
            changeOnly = v;
            if (v) {
                co8 = false;
                co16 = false;
                coVar = false;
            }
        }

        public void setChangeOnlyVar(boolean v) {
            coVar = v;
            changeOnly = v;
            if (v) {
                co8 = false;
                co16 = false;
                co15 = false;
            }
        }

        protected void writeSkipped(int skippedPixels) {
            if (co8) {
                write(uint8.encode(skippedPixels));
            } else if (co16) {
                write(uint16.encode(skippedPixels));
            } else if (co15) {
                if (skippedPixels < 0b10000000) {
                    write(uint8.encode(skippedPixels));
                } else {
                    write(uint16.encode(skippedPixels));
                }
            } else if (coVar) {
                while (skippedPixels > 0xff) {
                    write(uint8.encode(0xff));
                    skippedPixels -= 0xff;
                }
                write(uint8.encode(skippedPixels));
            }
        }

        @Override
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

            headerFlags.flags[4] = co8;
            headerFlags.flags[5] = co16;
            headerFlags.flags[6] = co15;
            headerFlags.flags[7] = coVar;
            write(headerFlags);

            write(new byte[1]); // padding

            write(uint16.encode(frameBuffers.length));

            // Start of header tables
            if (indexed) {
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

            int numFramesPointer = binary.size();
            // write(uint16.encode(frameBuffers.length));
            write(new byte[4]); // leaving space for num frames defined
            int numFrames = 0;

            write(new byte[2]); // padding

            int repeatedFrames = 0;
            int repeatedFramesPointer = -1;
            for (int fN = 0; fN < frameBuffers.length; fN++) {
                FrameBuffer frameBuffer = frameBuffers[fN];
                if (fN > 0 && frameBuffer.equals(frameBuffers[fN - 1])) {
                    repeatedFrames++;
                    continue;
                } else if (repeatedFrames > 0) {
                    write(uint16.encode(repeatedFrames), repeatedFramesPointer);
                    repeatedFrames = 0;
                }
                numFrames++;

                int frameStart = binary.size();
                write(new byte[4]); // leaving space for frame length value;

                write(uint16.encode(fN)); // Frame number
                repeatedFramesPointer = binary.size();
                write(uint16.encode(0)); // Frame repetitions (we may overwrite this later)

                int skippedPixels = 0;

                long lastPixel = 0x100000000l;
                int length = 0;
                for (int pI = 0; pI < frameBuffer.length(); pI++) {
                    int color = frameBuffer.getColorIndexed(pI);
                    if (noAlpha)
                        color &= 0xffffff;

                    if (changeOnly && fN > 0) {
                        if (color == frameBuffers[fN - 1].getColorIndexed(pI)) {
                            // this pixel is the same as the last frame so we should skip it

                            if (pI > 0 && skippedPixels == 0 && rle8 || rle16) { // Special case for first skipped pixel to write RLE if enabled
                                if (length < 0) // Special case for single pixel run before un-changed
                                    length = 0;
                                if (rle8)
                                    write(uint8.encode(length));
                                else if (rle16)
                                    write(uint16.encode(length));

                                writePixel(color);

                                length = -1; // set to `-1` to not count first instance of a color
                                lastPixel = color;
                                skippedPixels = 0;
                            }
                            skippedPixels++;
                            continue;
                        }
                    }

                    if (rle8 || rle16) {
                        if (pI == 0) {
                            lastPixel = color;
                            continue;
                        }

                        boolean write = lastPixel != color;
                        write = write || (rle8 && (length == 0xff));
                        write = write || (rle16 && (length == 0xffff));

                        if (write) {
                            writeSkipped(skippedPixels);

                            if (rle8)
                                write(uint8.encode(length));
                            else if (rle16)
                                write(uint16.encode(length));

                            writePixel(color);

                            length = -1; // set to `-1` to not count first instance of a color
                            lastPixel = color;
                        } else {
                            length++;
                        }
                    } else {
                        writeSkipped(skippedPixels);

                        writePixel(color);
                    }
                    skippedPixels = 0;
                }

                int frameLength = binary.size() - frameStart;
                write(uint32.encode(frameLength), frameStart); // write the frame length
            }

            write(uint16.encode(numFrames), numFramesPointer); // write the number of frames we actually defined

            return ArrayUtils.toPrimitive(binary.toArray(new Byte[0]));
        }
    }

    class Decoder extends FrameBufferBinary.Decoder {

        protected FrameBuffer[] frameBuffers;

        protected boolean co8 = false;
        protected boolean co16 = false;
        protected boolean co15 = false;
        protected boolean coVar = false;

        protected boolean changeOnly = false;

        @Override
        public FrameBuffer decode(byte[] bytes) throws IOException {
            this.binary = bytes;

            pointer = 0;

            if (readUint32().equals(FBS_TYPE_STRING))
                throw new IOException("Invalid file type");
            
            int dataPointer = (int)readUint32().value;

            int width = readUint16().value;
            int height = readUint16().value;

            ByteFlags flags = read(new ByteFlags());
            rle8 = flags.flags[0];
            rle16 = flags.flags[1];
            noAlpha = flags.flags[2];
            indexed = flags.flags[3];
            
            co8 = flags.flags[4];
            co16 = flags.flags[5];
            co15 = flags.flags[6];
            coVar = flags.flags[7];
            changeOnly = co8 || co16 || co15 || coVar;

            pointer += 1; // skip padding

            int numFrames = readUint16().value;
            frameBuffers = new FrameBuffer[numFrames];

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
            int defFrames = readUint16().value;
            pointer += 2; // skip padding
            for (int fN = 0; fN < defFrames; fN++) {
                int frameStart = pointer;
                int frameLength = (int) readUint32().value;

                FrameBuffer frameBuffer = new ArrayFrameBuffer(width, height);

                int frameNumber = readUint16().value;
                int repetitions = readUint16().value;
                
                frameBuffers[frameNumber] = frameBuffer;
                for (int i = 0; i < repetitions; i++) {
                    frameBuffers[frameNumber + i + 1] = frameBuffer;
                }

                int pixelIndex = 0;
                while (pointer < dataPointer + frameLength) {
                    if (co8) {
                        pixelIndex += readUint8().value;
                    } else if (co16) {
                        pixelIndex += readUint16().value;
                    } else if (co15) {
                        int l = readUint8().value;
                        if ((l & 0b10000000) != 0) {
                            pointer--; // un-do the read we just did
                            l = read(new uint15()).value;
                        }
                        pixelIndex += l;
                    } else if (coVar) {
                        int b = readUint8().value;
                        while(b == 0xff) {
                            pixelIndex += b;
                            b = readUint8().value;
                        }
                        pixelIndex += b;
                    }

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
                pointer = frameStart + frameLength;
            }

            return frameBuffers[0];
        }
    }
}
