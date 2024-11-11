package com.peter.ccgraphics.lua;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.ColorHelper;
import com.peter.ccgraphics.data.FontLoader;
import com.peter.ccgraphics.data.LuaFont;
import com.peter.ccgraphics.monitor.ArrayFrameBuffer;
import com.peter.ccgraphics.monitor.FrameBuffer;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;

public class GraphicsTerminal extends Terminal {

    private static final int CHAR_WIDTH = 6;
    private static final int CHAR_HEIGHT = 9;

    protected int pixelWidth;
    protected int pixelHeight;

    public GraphicsTerminal(int pixelWidth, int pixelHeight) {
        super((pixelWidth - 2) / CHAR_WIDTH, (pixelHeight - 2) / CHAR_HEIGHT, true);
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
    }

    @LuaFunction
    public FrameBuffer renderToFrame(boolean cursorVisible) {
        FrameBuffer frame = new ArrayFrameBuffer(pixelWidth, pixelHeight);
        // frame.drawBoxFilled(0,0,frame.getWidth(),frame.getHeight(), )

        LuaFont font = FontLoader.getFont("mono", 7);

        int cursorColor = ColorHelper.convert(palette.getRenderColours(cursorColour));

        for (int row = 0; row < height; row++) {
            TextBuffer bColors = backgroundColour[row];
            TextBuffer tColors = textColour[row];
            TextBuffer line = text[row];
            for (int col = 0; col < width; col++) {
                int sX = 1 + (col * CHAR_WIDTH);
                int sY = 1 + (row * CHAR_HEIGHT);
                frame.drawBoxFilled(sX, sY, CHAR_WIDTH, CHAR_HEIGHT, convertColor(bColors.charAt(col)));

                int tColor = convertColor(tColors.charAt(col));
                char c = line.charAt(col);

                
                if (c != ' ') {
                    int[] cArr = font.getChar(c);
                    int cWidth = font.getWidth(c);
                    for (int x = 0; x < cWidth; x++) {
                        for (int y = 0; y < font.charHeight; y++) {
                            if (cArr[x + (y * font.charWidth)] != 0) {
                                frame.setPixel(sX + x, sY + y, tColor);
                            }
                        }
                        if (cursorVisible && cursorBlink && cursorX == col && cursorY == row) {
                            frame.setPixel(sX + x, sY + font.charHeight, cursorColor);
                        }
                    }
                } else if (cursorVisible && cursorBlink && cursorX == col && cursorY == row) {
                    for (int x = 0; x < font.charWidth; x++) {
                        frame.setPixel(sX + x, sY + font.charHeight, cursorColor);
                    }
                }
            }
        }

        return frame;
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
        super.resize((pixelWidth - 2) / CHAR_WIDTH, (pixelHeight - 2) / CHAR_HEIGHT);
    }

    public void checkSize(int pixelWidth, int pixelHeight) {
        if (this.pixelWidth != pixelWidth || this.pixelHeight != pixelHeight) {
            resize(pixelWidth, pixelHeight);
        }
    }
}
