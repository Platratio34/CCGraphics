package com.peter.ccgraphics.blocks;

import dan200.computercraft.shared.platform.RegistryEntry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CCRegistryEntryType<T extends BlockEntity> implements RegistryEntry<BlockEntityType<T>> {

    private Identifier id;

    public CCRegistryEntryType(Identifier id) {
        this.id = id;
    }

    @Override
    public BlockEntityType<T> get() {
        return (BlockEntityType<T>)Registries.BLOCK_ENTITY_TYPE.get(id);
    }

    @Override
    public Identifier id() {
        return id;
    }

}
