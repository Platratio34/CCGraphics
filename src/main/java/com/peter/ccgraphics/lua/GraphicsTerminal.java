package com.peter.ccgraphics.lua;

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
        super((pixelWidth - 1) / CHAR_WIDTH, (pixelHeight - 1) / CHAR_HEIGHT, true);
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

                int[] cArr = font.getChar(c);
                int cWidth = font.getWidth(c);
                for (int x = 0; x < cWidth; x++) {
                    for (int y = 0; y < font.charHeight; y++) {
                        if (cArr[x = (y * cWidth)] != 0) {
                            frame.setPixel(sX + x, sY + y, tColor);
                        }
                    }
                    if (cursorVisible && cursorBlink && cursorX == col && cursorY == row) {
                        frame.setPixel(sX + x, sY + font.charHeight, cursorColor);
                    }
                }
            }
        }

        return frame;
    }

    private int convertColor(char color) {
        return ColorHelper.convert(palette.getRenderColours(Integer.decode("" + color)));
    }
}
