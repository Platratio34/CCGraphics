package com.peter.ccgraphics.font;

import java.util.HashMap;
import java.util.Map;

import com.peter.ccgraphics.font.LuaFont.CharData;
import com.peter.ccgraphics.monitor.FrameBuffer;

import dan200.computercraft.api.lua.LuaFunction;
import io.netty.buffer.ByteBuf;

public class CharacterGlyph extends FrameBuffer {

    private final LuaFont font;
    private final CharData data;

    private final byte[] glyph;
    protected int color = 0xffffffff;

    protected CharacterGlyph(LuaFont font, CharData data) {
        super(data.width, data.height);
        this.font = font;
        this.data = data;
        glyph = new byte[Math.ceilDiv(width * height, 8)];
    }
    
    private CharacterGlyph(byte[] glyph, LuaFont font, CharData data) {
        super(data.width, data.height);
        this.font = font;
        this.data = data;
        this.glyph = glyph;
    }

    @Override
    public void setPixel(int x, int y, int color) {
        throw new UnsupportedOperationException("CharacterGlyph is read-only");
    }

    protected void setPixel(int x, int y) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException(
                    "Pixel must be between (0,0) and (width-1, height-1) inclusive, was (" + x + "," + y + ")");
        int pI = x + (y * width);
        int bI = pI / 8;
        glyph[bI] |= (byte)(0b1 << (pI % 8));
    }

    @Override
    public int getPixel(int x, int y) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException(
                    "Pixel must be between (0,0) and (width-1, height-1) inclusive, was (" + x + "," + y + ")");
        return isFilled(x, y) ? color : 0x00000000;
    }

    private boolean isFilled(int x, int y) {
        int pI = x + (y * width);
        int bI = pI / 8;
        byte mask = (byte)(0b1 << (pI % 8));
        return (glyph[bI] & mask) != 0;
    }

    @Override
    public CharacterGlyph copy() {
        return new CharacterGlyph(glyph, font, data);
    }

    @Override
    protected void encodePixels(ByteBuf buf) {
        throw new UnsupportedOperationException("CharacterGlyph does not support encoding or decoding");
    }

    @Override
    protected void decodePixels(ByteBuf buf) {
        throw new UnsupportedOperationException("CharacterGlyph does not support encoding or decoding");
    }

    @Override
    public float[] getTextureBuffer() {
        throw new UnsupportedOperationException("CharacterGlyph does not support encoding or decoding");
    }

    @Override
    public Map<?, ?> getTable() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        map.put("width", width);
        map.put("height", height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map.put((double) (x + (y * height)), getPixel(x, y));
            }
        }
        return map;
    }

    @LuaFunction
    public final CharacterGlyph setColor(int color) {
        this.color = 0xff000000 | color;
        return this;
    }

}
