package com.peter.ccgraphics.data;

import java.io.IOException;
import org.apache.commons.lang3.ArrayUtils;

import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;

public class FrameBufferBinarySequence extends FrameBufferBinary {

    public static final uint32 FBS_TYPE_STRING = new uint32(0x66627320);

    public static final uint8 FRAME_TYPE_DEFAULT = new uint8(0x0000);
    public static final uint8 FRAME_TYPE_KEYFRAME = new uint8(0x0000);

    public class Encoder extends FrameBufferBinary.Encoder {

        protected final FrameBuffer[] frameBuffers;

        protected boolean changeOnly = false;

        public Encoder(FrameBuffer[] frameBuffers) {
            super(frameBuffers[0]);
            this.frameBuffers = frameBuffers;
        }

        public void setChangeOnly(boolean v) {
            changeOnly = v;
        }

        protected void writeSkipped(int skippedPixels) {
            while (skippedPixels > 0xff) {
                write(uint8.encode(0xff));
                skippedPixels -= 0xff;
            }
            write(uint8.encode(skippedPixels));
        }

        @Override
        public boolean tryIndexed() {
            lastColorIndex = 0;
            for (int i = 0; i < frameBuffers.length; i++) {
                if (!tryIndexed(frameBuffers[i]))
                    return false;
            }
            return true;
        }

        @Override
        public byte[] encode() {
            binary.clear();

            write(FBB_TYPE_STRING);

            write(uint32.encode(0)); // leaving space for pointer to data section

            write(uint16.encode(frameBuffer.getWidth()));
            write(uint16.encode(frameBuffer.getHeight()));

            ByteFlags headerFlags = new ByteFlags();
            headerFlags.flags[0] = rle8;
            headerFlags.flags[1] = rle16;
            headerFlags.flags[2] = opaque;
            headerFlags.flags[3] = indexed;

            headerFlags.flags[4] = changeOnly;
            write(headerFlags);

            write(new byte[1]); // padding

            write(uint16.encode(frameBuffers.length));

            // Start of header tables
            if (indexed) {
                writeColorIndex();
            }

            // start of data section
            write(uint32.encode(binary.size()), 0x4); // write data section pointer in header

            int numFramesPointer = binary.size();

            write(uint16.encode(0)); // leaving space for num frames defined

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
                    write(uint8.encode(repeatedFrames), repeatedFramesPointer);
                    repeatedFrames = 0;
                }
                numFrames++;

                int frameStart = binary.size();
                write(uint32.encode(0)); // leaving space for frame length value;

                write(uint16.encode(fN)); // Frame number

                repeatedFramesPointer = binary.size();
                write(uint8.encode(0)); // Frame repetitions (we may overwrite this later)

                write(FRAME_TYPE_DEFAULT);

                int skippedPixels = 0;

                long lastPixel = 0x100000000l;
                int length = 0;
                for (int pI = 0; pI < frameBuffer.length(); pI++) {
                    int color = frameBuffer.getColorIndexed(pI);
                    if (opaque)
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
                        // write = write || (rle8 && (length == 0xff));
                        // write = write || (rle16 && (length == 0xffff));

                        if (write) {
                            writeSkipped(skippedPixels);

                            if (rle8) {
                                while(length >= 0xff) {
                                    write(uint8.encode(0xff));
                                }
                                write(uint8.encode(length));
                            } else if (rle16) {
                                while(length >= 0xffff) {
                                    write(uint16.encode(0xffff));
                                }
                                write(uint16.encode(length));
                            }

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
            opaque = flags.flags[2];
            indexed = flags.flags[3];
            
            changeOnly = flags.flags[4];

            pointer += 1; // skip padding

            int numFrames = readUint16().value;
            frameBuffers = new FrameBuffer[numFrames];

            // handle header table entries
            int entryStart = pointer;
            uint16 entryType = readUint16();
            while (entryType.equals(HEADER_TABLE_ENTRY_LAST)) {
                int entryLength = readUint16().value;
                if (entryType.equals(HEADER_TABLE_ENTRY_COLOR_INDEX_TYPE)) {
                    readColorIndex(entryStart + entryLength);
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

                FrameBuffer frameBuffer;

                int frameNumber = readUint16().value;
                int frameRepetitions = readUint8().value;
                uint8 frameType = readUint8();

                if (fN == 0 || frameType.equals(FRAME_TYPE_KEYFRAME)) {
                    frameBuffer = new ArrayFrameBuffer(width, height);
                } else if (frameType.equals(FRAME_TYPE_DEFAULT)) {
                    frameBuffer = frameBuffers[fN - 1].copy();
                } else {
                    frameBuffer = new ArrayFrameBuffer(width, height);
                }
                
                frameBuffers[frameNumber] = frameBuffer;
                for (int i = 0; i < frameRepetitions; i++) {
                    frameBuffers[frameNumber + i + 1] = frameBuffer;
                }

                int pixelIndex = 0;
                while (pointer < dataPointer + frameLength) {
                    if (changeOnly) {
                        int v = readUint8().value;
                        while (v == 0xff) {
                            pixelIndex += v;
                            v = readUint8().value;
                        }
                        pixelIndex += v;
                    }

                    if (rle8 || rle16) {
                        int pixelRepetitions = 0;
                        if (rle8) {
                            int v = readUint8().value;
                            while (v == 0xff) {
                                pixelRepetitions += v;
                                v = readUint8().value;
                            }
                            pixelRepetitions += v;
                        } else if (rle16) {
                            int v = readUint16().value;
                            while (v == 0xffff) {
                                pixelRepetitions += v;
                                v = readUint16().value;
                            }
                            pixelRepetitions += v;
                        }
                        int color = readPixel();
                        for (int i = 0; i <= pixelRepetitions; i++) {
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
