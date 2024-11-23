package com.peter.ccgraphics;

import com.peter.ccgraphics.computer.GraphicsComputerMenu;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlockEntity;
import com.peter.ccgraphics.networking.ComputerFramePacket;
import com.peter.ccgraphics.networking.FrambufferPacket;
import com.peter.ccgraphics.rendering.GraphicsMonitorBlockEntityRenderer;
import com.peter.ccgraphics.rendering.gui.GraphicsComputerScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;

public class CCGraphicsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as
        // rendering.

        BlockEntityRendererFactories.register(GraphicsMonitorBlockEntity.BLOCK_ENTITY_TYPE,
                GraphicsMonitorBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(FrambufferPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                GraphicsMonitorBlockEntity entity = (GraphicsMonitorBlockEntity) context.client().world
                        .getBlockEntity(payload.pos());
                entity.getOriginClientMonitor().updateFrame(payload.frame());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ComputerFramePacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                PlayerEntity player = context.client().player;
                if (player != null && player.currentScreenHandler.syncId == payload.syncId()) {
                    ScreenHandler var5 = player.currentScreenHandler;
                    if (var5 instanceof GraphicsComputerMenu) {
                        GraphicsComputerMenu menu = (GraphicsComputerMenu) var5;
                        menu.setFrame(payload.frame());
                    }
                }
            });
        });

        HandledScreens.register(GraphicsComputerMenu.TYPE, GraphicsComputerScreen::new);
    }
}