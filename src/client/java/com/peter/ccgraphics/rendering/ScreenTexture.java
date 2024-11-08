package com.peter.ccgraphics.rendering;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.platform.GlStateManager;
import com.peter.ccgraphics.monitor.FrameBuffer;

public class ScreenTexture {

    protected int glId = -1;
    protected int width;
    protected int height;

    // protected final DirectVertexBuffer vbo;

    public ScreenTexture(int width, int height) {
        this.width = width;
        this.height = height;

        bind();
    }

    public void bind() {
        check();

        // GlStateManager._bindTexture(glId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glId);
    }

    public void setFrame(FrameBuffer frame) {
        bind();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT, frame.getTextureBuffer());
        // GL13.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        // GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        // GlStateManager._bindTexture(0);
        // frame.debugPrint();
    }

    private void check() {
        if (glId > -1)
            return;
        glId = GlStateManager._genTexture();

        bind();
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    public void dispose() {
        GlStateManager._deleteTexture(glId);
        glId = -1;
    }

    public int getId() {
        return glId;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_FLOAT, 0);
    }
}
