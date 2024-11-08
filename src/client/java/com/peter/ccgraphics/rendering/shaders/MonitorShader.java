package com.peter.ccgraphics.rendering.shaders;

import java.io.IOException;

import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.systems.RenderSystem;
import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.rendering.ScreenTexture;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.resource.ResourceFactory;

public class MonitorShader extends ShaderProgram {

    public static MonitorShader INSTANCE;
    private int lastTexture = -1;
    private ScreenTexture cTexture;

    public MonitorShader(ResourceFactory factory) throws IOException {
        super(factory, "ccgraphics/graphics_monitor", VertexFormats.POSITION_TEXTURE);
    }

    public void setTexture(ScreenTexture texture) {
        int texId = texture.getId();
        // bind();
        // getUniform("FrameSampler").set(GL20.GL_TEXTURE0);
        // int samplerUniform = GlUniform.getUniformLocation(getGlRef(), "FrameSampler");
        // // CCGraphics.LOGGER.info("Uniform location: {}", samplerUniform);
        // GlUniform.uniform1(samplerUniform, 0);
        // RenderSystem.activeTexture(GL20.GL_TEXTURE0);
        // RenderSystem.bindTexture(texId);
        // addSampler("Sampler0", (Integer) texId);
        // RenderSystem.setShaderTexture(0, texId);
        this.cTexture = texture;

        if (texId != lastTexture) {
            CCGraphics.LOGGER.info("Switched monitor texture to {}", texId);
            lastTexture = texId;
        }
    }

    @Override
    public void bind() {
        super.bind();
        
        // int samplerUniform = GlUniform.getUniformLocation(getGlRef(), "Sampler0");
        // // CCGraphics.LOGGER.info("Uniform location: {}", samplerUniform);
        // GlUniform.uniform1(samplerUniform, 0);
        // RenderSystem.activeTexture(GL20.GL_TEXTURE0);
        // RenderSystem.bindTexture(cTexture.getId());
        // RenderSystem.setShaderTexture(0, cTexture.getId());
    }

    public static final RenderLayer MONITOR_LAYER = RenderLayer.of("graphics_monitor", VertexFormats.POSITION_TEXTURE,
            VertexFormat.DrawMode.TRIANGLE_STRIP, 128, false, false,
            MultiPhaseParameters.builder().program(new RenderPhase.ShaderProgram(() -> {
                return INSTANCE;
            })).build(false));
}
