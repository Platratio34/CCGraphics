package com.peter.ccgraphics.lua;

import java.util.Map;
import javax.annotation.Nullable;

import com.peter.ccgraphics.monitor.ArrayFrameBuffer;
import com.peter.ccgraphics.monitor.MapFrameBuffer;
import com.peter.ccgraphics.monitor.FrameBuffer;

import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.ILuaAPIFactory;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;

/**
 * LUA API for handling various graphics classes
 */
public class GraphicsAPI implements ILuaAPI, ILuaAPIFactory {

    @Override
    public String[] getNames() {
        return new String[0];
    }

    @Override
    @Nullable
    public String getModuleName() {
        return "graphics";
    }

    @Override
    @Nullable
    public ILuaAPI create(IComputerSystem computer) {
        return new GraphicsAPI();
    }

    /**
     * Make a new Frame Buffer of the provided size
     * @param width Width of the frame
     * @param height Height of the frame
     * @return New frame buffer
     * @throws LuaException If <code>width</code> or <code>height</code> were equal to or less than <code>0</code>
     */
    @LuaFunction(value = "FrameBuffer")
    public final ArrayFrameBuffer newFrameBuffer(int width, int height) throws LuaException {
        if (width <= 0)
            throw new LuaException("Width must be greater than 0");
        if (height <= 0)
            throw new LuaException("Height must be greater than 0");
        return new ArrayFrameBuffer(width, height);
    }

    /**
     * Convert a LUA table to a frame buffer
     * @param table Table to convert
     * @return FrameBuffer created from the table
     * @throws LuaException If the table was missing required keys
     * @see {@link FrameBuffer#getTable()} for specifics on table format
     */
    @LuaFunction("tableToFrameBuffer")
    public final MapFrameBuffer tableToFrameBuffer(Map<?, ?> table) throws LuaException {
        MapFrameBuffer.verifyMapLUA(table);
        return new MapFrameBuffer(table);
    }

    /**
     * Pack Red, Green, & Blue into an integer for use with FrameBuffers. Sets Alpha to 0xff
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return Packed color
     * @throws LuaException If any of the components were outside the allowed range
     * @see {@link #unpackRGBA} to unpack colors into their components 
     */
    @LuaFunction(value = "packRGB")
    public final int packRGB(int r, int g, int b) throws LuaException {
        if (r < 0 || r > 0xff)
            throw new LuaException("`r` must be between 0 - 255 inclusive");
        if (g < 0 || g > 0xff)
            throw new LuaException("`g` must be between 0 - 255 inclusive");
        if (b < 0 || b > 0xff)
            throw new LuaException("`b` must be between 0 - 255 inclusive");
        return 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    /**
     * Pack Red, Green, Blue & Alpha into an integer for use with FrameBuffers
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @param a Alpha component (0-255)
     * @return Packed color
     * @throws LuaException If any of the components were outside the allowed range
     * @see {@link #unpackRGBA} to unpack colors into their components
     */
    @LuaFunction(value = "packRGBA")
    public final int packRGBA(int r, int g, int b, int a) throws LuaException {
        if (r < 0 || r > 0xff)
            throw new LuaException("`r` must be between 0 - 255 inclusive");
        if (g < 0 || g > 0xff)
            throw new LuaException("`g` must be between 0 - 255 inclusive");
        if (b < 0 || b > 0xff)
            throw new LuaException("`b` must be between 0 - 255 inclusive");
        if (a < 0 || a > 0xff)
            throw new LuaException("`a` must be between 0 - 255 inclusive");
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    /**
     * Unpack a color number to it's components
     * @param color
     * @return Red, Green, Blue, Alpha as integers from 0 to 255
     * @see {@link #packRGB} & {@link #packRGBA} to pack colors from their components
     */
    @LuaFunction(value = "unpackRGBA")
    public final MethodResult unpackRGBA(int color) {
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;
        return MethodResult.of(r, g, b, a);
    }

    /**
     * Get a new text renderer
     * @param fontName Font name (Currently only supports <code>mono</code>)
     * @param fontSize Font size (height) (Currently only supports <code>7</code>)
     * @return A new Font Renderer for rasterize text to frame buffers.
     * @throws LuaException If the requested font could not be found.
     */
    @LuaFunction(value = "getTextRenderer")
    public final LuaFontRenderer getTextRenderer(String fontName, int fontSize) throws LuaException {
        try {
            return new LuaFontRenderer(fontName, fontSize);
        } catch (Exception e) {
            throw new LuaException(e.getMessage());
        }
        
    }

}
