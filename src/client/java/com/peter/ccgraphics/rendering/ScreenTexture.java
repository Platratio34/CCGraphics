package com.peter.ccgraphics.rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.FrameBuffer;

public class ScreenTexture implements AutoCloseable {

    protected int glId = -1;
    protected int width;
    protected int height;

    public ScreenTexture(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void bind() {
        check();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glId);
    }

    public void setFrame(FrameBuffer frame) {
        if (frame.getWidth() != width || frame.getHeight() != height) {
            resize(frame.getWidth(), frame.getHeight());
        }
        bind();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT, frame.getTextureBuffer());
        // frame.debugPrint();
    }

    private void check() {
        if (glId > -1)
            return;
        glId = GlStateManager._genTexture();
        CCGraphics.LOGGER.debug("Making new texture ({},{})", width, height);

        bind();
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT, 0);
    }

    public void dispose() {
        GlStateManager._deleteTexture(glId);
        glId = -1;
    }

    public int getId() {
        check();
        return glId;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        CCGraphics.LOGGER.debug("Resizing texture to {} x {}", width, height);
        dispose();
    }

    @Override
    public void close() throws Exception {
        if (RenderSystem.isOnRenderThread())
            dispose();
        else
            RenderSystem.recordRenderCall(() -> {
                dispose();
            });
    }
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
