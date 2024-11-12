package com.peter.ccgraphics.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.squiddev.cobalt.LuaString;
import org.squiddev.cobalt.LuaValue;

import com.peter.ccgraphics.lua.CustomLuaObject;

@Mixin(value = LuaValue.class, remap = false)
public abstract class LuaValueMixin {

    @Inject(method = "luaTypeName()Lorg/squiddev/cobalt/LuaString;", at = @At("HEAD"), cancellable = true)
    private void luaTypeName(CallbackInfoReturnable<LuaString> info) {
        LuaValue value = (LuaValue)(Object) this;
        if (CustomLuaObject.isCustomObject(value)) {
                info.setReturnValue(LuaString.valueOf(CustomLuaObject.getName(value)));
                info.cancel();
        }
    }
}
