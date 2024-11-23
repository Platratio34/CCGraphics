package com.peter.ccgraphics.rendering.gui;

import org.checkerframework.checker.units.qual.h;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.peter.ccgraphics.computer.GraphicsComputerMenu;
import com.peter.ccgraphics.lua.FrameBuffer;
import com.peter.ccgraphics.rendering.ScreenTexture;

import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.shared.computer.core.InputHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

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
        RenderSystem.setShaderTexture(0, texture.getId());
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        MatrixStack matrices = graphics.getMatrices();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, (float) x, (float) y, (float) 0.1).texture(0, 0);
        bufferBuilder.vertex(matrix4f, (float) x, (float) y + texture.getHeight(), (float) 0.1).texture(0, 1);
        bufferBuilder.vertex(matrix4f, (float) x + texture.getWidth(), (float) y + texture.getHeight(), (float) 0.1)
                .texture(1, 1);
        bufferBuilder.vertex(matrix4f, (float) x + texture.getWidth(), (float) y, (float) 0.1).texture(1, 0);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
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
