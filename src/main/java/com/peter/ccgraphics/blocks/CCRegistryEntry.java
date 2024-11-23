package com.peter.ccgraphics.blocks;

import dan200.computercraft.shared.platform.RegistryEntry;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class CCRegistryEntry<T> implements RegistryEntry<T> {

    private Registry<T> registry;
    private Identifier id;

    public CCRegistryEntry(Registry<T> registry, Identifier id) {
        this.registry = registry;
        this.id = id;
    }

    @Override
    public T get() {
        return registry.get(id);
    }

    @Override
    public Identifier id() {
        return id;
    }

}
