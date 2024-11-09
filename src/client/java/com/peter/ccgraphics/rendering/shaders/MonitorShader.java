package com.peter.ccgraphics.rendering.shaders;

import java.io.IOException;

import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.systems.RenderSystem;
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
    private ScreenTexture cTexture;

    public MonitorShader(ResourceFactory factory) throws IOException {
        super(factory, "ccgraphics/graphics_monitor", VertexFormats.POSITION_TEXTURE);
    }

    public void setTexture(ScreenTexture texture) {
        this.cTexture = texture;
    }

    @Override
    public void bind() {
        RenderSystem.setShaderTexture(0, cTexture.getId());

        super.bind();
        
        int samplerUniform = GlUniform.getUniformLocation(getGlRef(), "Sampler0");
        GlUniform.uniform1(samplerUniform, 0);
        RenderSystem.activeTexture(GL20.GL_TEXTURE0);
        RenderSystem.bindTexture(cTexture.getId());
    }

    public static final RenderLayer MONITOR_LAYER = RenderLayer.of("graphics_monitor", VertexFormats.POSITION_TEXTURE,
            VertexFormat.DrawMode.TRIANGLE_STRIP, 128, false, false,
            MultiPhaseParameters.builder().program(new RenderPhase.ShaderProgram(() -> {
                return INSTANCE;
            })).build(false));
}
