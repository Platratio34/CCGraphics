package com.peter.ccgraphics.mixin;

import java.util.IdentityHashMap;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.squiddev.cobalt.LuaTable;
import org.squiddev.cobalt.LuaValue;

import com.peter.ccgraphics.lua.CustomLuaObject;

import dan200.computercraft.core.lua.CobaltLuaMachine;

@Mixin(value = CobaltLuaMachine.class, remap = false)
public abstract class CobaltLuaMachineMixin {

    @Inject(at = @At("HEAD"), method = "toValue(Ljava/lang/Object;Ljava/util/IdentityHashMap;)Lorg/squiddev/cobalt/LuaValue;", cancellable = true)
    private void toValue(@Nullable Object object, @Nullable IdentityHashMap<Object, LuaValue> values,
            CallbackInfoReturnable<LuaValue> info) {
        
        if (object instanceof CustomLuaObject obj) {
            LuaTable table = new LuaTable();
            invokeWrapLuaObject(object, table);
            table.rawset("___obj", obj);

            info.setReturnValue(table);
            info.getReturnValue();
        } else if (object instanceof LuaValue val) {
            // values.put(object, (LuaValue)object);
            // LuaTable
            info.setReturnValue(val);
            info.getReturnValue();
        }
    }

    @Inject(method = "toObject(Lorg/squiddev/cobalt/LuaValue;Ljava/util/IdentityHashMap;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private static void toObject(LuaValue value, @Nullable IdentityHashMap<LuaValue, Object> objects,
            CallbackInfoReturnable<Object> info) {
        if (value instanceof CustomLuaObject) {
            info.setReturnValue(value);
            info.cancel();
        } else if (value instanceof LuaTable) {
            LuaTable tbl = (LuaTable) value;
            LuaValue v2 = tbl.rawget("___obj");
            if (!v2.isNil() && v2 instanceof CustomLuaObject) {
                info.setReturnValue((CustomLuaObject) v2);
                info.cancel();
            }
        }
    }
    
    @Invoker("makeLuaObject")
    public abstract boolean invokeWrapLuaObject(Object object, LuaTable table);
}
