package com.peter.ccgraphics.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import net.minecraft.item.Item;

@Mixin(value = PocketComputerItem.class, remap = false)
public class PocketComputerItemMixin extends Item {

    public PocketComputerItemMixin(Settings settings) {
        super(settings);
    }

    // @Inject(method = "tick(Lnet.minecraft.item.ItemStack;Ldan200.computercraft.shared.pocket.core.PocketHolder;Ldan200.computercraft.shared.pocket.core.PocketBrain;)V", at = @At("HEAD"))
    // private void tickComp(ItemStack stack, PocketHolder holder, PocketBrain brain) {
    //     if(((Object)this) instanceof PocketGraphicsComputerItem pgc) {
    //         pgc.setBrain(brain);
    //     }
    // }
}
