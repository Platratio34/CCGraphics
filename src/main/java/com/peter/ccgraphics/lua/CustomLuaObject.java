package com.peter.ccgraphics.lua;

import org.squiddev.cobalt.Constants;
import org.squiddev.cobalt.LuaError;
import org.squiddev.cobalt.LuaTable;
import org.squiddev.cobalt.LuaValue;

public abstract class CustomLuaObject extends LuaValue {

    private final LuaTable luaTable = new LuaTable();

    public CustomLuaObject() {
        super(10);
    }

    public abstract String getLuaTypeName();

    public static String getName(LuaValue value) {
        return ((CustomLuaObject) value).getLuaTypeName();
    }

    public static boolean isCustomObject(LuaValue value) {
        return value instanceof CustomLuaObject;
    }

    @Override
    public LuaTable checkTable() throws LuaError {
        return luaTable;
    }
    
    /**
	 * Get a value in a table without metatag processing.
	 *
	 * @param key the key to look up, must not be null
	 * @return {@link LuaValue} for that key, or {@link Constants#NIL} if not found
	 */
	public LuaValue rawget(String key) {
		return luaTable.rawget(key);
	}

	/**
	 * Set a value in a table without metatag processing.
	 *
	 * @param key   the key to use, must not be null
	 * @param value the value to use, can be {@link Constants#NIL}, must not be null
	 */
    public void rawset(String key, LuaValue value) {
        luaTable.rawset(key, value);
    }
    
    public int length() {
        return luaTable.length();
    }

}
