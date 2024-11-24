package com.peter.ccgraphics.computer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.FrameBuffer;

import dan200.computercraft.api.component.ComputerComponent;

public class GraphicsComputerComponent {

    protected AtomicBoolean changed = new AtomicBoolean(false);

    protected FrameBuffer frameBuffer;

    protected ServerGraphicsComputer computer;

    public boolean graphicsMode = false;

    public int width;
    public int height;

    public static final ComputerComponent<GraphicsComputerComponent> GRAPHICS_COMPONENT = ComputerComponent.create(CCGraphics.MOD_ID, "graphics_computer");

    public GraphicsComputerComponent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public synchronized void setFrameBuffer(FrameBuffer frame) {
        if (frame == null) {
            throw new IllegalArgumentException("`frame` must be non-null");
        }
        this.frameBuffer = frame;
        changed.set(true);
    }

    protected synchronized boolean pollChanged() {
        return changed.getAndSet(false);
    }

    public synchronized FrameBuffer getFrameBuffer() {
        if (this.frameBuffer == null) {
            throw new IllegalStateException("frameBuffer was never set before tying to get it");
        }
        return this.frameBuffer;
    }

    public boolean isTerm() {
        return !graphicsMode;
    }
    public boolean isGraphical() {
        return graphicsMode;
    }
}