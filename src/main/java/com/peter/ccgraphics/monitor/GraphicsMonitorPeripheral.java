package com.peter.ccgraphics.monitor;

import javax.annotation.Nullable;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.GraphicsTerminal;

import java.util.Map;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.apis.TermMethods;

public class GraphicsMonitorPeripheral extends TermMethods implements IPeripheral {

    public final GraphicsMonitorBlockEntity monitor;

    public final GraphicsTerminal terminal;

    private boolean isTerm = false;
    private long lastFrame = 0;
    private boolean flashCursor = true;

    public GraphicsMonitorPeripheral(GraphicsMonitorBlockEntity monitorEntity) {
        this.monitor = monitorEntity;
        terminal = new GraphicsTerminal(monitor.getWidth(), monitor.getHeight());
    }

    @Override
    @Nullable
    public Object getTarget() {
        return monitor;
    }

    @Override
    public String getType() {
        return "graphics_monitor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this || (other instanceof GraphicsMonitorPeripheral && monitor == ((GraphicsMonitorPeripheral)other).monitor);
    }

    @LuaFunction
    public final ArrayFrameBuffer getNewFrameBuffer() {
        return new ArrayFrameBuffer(getWidth(), getHeight());
    }

    @LuaFunction(mainThread = true)
    public final boolean setFrameBuffer(Map<?,?> buffer) throws LuaException {
        try {
            return monitor.setFrameBuffer(FrameBuffer.fromTable(buffer));
        } catch (IllegalArgumentException e) {
            throw new LuaException(e.getMessage());
        }
    }

    @LuaFunction
    public final int getWidth() {
        return monitor.getPixelWidth();
    }

    @LuaFunction
    public final int getHeight() {
        return monitor.getPixelHeight();
    }

    @Override
    public void attach(IComputerAccess computer) {
        monitor.addComputer(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        monitor.removeComputer(computer);
    }

    @Override
    public GraphicsTerminal getTerminal() {
        return terminal;
    }

    public void resize(int w, int h) {
        terminal.resize(w, h);
    }

    @LuaFunction
    public final void makeTerm() {
        // CCGraphics.LOGGER.info("Making graphics monitor terminal");
        isTerm = true;
    }

    @LuaFunction
    public final void makeGraphics() {
        // CCGraphics.LOGGER.info("Making graphics monitor graphics");
        isTerm = false;
    }
    
    public void onUpdate() {
        if (!isTerm)
            return;
        long cTime = System.currentTimeMillis();
        long fromLast = cTime - lastFrame;
        if (fromLast > 500) {
            try {
                terminal.checkSize(monitor.getPixelWidth(), monitor.getPixelHeight());
                monitor.setFrameBuffer(terminal.renderToFrame(flashCursor));
                // CCGraphics.LOGGER.info("Updating terminal frame");
                flashCursor = !flashCursor;
                lastFrame = cTime;
            } catch (Exception e) {
                CCGraphics.LOGGER.error("Error in rendering frame of graphics terminal: ", e);
                isTerm = false;
                CCGraphics.LOGGER.error("Making monitor non-terminal");
            }
        }
    }
}
