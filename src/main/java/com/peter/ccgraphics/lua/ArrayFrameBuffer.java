package com.peter.ccgraphics.lua;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

/**
 * Array backed FrameBuffer.
 * More performant than {@link MapFrameBuffer}, but less memory efficient when converting from LUA tables
 */
public class ArrayFrameBuffer extends FrameBuffer {

    /**
     * Pixels stored as ARGB 8-bit each
     */
    private final int[] buffer;

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
        assertInFrame(x, y);
        buffer[xyToIndex(x, y)] = color;
    }

    @Override
    public void setColorIndexed(int index, int color) {
        buffer[index] = color;
    }

    @Override
    public int getPixel(int x, int y) {
        assertInFrame(x, y);
        return buffer[xyToIndex(x, y)];
    }

    @Override
    public int getColorIndexed(int index) {
        return buffer[index];
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
        System.arraycopy(buffer, 0, frame.buffer, 0, buffer.length);
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
        HashMap<Object, Integer> table = new HashMap<>();
        table.put("width", width);
        table.put("height", height);
        for (int i = 0; i < buffer.length; i++) {
            table.put(i, buffer[i]);
        }
        return table;
    }
}
