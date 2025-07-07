package com.peter.ccgraphics.monitor;

import org.jetbrains.annotations.Nullable;

public class MonitorState {

    public static final MonitorState UNLOADED;
    public static final MonitorState MISSING;
    private final State state;
    @Nullable
    private final GraphicsMonitorBlockEntity monitor;

    private MonitorState(State state, @Nullable GraphicsMonitorBlockEntity monitor) {
        this.state = state;
        this.monitor = monitor;
    }

    public static MonitorState present(GraphicsMonitorBlockEntity monitor) {
        return new MonitorState(State.PRESENT, monitor);
    }

    public boolean isPresent() {
        return this.state == State.PRESENT;
    }

    public boolean isMissing() {
        return this.state == State.MISSING;
    }

    @Nullable
    public GraphicsMonitorBlockEntity getMonitor() {
        return this.monitor;
    }

    static {
        UNLOADED = new MonitorState(State.UNLOADED,
                (GraphicsMonitorBlockEntity) null);
        MISSING = new MonitorState(State.MISSING,
                (GraphicsMonitorBlockEntity) null);
    }

    protected enum State {
        MISSING,
        UNLOADED,
        PRESENT;
    }
}
