package com.peter.ccgraphics.font;

import java.util.HashMap;
import java.util.Map;

import com.peter.ccgraphics.font.LuaFont.CharData;
import com.peter.ccgraphics.lua.FrameBuffer;

import dan200.computercraft.api.lua.LuaFunction;
import io.netty.buffer.ByteBuf;

public class CharacterGlyph extends FrameBuffer {

    private final LuaFont font;
    private final CharData data;

    private final byte[] glyph;
    protected final int color;

    protected CharacterGlyph(LuaFont font, CharData data) {
        super(data.width, data.height);
        this.font = font;
        this.data = data;
        glyph = new byte[Math.ceilDiv(width * height, 8)];
        this.color = 0xffffffff;
    }

    private CharacterGlyph(byte[] glyph, LuaFont font, CharData data, int color) {
        super(data.width, data.height);
        this.font = font;
        this.data = data;
        this.glyph = glyph;
        this.color = color;
    }

    @Override
    public void setPixel(int x, int y, int color) {
        throw new UnsupportedOperationException("CharacterGlyph is read-only");
    }

    @Override
    public void setColorIndexed(int index, int color) {
        throw new UnsupportedOperationException("CharacterGlyph is read-only");
    }

    protected void setPixel(int x, int y) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException(
                    "Pixel must be between (0,0) and (width-1, height-1) inclusive, was (" + x + "," + y + ")");
        int pI = x + (y * width);
        int bI = pI / 8;
        glyph[bI] |= (byte) (0b1 << (pI % 8));
    }

    @Override
    public int getPixel(int x, int y) {
        if (!inFrame(x, y))
            throw new ArrayIndexOutOfBoundsException(
                    "Pixel must be between (0,0) and (width-1, height-1) inclusive, was (" + x + "," + y + ")");
        return isFilled(x + (y * width)) ? color : 0x00000000;
    }
    
    @Override
    public int getColorIndexed(int index) {
        return isFilled(index) ? color : 0x00000000;
    }

    private boolean isFilled(int pI) {
        int bI = pI / 8;
        byte mask = (byte) (0b1 << (pI % 8));
        return (glyph[bI] & mask) != 0;
    }

    @Override
    public CharacterGlyph copy() {
        return new CharacterGlyph(glyph, font, data, color);
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
        HashMap<Object, Object> map = new HashMap<>();
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
    public final CharacterGlyph colored(int color) {
        return new CharacterGlyph(glyph, font, data, color);
    }

}
