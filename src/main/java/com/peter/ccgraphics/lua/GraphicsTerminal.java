package com.peter.ccgraphics.lua;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.ColorHelper;
import com.peter.ccgraphics.font.CharacterGlyph;
import com.peter.ccgraphics.font.FontLoader;
import com.peter.ccgraphics.font.LuaFont;

import dan200.computercraft.core.terminal.Palette;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;

public class GraphicsTerminal extends Terminal {

    protected int pixelWidth;
    protected int pixelHeight;

    protected int size = 7;
    protected LuaFont font;

    protected int charWidth;
    protected int charHeight;

    public GraphicsTerminal(int pixelWidth, int pixelHeight) {
        super((pixelWidth - 2) / getFont(7).hSpacing, (pixelHeight - 2) / getFont(7).vSpacing, true);
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        getFont();
    }

    private static LuaFont getFont(int size) {
        return FontLoader.getFont("mono", size);
    }

    private LuaFont getFont() {
        if (font == null) {
            font = getFont(size);
        }
        charHeight = font.vSpacing;
        charWidth = font.hSpacing;
        return font;
    }

    public FrameBuffer renderToFrame(boolean cursorVisible) {
        FrameBuffer frame = new ArrayFrameBuffer(pixelWidth, pixelHeight);

        LuaFont font = getFont();

        int cursorColor = ColorHelper.convert(palette.getRenderColours(cursorColour));

        for (int row = 0; row < height; row++) {
            TextBuffer bColors = backgroundColour[row];
            TextBuffer tColors = textColour[row];
            TextBuffer line = text[row];

            for (int col = 0; col < width; col++) {
                int sX = 1 + (col * charWidth);
                int sY = 1 + (row * charHeight);
                frame.drawBoxFilled(sX, sY, charWidth, charHeight, convertColor(bColors.charAt(col)));

                int tColor = convertColor(tColors.charAt(col));
                char c = line.charAt(col);

                if (c != ' ') {
                    CharacterGlyph glyph = font.getChar(c).colored(tColor);
                    frame.drawBufferMasked(sX, sY, glyph);
                }
                if (cursorVisible && cursorBlink && cursorX == col && cursorY == row) {
                    for (int x = 0; x < font.charWidth; x++) {
                        frame.setPixel(sX + x, sY + font.charHeight, cursorColor);
                    }
                }
            }
        }

        return frame;
    }

    public static FrameBuffer renderToFrame(boolean cursorVisible, Terminal terminal) {
        LuaFont font = FontLoader.getFont("mono", 7);
        int rWidth = 2 + (font.hSpacing * terminal.getWidth());
        int rHight = 2 + (font.vSpacing * terminal.getHeight());
        return renderToFrame(cursorVisible, new ArrayFrameBuffer(rWidth, rHight), terminal);
    }

    public static FrameBuffer renderToFrame(boolean cursorVisible, FrameBuffer outFrame, Terminal terminal) {
        LuaFont font = FontLoader.getFont("mono", 7);

        int charHeight = font.vSpacing;
        int charWidth = font.hSpacing;

        int width = terminal.getWidth();
        int height = terminal.getHeight();

        int rWidth = 2 + (charWidth * width);
        int rHight = 2 + (charHeight * height);

        if (outFrame.width < rWidth || outFrame.height < rHight) {
            throw new IllegalArgumentException("Input frame must be at least ("+rWidth+", "+rHight+")");
        }

        outFrame.drawBox(0, 0, outFrame.width, outFrame.height, 0xff000000);


        Palette palette = terminal.getPalette();

        int cursorColor = convertColor(terminal.getTextColour(), palette);

        for (int row = 0; row < height; row++) {
            TextBuffer bColors = terminal.getBackgroundColourLine(row);
            TextBuffer tColors = terminal.getTextColourLine(row);
            TextBuffer line = terminal.getLine(row);

            for (int col = 0; col < width; col++) {
                int sX = 1 + (col * charWidth);
                int sY = 1 + (row * charHeight);
                outFrame.drawBoxFilled(sX, sY, charWidth, charHeight, convertColor(bColors.charAt(col), palette));

                int tColor = convertColor(tColors.charAt(col), palette);
                char c = line.charAt(col);

                if (c != ' ') {
                    CharacterGlyph glyph = font.getChar(c).colored(tColor);
                    outFrame.drawBufferMasked(sX, sY, glyph);
                }
                if (cursorVisible && terminal.getCursorBlink() && terminal.getCursorX() == col && terminal.getCursorY() == row) {
                    for (int x = 0; x < font.charWidth; x++) {
                        outFrame.setPixel(sX + x, sY + font.charHeight, cursorColor);
                    }
                }
            }
        }

        return outFrame;
    }
    
    public void setTextSize(int size) {
        if (size == this.size)
            return;
        this.size = size;
        font = FontLoader.getFont("mono", size);
        charHeight = font.hSpacing;
        charWidth = font.vSpacing;
        super.resize((pixelWidth - 2) / charWidth, (pixelHeight - 2) / charHeight);
    }

    private int convertColor(char color) {
        return convertColor(color, palette);
    }

    protected static int convertColor(char color, Palette palette) {
        int cInt = 0;
        switch (color) {
            case '0':
                cInt = 0x0;
                break;
            case '1':
                cInt = 0x1;
                break;
            case '2':
                cInt = 0x2;
                break;
            case '3':
                cInt = 0x3;
                break;
            case '4':
                cInt = 0x4;
                break;
            case '5':
                cInt = 0x5;
                break;
            case '6':
                cInt = 0x6;
                break;
            case '7':
                cInt = 0x7;
                break;
            case '8':
                cInt = 0x8;
                break;
            case '9':
                cInt = 0x9;
                break;
            case 'a':
                cInt = 0xa;
                break;
            case 'b':
                cInt = 0xb;
                break;
            case 'c':
                cInt = 0xc;
                break;
            case 'd':
                cInt = 0xd;
                break;
            case 'e':
                cInt = 0xe;
                break;
            case 'f':
                cInt = 0xf;
                break;

            default:
                CCGraphics.LOGGER.error("Unknown color: {}", color);
                break;
        }

        return ColorHelper.convert(palette.getRenderColours(15 - cInt));
    }

    protected static int convertColor(int color, Palette palette) {
        
        return ColorHelper.convert( palette.getRenderColours(15-color) );
    }

    @Override
    public synchronized void resize(int pixelWidth, int pixelHeight) {
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        super.resize((pixelWidth - 2) / charWidth, (pixelHeight - 2) / charHeight);
    }

    public void checkSize(int pixelWidth, int pixelHeight) {
        if (this.pixelWidth != pixelWidth || this.pixelHeight != pixelHeight) {
            resize(pixelWidth, pixelHeight);
        }
    }
}
