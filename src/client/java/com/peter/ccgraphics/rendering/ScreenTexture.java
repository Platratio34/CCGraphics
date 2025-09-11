package com.peter.ccgraphics.rendering;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.FrameBuffer;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

public class ScreenTexture implements AutoCloseable {

    protected int glId = -1;
    protected int width;
    protected int height;
    protected GpuTexture gpuTexture = null;

    protected NativeImageBackedTexture niTexture;
    protected NativeImage nativeImage;

    public ScreenTexture(int width, int height) {
        this.width = width;
        this.height = height;
        nativeImage = new NativeImage(width, height, false);
    }

    public void bind() {
        check();

        GlStateManager._bindTexture(glId);
    }

    public void setFrame(FrameBuffer frame) {
        if (frame.getWidth() != width || frame.getHeight() != height) {
            resize(frame.getWidth(), frame.getHeight());
        }
        check();
        for (int x = 0; x < frame.getWidth(); x++) {
            for (int y = 0; y < frame.getHeight(); y++) {
                int p = frame.getPixel(x, y);
                int r = (p & 0x00_ff_00_00) >> 16;
                int g = (p & 0x00_00_ff_00) >> 8;
                int b = (p & 0x00_00_00_ff);
                nativeImage.setColor(x, y, 0xff00_0000 | (b << 16) | (g << 8) | r);
            }
        }
        if(niTexture != null)
            niTexture.upload();
    }

    private static int nextTId = 0;
    private void check() {
        if (niTexture != null)
            return;
        
        if(nativeImage == null)
            nativeImage = new NativeImage(width, height, false);
        niTexture = new NativeImageBackedTexture(() -> "screenTexture-" + (nextTId++), nativeImage);
        niTexture.upload();
    }

    public void dispose() {
        if (niTexture != null)
            niTexture.close();
        if (nativeImage != null)
            nativeImage.close();
        niTexture = null;
        nativeImage = null;
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
    public void close() {
        if (RenderSystem.isOnRenderThread())
            dispose();
        else
            RenderSystem.queueFencedTask(() -> {
                dispose();
            });
    }
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GpuTextureView getTextureView() {
        return niTexture.getGlTextureView();
    }
}
