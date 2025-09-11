package com.peter.ccgraphics.rendering.gui;

import com.peter.ccgraphics.computer.GraphicsComputerMenu;
import com.peter.ccgraphics.lua.FrameBuffer;
import com.peter.ccgraphics.rendering.ScreenTexture;

import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.shared.computer.core.InputHandler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;

public class GraphicsScreenWidget extends TerminalWidget {

    private ScreenTexture texture;
    private final GraphicsComputerMenu handler;

    public GraphicsScreenWidget(GraphicsComputerMenu handler, InputHandler computer, int x, int y) {
        super(handler.getTerminal(), computer, x, y);
        this.handler = handler;
        FrameBuffer frame = handler.getFrame();
        texture = new ScreenTexture(frame.getWidth(), frame.getHeight());
    }

    @Override
    public void renderWidget(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;
        
        texture.setFrame(handler.getFrame());

        drawScreen(graphics, getX(), getY());
    }
    
    protected void drawScreen(DrawContext graphics, int x, int y) {
        graphics.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, texture.getTextureView(), x, y, x + texture.getWidth(), y + texture.getHeight(), 0, 1, 0, 1, -1);
    }

    @Override
    public int getWidth() {
        return texture.getWidth();
    }

    @Override
    public int getHeight() {
        return texture.getHeight();
    }
}
