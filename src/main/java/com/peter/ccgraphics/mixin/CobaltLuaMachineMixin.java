package com.peter.ccgraphics.mixin;

import java.util.IdentityHashMap;

import javax.annotation.Nullable;

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
        
        if (object instanceof CustomLuaObject) {
            LuaTable table = invokeWrapLuaObject(object);
            if (table == null) {
                table = new LuaTable();
            }
            table.rawset("___obj", (LuaValue) object);

            info.setReturnValue(table);
            info.getReturnValue();
        } else if (object instanceof LuaValue) {
            // values.put(object, (LuaValue)object);
            // LuaTable
            info.setReturnValue((LuaValue) object);
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
    
    @Invoker("wrapLuaObject")
    public abstract LuaTable invokeWrapLuaObject(Object object);
}
