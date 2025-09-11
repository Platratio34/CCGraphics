package com.peter.ccgraphics.pocket;

import com.peter.ccgraphics.CCGraphics;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.core.PocketBrain;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PocketGraphicsComputerItem extends PocketComputerItem {

    public static final String NAME = "pocket_graphics_computer";
    public static final Identifier ID = CCGraphics.id(NAME);

    // public static final PocketGraphicsComputerItem ITEM = Registry.register(Registries.ITEM, ID, new PocketGraphicsComputerItem(new Settings()));

    public static void init() {}

    public PocketGraphicsComputerItem(Settings settings) {
        super(settings, ComputerFamily.ADVANCED);
    }

    // @Override
    // public void inventoryTick(ItemStack stack, World world, Entity entity, int compartmentSlot, boolean selected) {
    //     super.inventoryTick(stack, world, entity, compartmentSlot, selected);
    // }

    public void setBrain(PocketBrain brain) {
        PocketServerComputer comp = brain.computer();
    }
}
