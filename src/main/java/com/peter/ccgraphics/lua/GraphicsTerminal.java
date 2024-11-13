package com.peter.ccgraphics.lua;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.ColorHelper;
import com.peter.ccgraphics.font.CharacterGlyph;
import com.peter.ccgraphics.font.FontLoader;
import com.peter.ccgraphics.font.LuaFont;

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
        super((pixelWidth - 2) / getCharWidth(7), (pixelHeight - 2) / getCharHeight(7), true);
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        getFont();
    }

    private static LuaFont getFont(int size) {
        return FontLoader.getFont("mono", size);
    }

    private static int getCharWidth(int size) {
        return getFont(size).charWidth + 1;
    }
    private static int getCharHeight(int size) {
        return getFont(size).charHeight + 2;
    }

    private LuaFont getFont() {
        if (font == null) {
            font = FontLoader.getFont("mono", size);
        }
        charHeight = font.charHeight + 2;
        charWidth = font.charWidth + 1;
        return FontLoader.getFont("mono", size);
    }

    public FrameBuffer renderToFrame(boolean cursorVisible) {
        FrameBuffer frame = new ArrayFrameBuffer(pixelWidth, pixelHeight);
        // frame.drawBoxFilled(0,0,frame.getWidth(),frame.getHeight(), )

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
        int cInt = 0;
        switch (color) {
            case '0':
                cInt = 0x0; break;
            case '1':
                cInt = 0x1; break;
            case '2':
                cInt = 0x2; break;
            case '3':
                cInt = 0x3; break;
            case '4':
                cInt = 0x4; break;
            case '5':
                cInt = 0x5; break;
            case '6':
                cInt = 0x6; break;
            case '7':
                cInt = 0x7; break;
            case '8':
                cInt = 0x8; break;
            case '9':
                cInt = 0x9; break;
            case 'a':
                cInt = 0xa; break;
            case 'b':
                cInt = 0xb; break;
            case 'c':
                cInt = 0xc; break;
            case 'd':
                cInt = 0xd; break;
            case 'e':
                cInt = 0xe; break;
            case 'f':
                cInt = 0xf; break;
        
            default:
                CCGraphics.LOGGER.error("Unknown color: {}", color);
                break;
        }
        
        return ColorHelper.convert( palette.getRenderColours(15-cInt) );
    }

    @Override
    public synchronized void resize(int width, int height) {
        this.pixelWidth = width;
        this.pixelHeight = height;
        super.resize((pixelWidth - 2) / charWidth, (pixelHeight - 2) / charHeight);
    }

    public void checkSize(int pixelWidth, int pixelHeight) {
        if (this.pixelWidth != pixelWidth || this.pixelHeight != pixelHeight) {
            resize(pixelWidth, pixelHeight);
        }
    }
}
