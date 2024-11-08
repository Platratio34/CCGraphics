package com.peter.ccgraphics.mixin.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.rendering.shaders.MonitorShader;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceFactory;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {

    @Final
    @Shadow
    private Map<String, ShaderProgram> programs;

    GameRendererMixin() {
    }

    @Inject(method = { "loadPrograms(Lnet/minecraft/resource/ResourceFactory;)V" }, at = { @At("TAIL") })
    private void onReloadShaders(ResourceFactory resourceManager, CallbackInfo ci) {
        try {
            MonitorShader.INSTANCE = new MonitorShader(resourceManager);
            this.programs.put(MonitorShader.INSTANCE.getName(), MonitorShader.INSTANCE);
            CCGraphics.LOGGER.info("Added shader for graphics monitor");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not reload shaders (CCGraphics)", e);
        }
    }
}