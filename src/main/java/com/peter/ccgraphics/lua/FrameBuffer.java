package com.peter.ccgraphics.lua;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.data.FrameBufferBinary;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

/**
 * A custom representation of a frame buffer for rendering monitors or computers.<br/>
 * <br/>
 * There are 2 main implementations:
 * <ul>
 *  <li> {@link ArrayFrameBuffer} - Array backed Frame Buffer. More performant. </li>
 *  <li> {@link MapFrameBuffer} - Map backed Frame Buffer. Better for converting from LUA tables. </li>
 * </ul>
 */
public abstract class FrameBuffer extends CustomLuaObject {

    protected static final String LUA_TYPE_NAME = "frame_buffer_java";

    @Override
    public String getLuaTypeName() {
        return LUA_TYPE_NAME;
    }

    /**
     * Packet codec for encoding & decoding for networking
     */
    public static final PacketCodec<ByteBuf, FrameBuffer> PACKET_CODEC = PacketCodec.of(
            FrameBuffer::encode,
            FrameBuffer::decode);

    /**
     * The width of the frame stored in the buffer
     */
    protected final int width;
    /**
     * The height of the frame stored in the buffer
     */
    protected final int height;

    /**
     * Create a new Frame buffer
     * @param width Width of the frame
     * @param height Height of the frame
     */
    public FrameBuffer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Set the color of a given pixel
     * 
     * @param x     X position of the pixel
     * @param y     Y position of the pixel
     * @param color Color of the pixel. ARGB 8b each
     */
    public abstract void setPixel(int x, int y, int color);

    /**
     * Set the color of a pixel
     * 
     * @param x X position of the pixel
     * @param y Y position of the pixel
     * @return Color of the pixel. ARGB 8b each
     * @throws LuaException If the location is outside the frame
     */
    @LuaFunction(value = "setPixel")
    public final void setPixelLUA(double x, double y, int color) throws LuaException {
        int xI = convertDouble(x);
        int yI = convertDouble(y);
        try {
            setPixel(xI, yI, color);
        } catch (IndexOutOfBoundsException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Get the color of a pixel
     * 
     * @param x X position of the pixel
     * @param y Y position of the pixel
     * @return Color of the pixel. ARGB 8b each
     * @throws ArrayIndexOutOfBoundsException If the location is outside the frame
     */
    public abstract int getPixel(int x, int y);

    /**
     * Get an RGB byte buffer representing this frame buffer
     * 
     * @return RGB buffer of this frame
     * @throws ArrayIndexOutOfBoundsException If the location is outside the frame
     */
    @LuaFunction(value = "getPixel")
    public final int getPixelLua(double x, double y) throws LuaException {
        int xI = convertDouble(x);
        int yI = convertDouble(y);
        try {
            return getPixel(xI, yI);
        } catch(IndexOutOfBoundsException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Draw a box outline
     * 
     * @param x     X position of the box
     * @param y     Y position of the box
     * @param w     Width of the box
     * @param h     Height of the box
     * @param color Color of the outline. ARGB 8b each
     * @throws ArrayIndexOutOfBoundsException If the box extends outside the frame
     */
    public void drawBox(int x, int y, int w, int h, int color) {
        assertInFrame(x, y);
        if (x + w > width || y + h > height)
            throw new ArrayIndexOutOfBoundsException("(w, h) must be between (0, 0) & (width-x, height-y) inclusive, was ("+w+","+h+")");

        if (w == 0 || h == 0)
            return;
        if (w == 1 && h == 1) {
            setPixel(x, y, color);
        } else if (w == 1) {
            for (int pY = y; pY < y + h; pY++) {
                setPixel(x, pY, color);
            }
        } else if (h == 1) {
            for (int pX = x; pX < x + w; pX++) {
                setPixel(pX, y, color);
            }
        } else {
            for (int pX = x; pX < x + w; pX++) {
                setPixel(pX, y, color);
                setPixel(pX, y + h - 1, color);
            }
            for (int pY = y + 1; pY < y + h - 1; pY++) {
                setPixel(x, pY, color);
                setPixel(x + w - 1, pY, color);
            }
        }
    }

    /**
     * Draw a box outline
     * 
     * @param x     X position of the box
     * @param y     Y position of the box
     * @param w     Width of the box
     * @param h     Height of the box
     * @param color Color of the outline. ARGB 8b each
     * @throws LuaException If the box extends outside of the frame
     */
    @LuaFunction(value = "drawBox")
    public final void drawBoxLUA(double x, double y, double w, double h, int color) throws LuaException {
        try {
            drawBox(convertDouble(x), convertDouble(y), convertDouble(w), convertDouble(h), color);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Draw a filled box
     * 
     * @param x     X position of the box
     * @param y     Y position of the box
     * @param w     Width of the box
     * @param h     Height of the box
     * @param color Color of the box. ARGB 8b each
     * @throws ArrayIndexOutOfBoundsException If the box extends outside the frame
     */
    public void drawBoxFilled(int x, int y, int w, int h, int color) {
        assertInFrame(x, y);
        if (x + w > width || y + h > height)
            throw new ArrayIndexOutOfBoundsException(
                    "(w, h) must be between (0, 0) & (width-x, height-y) inclusive, was (" + w + "," + h + "); x,y=("+x+","+y+") ("+(width-x)+","+(height-y)+")");

        if (w == 0 || h == 0)
            return;
        if (w == 1 && h == 1) {
            setPixel(x, y, color);
            return;
        }
        
        for (int pX = x; pX < x + w; pX++) {
            for (int pY = y; pY < y + h; pY++) {
                setPixel(pX, pY, color);
            }
        }
    }

    /**
     * Draw a filled box
     * 
     * @param x     X position of the box
     * @param y     Y position of the box
     * @param w     Width of the box
     * @param h     Height of the box
     * @param color Color of the box. ARGB 8b each
     * @throws LuaException If the box extends outside of the frame
     */
    @LuaFunction(value = "drawBoxFilled")
    public final void drawBoxFilledLUA(double x, double y, double w, double h, int color) throws LuaException {
        try {
            drawBoxFilled(convertDouble(x), convertDouble(y), convertDouble(w), convertDouble(h), color);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Draw a line on this buffer
     * 
     * @param x1    X position of the start of the line
     * @param y1    Y position of the start of the line
     * @param x2    X position of the end of the line
     * @param y2    Y position of the end of the line
     * @param color Color of the line. ARGB 8b each
     * @throws ArrayIndexOutOfBoundsException If the line extends outside the frame
     */
    public void drawLine(int x1, int y1, int x2, int y2, int color) {
        if (x1 < 0 || x1 >= width || y1 < 0 || y1 >= height)
            throw new ArrayIndexOutOfBoundsException("(x1, y1) must be between (0,0) & (width-1, height-1)");
        if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height)
            throw new ArrayIndexOutOfBoundsException("(x2, y2) must be between (0,0) & (width-1, height-1)");

        if (x1 == x2 && y1 == y2) {
            setPixel(x1, y1, color);
            return;
        }

        int minX = x1;
        int minY = y1;
        int maxX = x2;
        int maxY = y2;
        if (x2 < x1) {
            maxX = x1;
            minX = x2;
            maxY = y2;
            minY = y1;
        }
        if (minX >= width || maxX < 0)
            return;

        int diffX = maxX - minX;
        int diffY = maxY - minY;

        if (diffX > Math.abs(diffY)) {
            float y = minY;
            float dy = (float) diffY / (float) diffX;

            for (int x = minX; x <= maxX; x++) {
                setPixel(x, Math.round(y), color);
                y += dy;
            }
        } else {
            float x = minX;
            float dx = (float) diffX / (float) diffY;
            if (maxY >= minY) {
                for (int y = minY; y <= maxY; y++) {
                    setPixel(Math.round(x), y, color);
                    x += dx;
                }
            } else {
                for (int y = minY; y >= maxY; y--) {
                    setPixel(Math.round(x), y, color);
                    x += dx;
                }
            }
        }
    }

    /**
     * Draw a line on this buffer
     * 
     * @param x1    X position of the start of the line
     * @param y1    Y position of the start of the line
     * @param x2    X position of the end of the line
     * @param y2    Y position of the end of the line
     * @param color Color of the line. ARGB 8b each
     * @throws LuaException If the line extends outside the frame
     */
    @LuaFunction(value = "drawLine")
    public final void drawLineLUA(double x1, double y1, double x2, double y2, int color) throws LuaException {
        try {
            drawLine(convertDouble(x1), convertDouble(y1), convertDouble(x2), convertDouble(y2), color);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Draw the provided buffer onto this buffer
     * 
     * @param x       X position on THIS buffer to start drawing
     * @param y       Y position on THIS buffer to start drawing
     * @param buffer2 Buffer to draw
     * @param xOff    X position on <code>buffer2</code> to start drawing from
     * @param yOff    Y position on <code>buffer2</code> to start drawing from
     * @param w       Width of <code>buffer2</code> to draw from <code>xOff</code>
     * @param h       Height of <code>buffer2</code> to draw from <code>yOff</code>
     * @throws ArrayIndexOutOfBoundsException If the draw area extends outside the
     *                                        frame
     */
    public void drawBuffer(int x, int y, FrameBuffer buffer2, int xOff, int yOff, int w, int h) {
        assertInFrame(x, y);
        if (xOff < 0 || yOff < 0 || xOff >= buffer2.width || yOff >= buffer2.width)
            throw new ArrayIndexOutOfBoundsException(
                    "Point (xOff, yOff) must be between (0, 0) and (buffer2.width-1, buffer2.height-1)");
        if (w < 0 || xOff + w > buffer2.width)
            throw new ArrayIndexOutOfBoundsException("Width must be between 0 and buffer2.width-xOff");
        if (h < 0 || yOff + h > buffer2.height)
            throw new ArrayIndexOutOfBoundsException("Height must be between 0 and buffer2.height-yOff");

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                setPixel(x + i, y + j, buffer2.getPixel(i + xOff, j + yOff));
            }
        }
    }

    /**
     * Draw the provided buffer onto this buffer.
     * 
     * @param x       X position on THIS buffer to start drawing
     * @param y       Y position on THIS buffer to start drawing
     * @param buffer2 Buffer to draw
     * @param xOff    <i>(Optional)</i> X position on <code>buffer2</code> to start drawing from. Defaults to <code>0</code>
     * @param yOff    <i>(Optional)</i> Y position on <code>buffer2</code> to start drawing from. Defaults to <code>0</code>
     * @param w       <i>(Optional)</i> Width of <code>buffer2</code> to draw from <code>xOff</code>. Defaults to <code>buffer2.getWidth()</code>
     * @param h       <i>(Optional)</i> Height of <code>buffer2</code> to draw from <code>yOff</code>. Defaults to <code>buffer2.getHeight()</code>
     * @throws LuaException If the draw area extends outside the frame OR the frame was an invalid frame buffer
     */
    @LuaFunction(value = "drawBuffer")
    public final void drawBufferLUA(IArguments arguments) throws LuaException {
        try {
            Object arg2 = arguments.get(2);
            FrameBuffer buffer2;
            if (arg2 instanceof FrameBuffer frameBuffer) {
                buffer2 = frameBuffer;
            } else {
                buffer2 = fromTableLUA(arguments.getTable(2));
            }
            int xOff = convertDouble(arguments.optDouble(3, 0));
            int yOff = convertDouble(arguments.optDouble(4, 0));
            int w = convertDouble(arguments.optDouble(5, buffer2.width - xOff));
            int h = convertDouble(arguments.optDouble(6, buffer2.height - yOff));
            drawBuffer(convertDouble(arguments.getDouble(0)), convertDouble(arguments.getDouble(1)), buffer2, xOff, yOff,
                    w, h);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Draw the provided buffer onto this buffer. Pixels with an Alpha of 0 will not
     * be drawn onto this buffer.
     * 
     * @param x       X position on THIS buffer to start drawing
     * @param y       Y position on THIS buffer to start drawing
     * @param buffer2 Buffer to draw
     * @param xOff    X position on <code>buffer2</code> to start drawing from
     * @param yOff    Y position on <code>buffer2</code> to start drawing from
     * @param w       Width of <code>buffer2</code> to draw from <code>xOff</code>
     * @param h       Height of <code>buffer2</code> to draw from <code>yOff</code>
     * @throws ArrayIndexOutOfBoundsException If the draw area extends outside the
     *                                        frame
     */
    public void drawBufferMasked(int x, int y, FrameBuffer buffer2, int xOff, int yOff, int w, int h) {
        assertInFrame(x, y);
        if (xOff < 0 || yOff < 0 || xOff >= buffer2.width || yOff >= buffer2.height)
            throw new ArrayIndexOutOfBoundsException(
                    "Point (xOff, yOff) must be between (0, 0) and (buffer2.width-1, buffer2.height-1)");
        if (w < 0 || xOff + w > buffer2.width)
            throw new ArrayIndexOutOfBoundsException("Width must be between 0 and buffer2.width-xOff");
        if (h < 0 || yOff + h > buffer2.height)
            throw new ArrayIndexOutOfBoundsException("Height must be between 0 and buffer2.height-yOff");

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = buffer2.getPixel(i + xOff, j + yOff);
                if ((color >> 24 & 0xff) > 0x00)
                    setPixel(x + i, y + j, color);
            }
        }
    }
    
    /**
     * Draw the provided buffer onto this buffer. Pixels with an Alpha of 0 will not
     * be drawn onto this buffer.
     * 
     * @param x       X position on THIS buffer to start drawing
     * @param y       Y position on THIS buffer to start drawing
     * @param buffer2 Buffer to draw
     * @param xOff    X position on <code>buffer2</code> to start drawing from
     * @param yOff    Y position on <code>buffer2</code> to start drawing from
     * @throws ArrayIndexOutOfBoundsException If the draw area extends outside the
     *                                        frame
     */
    public void drawBufferMasked(int x, int y, FrameBuffer buffer2, int xOff, int yOff) {
        drawBufferMasked(x, y, buffer2, xOff, yOff, buffer2.width - xOff, buffer2.height - yOff);
    }

    /**
     * Draw the provided buffer onto this buffer. Pixels with an Alpha of 0 will not
     * be drawn onto this buffer.
     * 
     * @param x       X position on THIS buffer to start drawing
     * @param y       Y position on THIS buffer to start drawing
     * @param buffer2 Buffer to draw
     * @throws ArrayIndexOutOfBoundsException If the draw area extends outside the
     *                                        frame
     */
    public void drawBufferMasked(int x, int y, FrameBuffer buffer2) {
        drawBufferMasked(x, y, buffer2, 0, 0, buffer2.width, buffer2.height);
    }

    /**
     * Draw the provided buffer onto this buffer. Pixels with an Alpha of 0 will not
     * be drawn onto this buffer.
     * 
     * @param x       X position on THIS buffer to start drawing
     * @param y       Y position on THIS buffer to start drawing
     * @param buffer2 Buffer to draw
     * @param xOff    <i>(Optional)</i> X position on <code>buffer2</code> to start drawing from. Defaults to <code>0</code>
     * @param yOff    <i>(Optional)</i> Y position on <code>buffer2</code> to start drawing from. Defaults to <code>0</code>
     * @param w       <i>(Optional)</i> Width of <code>buffer2</code> to draw from <code>xOff</code>. Defaults to <code>buffer2.getWidth() - xOff</code>
     * @param h       <i>(Optional)</i> Height of <code>buffer2</code> to draw from <code>yOff</code>. Defaults to <code>buffer2.getHeight() - yOff</code>
     * @throws LuaException If the draw area extends outside the frame OR the frame was an invalid frame buffer
     */
    @LuaFunction(value = "drawBufferMasked")
    public final void drawBufferMaskedLUA(IArguments arguments) throws LuaException {
        try {
            Object arg2 = arguments.get(2);
            FrameBuffer buffer2;
            if (arg2 instanceof FrameBuffer frameBuffer) {
                buffer2 = frameBuffer;
            } else {
                buffer2 = fromTableLUA(arguments.getTable(2));
            }
            int xOff = convertDouble(arguments.optDouble(3, 0));
            int yOff = convertDouble(arguments.optDouble(4, 0));
            int w = convertDouble(arguments.optDouble(5, buffer2.width - xOff));
            int h = convertDouble(arguments.optDouble(6, buffer2.height - yOff));
            drawBufferMasked(convertDouble(arguments.getDouble(0)), convertDouble(arguments.getDouble(1)), buffer2, xOff,
                    yOff, w, h);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Get the width of the frame
     * 
     * @return Width of the frame
     */
    @LuaFunction
    public final int getWidth() {
        return width;
    }

    /**
     * Get the height of the frame
     * 
     * @return Height of the frame
     */
    @LuaFunction
    public final int getHeight() {
        return height;
    }

    /**
     * Make a copy of the frame using the same backing
     * 
     * @return Deep copy of this frame
     */
    public abstract FrameBuffer copy();

    /**
     * Make a copy of the frame using the same backing
     * 
     * @return Deep copy of this frame
     */
    @LuaFunction("copy")
    public final FrameBuffer copyLUA() {
        return copy();
    }

    /**
     * Check if the given position is inside the frame
     * 
     * @param x X position to check
     * @param y Y position to check
     * @return If the position was inside the frame
     */
    public boolean inFrame(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Check if the given position is inside the frame
     * 
     * @param x X position to check
     * @param y Y position to check
     * @return If the position was inside the frame
     */
    public final boolean inFrameLUA(double x, double y) {
        return inFrame(convertDouble(x), convertDouble(y));
    }

    /**
     * Check if the given position is inside the frame
     * 
     * @param x X position to check
     * @param y Y position to check
     * @return If the position was inside the frame
     * @throws ArrayIndexOutOfBoundsException If the point was outside the frame
     */
    public void assertInFrame(int x, int y) throws ArrayIndexOutOfBoundsException {
        if (!inFrame(x, y)) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "Point must be between (0,0) and (width-1, height-1) inclusive; (0,0) <= (%d,%d) <= (%d,%d)",x,y,width,height));
        }
    }

    /**
     * Convert a double to an integer by flooring it
     * 
     * @param v Value to convert
     * @return Value as a floored integer
     */
    protected static int convertDouble(double v) {
        return (int) Math.floor(v);
    }

    private void encode(ByteBuf buf) {
        // buf.writeInt(width);
        // buf.writeInt(height);
        // encodePixels(buf);
        FrameBufferBinary.Encoder encoder = new FrameBufferBinary.Encoder(this);
        encoder.setOpaque(true);
        encoder.tryRLE();
        encoder.tryIndexed();
        buf.writeBytes(encoder.encode());
    }

    /**
     * Encode the pixels of the buffer for network transmission.<br/>
     * <br/>
     * Pixels should be encodes in 4byte ARGB in order.<br/>
     * <br/>
     * Creates a {@link ArrayFrameBuffer}
     * 
     * @param buf Buffer to put the pixels into.
     */
    protected abstract void encodePixels(ByteBuf buf);

    private static FrameBuffer decode(ByteBuf buf) {
        FrameBufferBinary.Decoder decoder = new FrameBufferBinary.Decoder();
        // CCGraphics.LOGGER.info("Decoding frame buffer");
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        try {
            FrameBuffer frame = decoder.decode(bytes);
            int endOfFrame = (buf.readerIndex() + (decoder.usedBytes()));
            buf.readerIndex(endOfFrame);
            return frame;
        } catch (IOException e) {
            CCGraphics.LOGGER.error("Unable to decode frame buffer", e);
            return null;
        }
    }

    /**
     * Decode pixels from a byte buffer from network transmission.<br/>
     * <br/>
     * Pixels are encoded in 4byte ARGB in order.
     * 
     * @param buf Buffer to read pixels from
     */
    protected abstract void decodePixels(ByteBuf buf);

    /**
     * Get an RGB byte buffer representing this frame buffer for making a texture
     * 
     * @return RGB buffer of this frame
     */
    public abstract float[] getTextureBuffer();

    /**
     * Get a map that represents this frame buffer for passing to LUA. <br/>
     * <br/>
     * Contains the keys "width", "height", and a key for every pixel by index.
     * 
     * @return Map representing this frame buffer
     */
    public abstract Map<?, ?> getTable();

    /**
     * Get a map that represents this frame buffer for passing to LUA. <br/>
     * <br/>
     * Contains the keys "width", "height", and a key for every pixel by index.
     * 
     * @return Map representing this frame buffer
     */
    @LuaFunction(value = "getTable")
    public final Map<?, ?> getTableLUA() {
        return getTable();
    }

    /**
     * Make a new {@link MapFrameBuffer} from a LUA table
     * 
     * @param map LUA table containing the buffer data and size
     * @return New Frame Buffer from the LUA table
     * @throws NoSuchElementException Thrown if keys are missing from the table
     */
    public static FrameBuffer fromTable(Map<?, ?> map) {
        return new MapFrameBuffer(map);
    }

    /**
     * Make a new {@link MapFrameBuffer} from a LUA table.<br/></br>
     * 
     * @param map LUA table containing the buffer data and size
     * @return New Frame Buffer from the LUA table
     * @throws LuaException Thrown if keys are missing from the table
     */
    @LuaFunction(value = "fromTable")
    public final FrameBuffer fromTableLUA(Map<?, ?> map) throws LuaException {
        MapFrameBuffer.verifyMapLUA(map);
        return fromTable(map);
    }

    public void debugPrint() {
        for (int y = 0; y < height; y++) {
            String l = "";
            for (int x = 0; x < width; x++) {

                l += " " + Integer.toHexString(getPixel(x, y) & 0x00ffffff);
            }
            System.out.println(l);
        }
    }

    /**
     * Gets the total number of pixels in the frame buffer
     * @return total pixels
     */
    public int getLength() {
        return width * height;
    }

    public abstract int getColorIndexed(int index);

    public abstract void setColorIndexed(int index, int color);

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof FrameBuffer) )
            return false;
        FrameBuffer other = (FrameBuffer) obj;
        if (width != other.width || height != other.height)
            return false;
        for (int i = 0; i < width * height; i++) {
            if (getColorIndexed(i) != other.getColorIndexed(i))
                return false;
        }
        return true;
    }
}
