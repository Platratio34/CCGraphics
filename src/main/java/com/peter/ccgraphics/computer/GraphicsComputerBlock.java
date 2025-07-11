package com.peter.ccgraphics.computer;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.blocks.CCRegistry;
import com.peter.ccgraphics.blocks.CCRegistryEntryType;

import dan200.computercraft.shared.computer.blocks.ComputerBlock;
import dan200.computercraft.shared.platform.RegistryEntry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GraphicsComputerBlock extends ComputerBlock<GraphicsComputerBlockEntity> {

    public static final String NAME = "graphics_computer";
    public static final Identifier ID = CCGraphics.id(NAME);

    public static final GraphicsComputerBlock BLOCK = Registry.register(Registries.BLOCK, ID,
            new GraphicsComputerBlock(CCRegistry.computerProperties().registryKey(CCGraphics.blockRegistryKey(ID))));
    

    public static final BlockItem ITEM = Registry.register(Registries.ITEM, ID,
            new BlockItem(BLOCK, new Item.Settings().registryKey(CCGraphics.itemRegistryKey(ID))));

    private static RegistryEntry<BlockEntityType<GraphicsComputerBlockEntity>> getEntry() {
        return new CCRegistryEntryType<>(ID);
    }

    public static void init() {}

    public GraphicsComputerBlock(AbstractBlock.Settings settings) {
        super(settings, getEntry());
        CCGraphics.LOGGER.info("Created graphics computer block");
    }

}
