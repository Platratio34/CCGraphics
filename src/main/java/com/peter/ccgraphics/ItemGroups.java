package com.peter.ccgraphics;

import com.peter.ccgraphics.computer.GraphicsComputerBlock;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlock;
import com.peter.ccgraphics.pocket.PocketGraphicsComputerItem;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ItemGroups {


    public static final ItemGroup MAIN = Registry.register(Registries.ITEM_GROUP, CCGraphics.id("main"),
            FabricItemGroup.builder().icon(() -> new ItemStack(GraphicsMonitorBlock.ITEM))
                    .displayName(Text.of("CC:Graphics"))
                    .entries((ctx, entries) -> {
                        entries.add(GraphicsComputerBlock.ITEM);
                        entries.add(GraphicsMonitorBlock.ITEM);
                        entries.add(PocketGraphicsComputerItem.ITEM);
                    })
                    .build());

    public static void init() {
    }
}
