package com.peter.ccgraphics.lua;

import org.jetbrains.annotations.Nullable;

import com.peter.ccgraphics.computer.GraphicsComputerComponent;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.IComputerSystem;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class ScreenAPI implements ILuaAPI {

    protected final IComputerSystem computer;
    protected GraphicsComputerComponent component;

    protected ScreenAPI(IComputerSystem computer) {
        this.computer = computer;
        component = computer.getComponent(GraphicsComputerComponent.GRAPHICS_COMPONENT);
    }

    @Nullable
    public static ILuaAPI create(IComputerSystem computer) {
        if (computer.getComponent(GraphicsComputerComponent.GRAPHICS_COMPONENT) == null) {
            return null;
        }
        return new ScreenAPI(computer);
    }

    @Override
    public String[] getNames() {
        return new String[] { "screen" };
    }
    
    /**
     * Set the current frame buffer for the screen
     * @param frame Frame buffer to set
     * @param arguments
     * @throws LuaException On incorrect arguments
     */
    @LuaFunction
    public final void setFrame(IArguments arguments) throws LuaException {
        FrameBuffer frame = (FrameBuffer) arguments.get(0);
        if (frame == null) {
            throw new LuaException("Argument #1 must be a FrameBuffer");
        }
        if (frame.width != getWidth() || frame.height != getHeight()) {
            throw new LuaException("Frame buffer must be the size of the screen");
        }
        component.setFrameBuffer(frame);
    }

    /**
     * Put the screen in graphics mode (ie setting the frame buffer directly)
     */
    @LuaFunction
    public final void setGraphicsMode() {
        component.graphicsMode = true;
    }

    /**
     * Puts the screen in terminal mode
     */
    @LuaFunction
    public final void setTerminalMode() {
        component.graphicsMode = false;
    }

    /**
     * Gets the width of the screen in pixels
     * @return Screen width
     */
    @LuaFunction
    public final int getWidth() {
        return component.width;
    }

    /**
     * Gets the height of the screen in pixels
     * @return Screen height
     */
    @LuaFunction
    public final int getHeight() {
        return component.height;
    }

    /**
     * Get a new frame buffer the size of this screen
     * @return New frame buffer
     */
    @LuaFunction
    public final FrameBuffer getFrameBuffer() {
        return new ArrayFrameBuffer(getWidth(), getHeight());
    }


}
