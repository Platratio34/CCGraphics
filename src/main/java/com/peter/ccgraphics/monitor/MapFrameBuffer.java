package com.peter.ccgraphics.monitor;

import java.util.Map;
import java.util.NoSuchElementException;

import com.peter.ccgraphics.lua.LuaTableHelper;

import dan200.computercraft.api.lua.LuaException;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;

/**
 * Map backed FrameBuffer.
 * Intended for creating from LUA tables.
 * Less performant than {@link ArrayFrameBuffer}, but better on memory when converting from LUA.
 */
public class MapFrameBuffer extends FrameBuffer {

    /**
     * Pixels stored as ARGB 8-bit each by index (double)
     */
    private final Map<Object, Object> map;

    /**
     * Create a new Frame buffer
     * @param width Width of the frame
     * @param height Height of the frame
     */
    protected MapFrameBuffer(int w, int h) {
        super(w, h);
        map = new HashMap<Object, Object>();
    }

    /**
     * Create a new Frame buffer
     * @param width Width of the frame
     * @param height Height of the frame
     * @param map Map of pixels
     * @throws NoSuchElementException If keys for pixels are missing or values are non-numeric
     */
    @SuppressWarnings("unchecked")
    public MapFrameBuffer(int w, int h, Map<?, ?> map) {
        super(w, h);
        this.map = (Map<Object, Object>) map;
        verifyMap(false);
    }

    /**
     * Create a new Frame buffer. <br/><br/>
     * Width & Height will be taken from the keys "width" and "height" in the map.
     * @param map Map of pixels and size
     * @throws NoSuchElementException If keys for pixels are missing or values are non-numeric
     */
    @SuppressWarnings("unchecked")
    public MapFrameBuffer(Map<?, ?> map) {
        super(LuaTableHelper.getInt(map, "width"), LuaTableHelper.getInt(map, "height"));
        this.map = (Map<Object, Object>) map;
        verifyMap();
    }

    private void verifyMap() throws NoSuchElementException {
        verifyMap(true);
    }

    private void verifyMap(boolean checkWH) throws NoSuchElementException {
        if (checkWH && !(LuaTableHelper.hasNumber(map, "width") && LuaTableHelper.hasNumber(map, "height"))) {
            throw new NoSuchElementException("Map must have `width` and `height` as numbers");
        }
        for (double i = 0; i < width * height; i++) {
            if (!LuaTableHelper.hasNumber(map, i))
                throw new NoSuchElementException(
                        "Map must have numeric entries for each pixel. Pixel at [" + i + "] was non-numeric");
        }
    }

    /**
     * Verify the provided map for use as a MapFrameBuffer, throwing a LuaException on issue.<br/></br>
     * Table must contain numeric values for the keys `width`, and `height`, and a numeric value for ever index between 0 and (width * height - 1)
     * @param map LUA table to check
     * @return If the map was valid
     * @throws LuaException If any keys were missing, or values were the wrong type
     */
    public static boolean verifyMapLUA(Map<?, ?> map) throws LuaException {
        if (!LuaTableHelper.hasNumber(map, "width") || !LuaTableHelper.hasNumber(map, "height"))
            throw new LuaException("Table must have `width` and `height` as numbers");
        int width = LuaTableHelper.getInt(map, "width");
        int height = LuaTableHelper.getInt(map, "width");
        for (double i = 0; i < width * height; i++) {
            if (!LuaTableHelper.hasNumber(map, i))
                throw new LuaException("Table must have numeric entries for each pixel. Pixel at ["+i+"] was non-numeric");
        }
        return true;
    }

    private double xyToIndex(int x, int y) {
        return x + (y * width);
    }

    private int getValueAtIndex(double i) {
        return LuaTableHelper.getIntOpt(map, i, 0x00000000);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException("Pixel must be between (0,0) and (width-1, height-1) inclusive");
        map.put(xyToIndex(x, y), color);
    }


    @Override
    public int getPixel(int x, int y) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException("Pixel must be between (0,0) and (width-1, height-1) inclusive");
        double index = xyToIndex(x, y);
        return getValueAtIndex(index);
    }


    @Override
    public float[] getTextureBuffer() {
        float[] buffer = new float[width * height * 3];
        for (double i = 0; i < (width * height); i++) {
            int pixel = getValueAtIndex(i);
            buffer[(int)i*3] = ((float) (pixel >> 16 & 0xff) / 255f);
            buffer[(int)i*3+1] = ((float) (pixel >> 8 & 0xff) / 255f);
            buffer[(int)i*3+2] = ((float) (pixel & 0xff) / 255f);
        }
        return new float[0];
    }

    @Override
    public FrameBuffer copy() {
        Map<Object, Object> newMap = new HashMap<Object, Object>();
        for (double i = 0; i < width * height; i++) {
            if (map.containsKey(i))
                newMap.put(i, map.get(i));
        }
        return new MapFrameBuffer(width, height, newMap);
    }
    
    @Override
    protected void encodePixels(ByteBuf buf) {
        for (double i = 0; i < width*height; i++) {
            buf.writeInt(getValueAtIndex(i));
        }
    }

    @Override
    protected void decodePixels(ByteBuf buf) {
        for (double i = 0; i < width*height; i++) {
            map.put(i, buf.readInt());
        }
    }

    @Override
    public Map<?, ?> getTable() {
        Map<Object, Object> newMap = new HashMap<Object, Object>();
        for (double i = 0; i < width * height; i++) {
            if (map.containsKey(i))
                newMap.put(i, map.get(i));
        }
        newMap.put("width", width);
        newMap.put("height", height);
        return newMap;
    }
}
