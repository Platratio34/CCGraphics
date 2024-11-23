package com.peter.ccgraphics.data;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;

public class FrameBufferBinarySequence extends FrameBufferBinary {

    public static final Utf8String FBS_TYPE_STRING = new Utf8String("fbs", true);

    public static final uint8 FRAME_TYPE_DEFAULT = new uint8(0x00);
    public static final uint8 FRAME_TYPE_KEYFRAME = new uint8(0001);
    public static final uint8 FRAME_TYPE_OPTION = new uint8(0x80);
    public static final uint8 FRAME_TYPE_END = new uint8(0xff);

    public static class Encoder extends FrameBufferBinary.Encoder {

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
            while (skippedPixels > uint8.MAX) {
                write(uint8.encode(uint8.MAX));
                skippedPixels -= uint8.MAX;
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
            indexed = true;
            return true;
        }

        @Override
        protected void writeHeaderFlags(ByteFlags flags) {
            flags.flags[4] = changeOnly;
            super.writeHeaderFlags(flags);
        }

        @Override
        public byte[] encode() {
            binary.clear();

            writeHeader(FBB_TYPE_STRING);

            write(uint16.encode(frameBuffers.length), pointer()-2); // have to back-step to write it in the normal padding

            // Start of header tables
            if (indexed) {
                writeColorIndex();
            }
            write(HEADER_TABLE_ENTRY_LAST);

            // start of data section
            write(uint32.encode(pointer()), 0x4); // write data section pointer in header

            int numFramesPointer = pointer();

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

                int frameStart = pointer();
                write(uint32.encode(0)); // leaving space for frame length value;

                write(uint16.encode(fN)); // Frame number

                repeatedFramesPointer = pointer();
                write(uint8.encode(0)); // Frame repetitions (we may overwrite this later)

                if (fN == 0) // mark the first frame as a keyframe so we don't need to constantly say we skipped 0 bytes
                    write(FRAME_TYPE_KEYFRAME);
                else
                    write(FRAME_TYPE_DEFAULT);

                int skippedPixels = 0;

                int lastPixel = frameBuffer.getColorIndexed(0);
                if (opaque)
                    lastPixel &= 0x00ffffff;
                int length = -1; // set to `-1` to not count first instance of the first pixel
                for (int pI = 0; pI < frameBuffer.getLength(); pI++) {
                    int color = frameBuffer.getColorIndexed(pI);
                    if (opaque)
                        color &= 0xffffff;

                    if (changeOnly && fN > 0) {
                        if (color == frameBuffers[fN - 1].getColorIndexed(pI)) {
                            // this pixel is the same as the last frame so we should skip it

                            if (pI > 0 && skippedPixels == 0 && rle) { // Special case for first skipped pixel to write RLE if enabled
                                if (length < 0) // Special case for single pixel run before un-changed
                                    length = 0;

                                writeRLE(length);

                                writePixel(lastPixel);

                                length = 0; // set to `-1` to not count first instance of a color
                                lastPixel = color;
                            }

                            skippedPixels++;
                            continue;
                        }
                    }

                    if (rle) {

                        boolean write = lastPixel != color;

                        if (write) {
                            if (fN > 0)
                                writeSkipped(skippedPixels);

                            writeRLE(length);

                            writePixel(lastPixel);

                            length = 0;
                            lastPixel = color;
                        } else {
                            length++;
                        }
                    } else {
                        if (fN > 0)
                            writeSkipped(skippedPixels);

                        writePixel(color);
                    }
                    skippedPixels = 0;
                }

                int frameLength = pointer() - frameStart;
                write(uint32.encode(frameLength), frameStart); // write the frame length
            }

            write(uint16.encode(numFrames), numFramesPointer); // write the number of frames we actually defined

            return ArrayUtils.toPrimitive(binary.toArray(new Byte[0]));
        }
    }

    /**
     * Decoder for FrameBufferSequence. <b>DOES NOT SUPPORT STREAM</b> See {@link StreamDecoder} for decoding frame streams
     */
    public static class Decoder extends FrameBufferBinary.Decoder {

        protected FrameBuffer[] frameBuffers;

        protected boolean changeOnly = false;

        protected void decodeFrame(FrameBuffer frameBuffer, boolean keyframe, int frameEnd) {
            int pixelIndex = 0;
            while (pointer < frameEnd) {
                if (changeOnly && !keyframe) {
                    int v = readUint8().value;
                    while (v == 0xff) {
                        pixelIndex += v;
                        v = readUint8().value;
                    }
                    pixelIndex += v;
                }

                if (rle) {
                    int pixelRepetitions = readRLE();
                    int color = readPixel();
                    for (int i = 0; i <= pixelRepetitions; i++) {
                        frameBuffer.setColorIndexed(pixelIndex, color);
                        pixelIndex++;
                    }
                } else {
                    frameBuffer.setColorIndexed(pixelIndex, readPixel());
                }
            }
        }

        @Override
        protected void updateFlags(ByteFlags flags) {
            changeOnly = flags.flags[4];
        }

        @Override
        public FrameBuffer decode(byte[] bytes) throws IOException {
            this.binary = bytes;

            pointer = 0;

            readHeader(FBS_TYPE_STRING);

            pointer -= 2; // back-step into normal padding for number of frames

            int numFrames = readUint16().value;
            frameBuffers = new FrameBuffer[numFrames];

            readHeaderEntries();

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

                if (frameType.equals(FRAME_TYPE_OPTION)) {

                    updateFlags(read(new ByteFlags()));

                    readHeaderEntries();

                } else if (frameType.equals(FRAME_TYPE_KEYFRAME)) {
                    frameBuffer = new ArrayFrameBuffer(width, height);

                    frameBuffers[frameNumber] = frameBuffer;
                    for (int i = 0; i < frameRepetitions; i++) {
                        frameBuffers[frameNumber + i + 1] = frameBuffer;
                    }

                    decodeFrame(frameBuffer, true, frameStart + frameLength);

                } else if (frameType.equals(FRAME_TYPE_DEFAULT)) {
                    if (fN == 0)
                        frameBuffer = new ArrayFrameBuffer(width, height);
                    else
                        frameBuffer = frameBuffers[fN - 1].copy();

                    frameBuffers[frameNumber] = frameBuffer;
                    for (int i = 0; i < frameRepetitions; i++) {
                        frameBuffers[frameNumber + i + 1] = frameBuffer;
                    }

                    decodeFrame(frameBuffer, false, frameStart + frameLength);
                }
                pointer = frameStart + frameLength;
            }

            return frameBuffers[0];
        }

        public FrameBuffer[] getFrames() {
            return frameBuffers;
        }
    }
    
    public class StreamDecoder extends Decoder {

        protected boolean decodedHeader = false;
        
        protected int width;
        protected int height;

        protected FrameBuffer lastFrame;

        protected boolean closed = false;

        /**
         * Decode a chunk from a stream
         * @param chunk Chunk to decode
         * @return List of frames present in chunk. May be 0 frames if only header or non-image frames present in chunk.
         * @throws IOException
         */
        public ArrayList<FrameBuffer> decodeChunk(byte[] chunk) throws IOException {
            this.binary = chunk;
            pointer = 0;

            ArrayList<FrameBuffer> frames = new ArrayList<FrameBuffer>();

            if (!decodedHeader) {
                readHeader(FBS_TYPE_STRING);

                pointer -= 1; // back-step into padding for number of frames

                int numFrames = readUint16().value;
                if (numFrames != 0xffff) {
                    throw new IOException("Wasn't a FBB stream");
                }

                readHeaderEntries();

                pointer = dataPointer;
                pointer += 4; // Padding + Num frames defined. Unused for stream
            }

            while (pointer < binary.length) {
                int frameStart = pointer;
                int frameLength = (int) readUint32().value;

                FrameBuffer frameBuffer;

                int frameNumber = readUint16().value;
                int frameRepetitions = readUint8().value;
                uint8 frameType = readUint8();

                if (frameType.equals(FRAME_TYPE_END)) {
                    closed = true;
                } else if (frameType.equals(FRAME_TYPE_OPTION)) {

                    updateFlags(read(new ByteFlags()));

                    readHeaderEntries();

                } else if (frameType.equals(FRAME_TYPE_KEYFRAME)) {
                    frameBuffer = new ArrayFrameBuffer(width, height);

                    frameBuffers[frameNumber] = frameBuffer;
                    for (int i = 0; i < frameRepetitions; i++) {
                        frameBuffers[frameNumber + i + 1] = frameBuffer;
                    }

                    decodeFrame(frameBuffer, true, frameStart + frameLength);
                    lastFrame = frameBuffer;

                } else if (frameType.equals(FRAME_TYPE_DEFAULT)) {
                    if (lastFrame == null)
                        frameBuffer = new ArrayFrameBuffer(width, height);
                    else
                        frameBuffer = lastFrame.copy();

                    frameBuffers[frameNumber] = frameBuffer;
                    for (int i = 0; i < frameRepetitions; i++) {
                        frameBuffers[frameNumber + i + 1] = frameBuffer;
                    }

                    decodeFrame(frameBuffer, false, frameStart + frameLength);
                    lastFrame = frameBuffer;
                }

                pointer = frameStart + frameLength;
            }

            lastFrame = lastFrame.copy();

            return frames;
        }
        
        public boolean closed() {
            return closed;
        }

        @Override
        public FrameBuffer decode(byte[] bytes) throws IOException {
            throw new UnsupportedOperationException(
                    "Stream Decoded does not support `decode`. See `decodeChunk` instead");
        }
    }
}
