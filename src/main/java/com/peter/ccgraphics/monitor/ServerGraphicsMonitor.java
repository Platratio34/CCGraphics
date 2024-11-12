package com.peter.ccgraphics.monitor;

import java.util.concurrent.atomic.AtomicBoolean;

import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;
import com.peter.ccgraphics.networking.FrambufferPacket;

import dan200.computercraft.shared.util.TickScheduler;
import net.minecraft.server.world.ServerWorld;

public class ServerGraphicsMonitor {

    private final GraphicsMonitorBlockEntity origin;
    private int scale = 1;
    private final AtomicBoolean resized = new AtomicBoolean(false);
    private final AtomicBoolean changed = new AtomicBoolean(false);

    public static final int DEFAULT_RESOLUTION = 64;
    private static final long MIN_FRAME_TIME = 1000 / 5;

    private FrameBuffer cFrame;
    private long lastFrameTime = 0;

    public ServerGraphicsMonitor(GraphicsMonitorBlockEntity origin) {
        this.origin = origin;
        cFrame = new ArrayFrameBuffer(getPixelWidth(), getPixelHeight());
    }

    synchronized void rebuild() {
        if (getPixelHeight() != cFrame.getHeight() || getPixelWidth() != cFrame.getWidth()) {
            cFrame = new ArrayFrameBuffer(getPixelWidth(), getPixelHeight());
            markChanged();
        }
    }

    synchronized void reset() {

    }

    synchronized void markChanged() {
        if (!this.changed.getAndSet(true)) {
            TickScheduler.schedule(origin.tickToken);
        }
        FrambufferPacket.sendUpdate((ServerWorld)origin.getWorld(), origin, cFrame);
    }

    int getScale() {
        return scale;
    }

    synchronized void setScale(int scale) {
        if (this.scale != scale) {
            this.scale = scale;
            rebuild();
        }
    }

    boolean pollResized() {
        return this.resized.getAndSet(false);
    }

    synchronized void markResized() {
        resized.set(true);
    }

    boolean pollChanged() {
        return this.changed.getAndSet(false);
    }

    public boolean setFrame(FrameBuffer frame) {
        long cTime = System.currentTimeMillis();
        if (cTime - lastFrameTime < MIN_FRAME_TIME) {
            return false;
        }
        if (cFrame.getWidth() != frame.getWidth() || cFrame.getHeight() != frame.getHeight()) {
            throw new IllegalArgumentException("Frame buffer must be same size as monitor");
        }
        cFrame = frame.copy();
        lastFrameTime = cTime;
        
        markChanged();

        return true;
    }

    public int getPixelWidth() {
        return scale * origin.getWidth() * DEFAULT_RESOLUTION;
    }

    public int getPixelHeight() {
        return scale * origin.getHeight() * DEFAULT_RESOLUTION;
    }
    
    public FrameBuffer getFrame() {
        return cFrame;
    }
}

