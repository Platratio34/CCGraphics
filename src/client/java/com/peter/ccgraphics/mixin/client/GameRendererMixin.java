package com.peter.ccgraphics.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceFactory;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {

    // @Final
    // @Shadow
    // private Map<String, ShaderProgram> programs;

    GameRendererMixin() {
    }

    @Inject(method = { "preloadPrograms(Lnet/minecraft/resource/ResourceFactory;)V" }, at = { @At("TAIL") })
    private void onReloadShaders(ResourceFactory resourceManager, CallbackInfo ci) {
        try {
            // MonitorShader.INSTANCE = new MonitorShader(resourceManager);
            // this.programs.put(MonitorShader.INSTANCE.getName(), MonitorShader.INSTANCE);
            // CCGraphics.LOGGER.info("Added shader for graphics monitor");
            
        } catch (Exception e) {
            throw new RuntimeException("Could not reload shaders (CCGraphics)", e);
        }
    }
}