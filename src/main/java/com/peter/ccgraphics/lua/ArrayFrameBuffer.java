package com.peter.ccgraphics.lua;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * Array backed FrameBuffer.
 * More performant than {@link MapFrameBuffer}, but less memory efficient when converting from LUA tables
 */
public class ArrayFrameBuffer extends FrameBuffer {

    /**
     * Pixels stored as ARGB 8-bit each
     */
    private int[] buffer;

    /**
     * Create a new Frame buffer
     * @param width Width of the frame
     * @param height Height of the frame
     */
    public ArrayFrameBuffer(int width, int height) {
        super(width, height);
        buffer = new int[width * height];
    }

    private int xyToIndex(int x, int y) {
        return x + (y * width);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException("Pixel must be between (0,0) and (width-1, height-1) inclusive, was ("+x+","+y+")");
        buffer[xyToIndex(x, y)] = color;
    }

    @Override
    public int getPixel(int x, int y) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException("Pixel must be between (0,0) and (width-1, height-1) inclusive, was ("+x+","+y+")");
        return buffer[xyToIndex(x, y)];
    }

    @Override
    protected void encodePixels(ByteBuf buf) {
        for (int i = 0; i < buffer.length; i++) {
            buf.writeInt(buffer[i]);
        }
    }

    @Override
    protected void decodePixels(ByteBuf buf) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = buf.readInt();
        }
    }

    @Override
    public FrameBuffer copy() {
        ArrayFrameBuffer frame = new ArrayFrameBuffer(width, height);
        for (int i = 0; i < buffer.length; i++) {
            frame.buffer[i] = buffer[i];
        }
        return frame;
    }

    @Override
    public float[] getTextureBuffer() {
        float[] texBuffer = new float[width * height * 3];
        for (int i = 0; i < buffer.length; i++) {
            int pixel = this.buffer[i];
            texBuffer[i*3] = ((float) (pixel >> 16 & 0xff) ) / 255f;
            texBuffer[i*3+1] = ((float) (pixel >> 8 & 0xff) ) / 255f;
            texBuffer[i*3+2] = ((float) (pixel & 0xff) ) / 255f;
        }
        return texBuffer;
    }

    @Override
    public final Map<?, ?> getTable() {
        HashMap<Object, Integer> table = new HashMap<Object, Integer>();
        table.put("width", width);
        table.put("height", height);
        for (int i = 0; i < buffer.length; i++) {
            table.put(i, buffer[i]);
        }
        return table;
    }
}
