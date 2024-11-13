package com.peter.ccgraphics.lua;

import java.util.Optional;

import org.joml.Vector2i;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.font.CharacterGlyph;
import com.peter.ccgraphics.font.FontLoader;
import com.peter.ccgraphics.font.LuaFont;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;

/**
 * Renderer for {@link LuaFont}. Rasterize text to a FrameBuffer
 */
public class LuaFontRenderer {

    private final LuaFont font;

    /**
     * Create a new font renderer
     * @param name Font name
     * @param size Font size (height)
     * @throws RuntimeException If the requested font could not be found
     */
    public LuaFontRenderer(String name, int size) {
        if (!FontLoader.hasFont(name, size))
            throw new RuntimeException(
                    "Could not make font renderer for " + name + " in size " + size + ", no such font");
        font = FontLoader.getFont(name, size);
    }
    
    /**
     * Rasterize the provided text to a frame buffer
     * @param text Text to rasterize (can contain new lines)
     * @param color Color of the text when rendered
     * @return Frame buffer of rasterized text
     */
    public FrameBuffer rasterize(String text, int color) {
        Vector2i size = getTextSize(text);

        FrameBuffer frame = new ArrayFrameBuffer(size.x, size.y);

        int yStart = 0;
        int xStart = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                xStart += font.hSpacing;
            } else if (c == '\t') {
                xStart += (font.hSpacing) * 4;
            } else if (c == '\n') {
                xStart = 0;
                yStart += font.vSpacing;
            } else if (c == '\r') {
                // pass
            } else {
                CharacterGlyph glyph = font.getChar(c).colored(color);
                frame.drawBufferMasked(xStart, yStart, glyph);
                if (font.isMono)
                    xStart += font.hSpacing;
                else
                    xStart += font.getWidth(c) + 1;
            }
        }

        return frame;
    }

    /**
     * Rasterize the provided text to a frame buffer. Text will be white
     * @param text Text to rasterize (can contain new lines)
     * @return Frame buffer of rasterized text
     */
    public FrameBuffer rasterize(String text) {
        return rasterize(text, 0xffffffff);
    }

    /**
     * Rasterize the provided text to a frame buffer
     * @param text Text to rasterize (can contain new lines)
     * @param color Color of the text when rendered
     * @return Frame buffer of rasterized text
     */
    @LuaFunction(value = "rasterize")
    public final FrameBuffer rasterizeLUA(String text, Optional<Integer> color) throws LuaException {
        try {
            if (!color.isPresent())
                return rasterize(text);
            return rasterize(text, color.get());
        } catch (Exception e) {
            CCGraphics.LOGGER.error("Exception in rasterizeing text:",e);
            throw new LuaException("Error rasterizeing text: " + e.getMessage());
        }
    }

    
    /**
     * Get the size of text if it were rasterized
     * @param text Text to calculate size of
     * @return Width and height of rasterized text &
     */
    public Vector2i getTextSize(String text) {
        int w = 0;
        int h = font.vSpacing;
        int cW = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                cW += font.hSpacing;
            } else if (c == '\t') {
                cW += (font.hSpacing) * 4;
            } else if (c == '\n') {
                w = (cW > w) ? cW : w;
                cW = 0;
                h += font.vSpacing;
            } else if (c == '\r') {
                // pass
            } else {
                if (font.isMono)
                    cW += font.hSpacing;
                else
                    cW += font.getWidth(c) + 1;
            }
        }
        w = (cW > w) ? cW : w;
        w -= 1;
        h -= 1;
        return new Vector2i(w, h);
    }
    
    /**
     * Get the size of text if it were rasterized
     * @param text Text to calculate size of
     * @return Width and height of rasterized text
     */
    @LuaFunction(value = "getTextSize")
    public final MethodResult getTextSizeLUA(String text) {
        Vector2i v = getTextSize(text);
        return MethodResult.of(v.x, v.y);
    }
}
