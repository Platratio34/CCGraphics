package com.peter.ccgraphics.lua;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import javax.imageio.ImageIO;

import com.peter.ccgraphics.ColorHelper;
import com.peter.ccgraphics.data.FrameBufferBinary.Decoder;

import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;

/**
 * LUA API for handling various graphics classes
 */
public class GraphicsNativeAPI implements ILuaAPI {

    @SuppressWarnings("unused")
    private final IComputerSystem computer;
    
    private GraphicsNativeAPI(IComputerSystem computer) {
            this.computer = computer;
    }

    @Override
    public String[] getNames() {
        return new String[0];
    }

    @Override
    @Nullable
    public String getModuleName() {
        return "graphics_native";
    }

    @Nullable
    public static ILuaAPI create(IComputerSystem computer) {
        return new GraphicsNativeAPI(computer);
    }

    @Override
    public void startup() {
        // fileSystem = environment.getFileSystem();
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
        return ColorHelper.pack(r, g, b);
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
        return ColorHelper.pack(r, g, b, a);
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

    /**
     * Load an image to a frame buffer
     * @param image Image (as byte array)
     * @return Frame buffer created from the image
     * @throws LuaException If the image is invalid
     */
    public FrameBuffer loadImage(byte[] arr) throws LuaException {
        try (InputStream imgStream = new ByteArrayInputStream(arr)) {
            
            FrameBuffer frame;
            BufferedImage img = ImageIO.read(imgStream);
            if (img == null) {
                throw new LuaException("Invalid image");
            }
            frame = new ArrayFrameBuffer(img.getWidth(), img.getHeight());
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    frame.setPixel(x, y, img.getRGB(x, y));
                }
            }
            return frame;
        } catch (IOException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Load an image to a frame buffer
     * @param image Image (as byte array)
     * @return Frame buffer created from the image
     * @throws LuaException If the image is invalid
     */
    @LuaFunction
    public final FrameBuffer loadImage(Map<?, ?> image) throws LuaException {
        byte[] buff = new byte[image.size()];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) ((int) (double) (image.get((double) i + 1)) & 0xff);
        }
        return loadImage(buff);
    }

    /**
     * Load an image to a frame buffer
     * @param image Image (as byte string)
     * @return Frame buffer created from the image
     * @throws LuaException If the image is invalid
     */
    @LuaFunction
    public final FrameBuffer loadImageString(String image) throws LuaException {
        byte[] arr = new byte[image.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) image.charAt(i);
        }
        return loadImage(arr);
    }

    @LuaFunction
    public final FrameBuffer loadFBBString(String fbb) throws LuaException {
        Decoder decoder = new Decoder();
        byte[] arr = new byte[fbb.length()];
        for(int i = 0; i < fbb.length(); i++) {
            char c = fbb.charAt(i);
            arr[i] = (byte)c;
        }
        try {
            return decoder.decode(arr);
        } catch (IOException e) {
            throw new LuaException(e.getMessage());
        }
    }

}
