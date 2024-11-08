package com.peter.ccgraphics;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peter.ccgraphics.lua.GraphicsAPI;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlock;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlockEntity;
import com.peter.ccgraphics.networking.FrambufferPacket;

import dan200.computercraft.api.ComputerCraftAPI;

public class CCGraphics implements ModInitializer {
	public static final String MOD_ID = "ccgraphics";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Starting Peter's CCGraphics extension");

        GraphicsMonitorBlock.register();
        GraphicsMonitorBlockEntity.register();

        ComputerCraftAPI.registerAPIFactory(new GraphicsAPI());

        FrambufferPacket.register();

    }
    
    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }
}