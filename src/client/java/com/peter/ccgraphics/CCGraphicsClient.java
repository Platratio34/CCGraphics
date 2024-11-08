package com.peter.ccgraphics;

import com.peter.ccgraphics.monitor.GraphicsMonitorBlockEntity;
import com.peter.ccgraphics.networking.FrambufferPacket;
import com.peter.ccgraphics.rendering.GraphicsMonitorBlockEntityRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class CCGraphicsClient implements ClientModInitializer {
	@Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        BlockEntityRendererFactories.register(GraphicsMonitorBlockEntity.BLOCK_ENTITY_TYPE,
                GraphicsMonitorBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(FrambufferPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                GraphicsMonitorBlockEntity entity = (GraphicsMonitorBlockEntity)context.client().world.getBlockEntity(payload.pos());
                entity.getOriginClientMonitor().updateFrame(payload.frame());
            });
        });
    }
}