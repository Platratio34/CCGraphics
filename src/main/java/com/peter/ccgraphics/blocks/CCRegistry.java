package com.peter.ccgraphics.blocks;

import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Codec;
import com.peter.ccgraphics.computer.GraphicsComputerBlock;
import com.peter.ccgraphics.computer.GraphicsComputerBlockEntity;

import dan200.computercraft.shared.platform.PlatformHelper;
import dan200.computercraft.shared.platform.RegistrationHelper;
import dan200.computercraft.shared.platform.RegistryEntry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BlockEntityType.Builder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class CCRegistry {

    static final RegistrationHelper<Block> BLOCK_REGISTRY = PlatformHelper.get()
            .createRegistrationHelper(RegistryKeys.BLOCK);
    static final RegistrationHelper<BlockEntityType<?>> BLOCK_ENTITY_REGISTRY = PlatformHelper.get()
            .createRegistrationHelper(RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final Codec<RegistryEntry<? extends Block>> BLOCK_REGISTRY_CODEC = RegistryEntry.codec(Registries.BLOCK);

    public static RegistryEntry<GraphicsComputerBlock> GRAPHICS_COMPUTER_BLOCK;

    public static RegistryEntry<BlockEntityType<GraphicsComputerBlockEntity>> GRAPHICS_COMPUTER_BLOCK_ENTITY;

    static {
        // GRAPHICS_COMPUTER_BLOCK = BLOCK_REGISTRY_CODEC.;
        // GRAPHICS_COMPUTER_BLOCK = BLOCK_REGISTRY.register(GraphicsComputerBlock.NAME, () -> {
        //     return new GraphicsComputerBlock(computerProperties().mapColor(MapColor.STONE_GRAY),
        //             GRAPHICS_COMPUTER_BLOCK_ENTITY);
        // });
        // GRAPHICS_COMPUTER_BLOCK_ENTITY = ofBlock(GRAPHICS_COMPUTER_BLOCK, (pos, state) -> {
        //     return new GraphicsComputerBlockEntity(GRAPHICS_COMPUTER_BLOCK_ENTITY.get(), pos, state);
        // });
    }
    
    public static void init() {
        
    }

    public static <T extends BlockEntity> RegistryEntry<BlockEntityType<T>> ofBlock(
            RegistryEntry<? extends Block> block, BlockEntityType.BlockEntityFactory<T> factory) {
        return BLOCK_ENTITY_REGISTRY.register(block.id().getPath(), () -> {
            return Builder.create(factory, new Block[] { (Block) block.get() }).build((Type<T>) null);
        });
    }

    private static AbstractBlock.Settings properties() {
        return AbstractBlock.Settings.create().strength(2.0F);
    }

    public static AbstractBlock.Settings computerProperties() {
        return properties().solidBlock((block, level, blockPos) -> {
            return false;
        });
    }

    public static CCRegistryEntry<Block> entryOfBlock(Identifier id) {
        return new CCRegistryEntry<>(Registries.BLOCK, id);
    }

    public static CCRegistryEntry<BlockEntityType<?>> entryOfBlockEntityType(Identifier id) {
        return new CCRegistryEntry<>(Registries.BLOCK_ENTITY_TYPE, id);
    }
}
