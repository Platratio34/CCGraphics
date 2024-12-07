package com.peter.ccgraphics.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.peter.ccgraphics.pocket.PocketGraphicsComputerItem;

import dan200.computercraft.shared.pocket.core.PocketBrain;
import dan200.computercraft.shared.pocket.core.PocketHolder;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import net.minecraft.item.ItemStack;

@Mixin(value = PocketComputerItem.class, remap = false)
public class PocketComputerItemMixin {

    @Inject(method = "tick(Lnet.minecraft.item.ItemStack;Ldan200.computercraft.shared.pocket.core.PocketHolder;Ldan200.computercraft.shared.pocket.core.PocketBrain;)V", at = @At("HEAD"))
    private void tick(ItemStack stack, PocketHolder holder, PocketBrain brain) {
        if(((Object)this) instanceof PocketGraphicsComputerItem pgc) {
            pgc.setBrain(brain);
        }
    }
}
