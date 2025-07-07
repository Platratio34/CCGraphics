package com.peter.ccgraphics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peter.ccgraphics.blocks.CCRegistry;
import com.peter.ccgraphics.computer.GraphicsComputerBlock;
import com.peter.ccgraphics.computer.GraphicsComputerBlockEntity;
import com.peter.ccgraphics.font.FontLoader;
import com.peter.ccgraphics.lua.ScreenAPI;
import com.peter.ccgraphics.lua.GraphicsNativeAPI;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlock;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlockEntity;
import com.peter.ccgraphics.networking.ComputerFramePacket;
import com.peter.ccgraphics.networking.FrambufferPacket;
import com.peter.ccgraphics.pocket.PocketGraphicsComputerItem;

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

        GraphicsComputerBlock.init();
        GraphicsComputerBlockEntity.init();

        PocketGraphicsComputerItem.init();

        ComputerCraftAPI.registerAPIFactory(GraphicsNativeAPI::create);
        ComputerCraftAPI.registerAPIFactory(ScreenAPI::create);

        FrambufferPacket.register();
        ComputerFramePacket.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FontLoader());

        CCRegistry.init();

        ItemGroups.init();

    }
    
    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static RegistryKey<Block> blockRegistryKey(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, id(id));
    }

    public static RegistryKey<Block> blockRegistryKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }

    public static RegistryKey<Item> itemRegistryKey(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, id(id));
    }

    public static RegistryKey<Item> itemRegistryKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.ITEM, id);
    }
}