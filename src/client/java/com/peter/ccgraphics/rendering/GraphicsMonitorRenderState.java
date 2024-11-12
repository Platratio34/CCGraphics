package com.peter.ccgraphics.rendering;

import com.peter.ccgraphics.monitor.ClientGraphicsMonitor;

import dan200.computercraft.shared.peripheral.monitor.ClientMonitor;

public class GraphicsMonitorRenderState implements ClientMonitor.RenderState {

    private ScreenTexture texture;
    public long lastFrame = -1l;

    public GraphicsMonitorRenderState() { }

    public ScreenTexture getOrCreateBuffer(ClientGraphicsMonitor monitor) {
        if (texture != null) {
            return texture;
        }
        texture = new ScreenTexture(monitor.getFrame().getWidth(), monitor.getFrame().getHeight());
        texture.setFrame(monitor.getFrame());
        return texture;
    }

    public void changeSize() {
        if(texture != null)
            texture.dispose();
        texture = null;
    }

    @Override
    public void close() {
        texture.dispose();
    }

}
