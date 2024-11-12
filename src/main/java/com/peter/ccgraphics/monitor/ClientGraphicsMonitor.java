package com.peter.ccgraphics.monitor;

import java.util.function.Supplier;

import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;

import dan200.computercraft.shared.peripheral.monitor.ClientMonitor;

public class ClientGraphicsMonitor {

    private final GraphicsMonitorBlockEntity origin;
    private boolean changed = false;
    private FrameBuffer cFrame = new ArrayFrameBuffer(1, 1);

    private ClientMonitor.RenderState renderState;

    public ClientGraphicsMonitor(GraphicsMonitorBlockEntity origin) {
        this.origin = origin;
    }

    public GraphicsMonitorBlockEntity getOrigin() {
        return origin;
    }

    void destroy() {

    }

    public boolean pollChanged() {
        boolean changed = this.changed;
        this.changed = false;
        return changed;
    }

    public FrameBuffer getFrame() {
        return cFrame;
    }

    public void updateFrame(FrameBuffer frame) {
        this.cFrame = frame;
        changed = true;
    }

    public ClientMonitor.RenderState getRenderState(Supplier<ClientMonitor.RenderState> supplier) {
        if (renderState == null) {
            renderState = supplier.get();
        }
        return renderState;
    }
}
