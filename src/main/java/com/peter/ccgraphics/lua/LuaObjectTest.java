package com.peter.ccgraphics.lua;

public class LuaObjectTest extends CustomLuaObject {

    protected static final String TYPE_NAME = "java_object";

    @Override
    public String getLuaTypeName() {
        return TYPE_NAME;
    }
}
