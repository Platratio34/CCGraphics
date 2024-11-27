package com.peter.ccgraphics.computer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import com.peter.ccgraphics.lua.FrameBuffer;
import com.peter.ccgraphics.lua.GraphicsTerminal;
import com.peter.ccgraphics.networking.ComputerFramePacket;

import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.menu.ComputerMenu;
import dan200.computercraft.shared.computer.terminal.NetworkedTerminal;
import dan200.computercraft.shared.util.ComponentMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ServerGraphicsComputer extends ServerComputer {

    public int pixelWidth;
    public int pixelHeight;

    protected AtomicBoolean termFrameBufferInvalid = new AtomicBoolean(true);

    private ServerWorld world;

    protected NetworkedTerminal terminal;

    protected GraphicsComputerComponent graphicsComponent;

    protected FrameBuffer lastFrame;

    protected int cursorBlink = 0;
    protected boolean cursorLast = false;
    protected static final int CURSOR_BLINK_MAX = 16;
    protected static final int CURSOR_BLINK_SWITCH = CURSOR_BLINK_MAX / 2;
    
    protected LinkedList<ServerPlayerEntity> addedListeners = new LinkedList<ServerPlayerEntity>();

    public ServerGraphicsComputer(ServerWorld level, BlockPos position, int computerID, @Nullable String label,
            ComputerFamily family, int terminalWidth, int terminalHeight, ComponentMap baseComponents,
            GraphicsComputerComponent graphicsComponent) {
        super(level, position, computerID, label, family, terminalWidth / 6, terminalHeight / 9, baseComponents);
        this.pixelWidth = terminalWidth;
        this.pixelHeight = terminalHeight;
        world = level;
        terminal = getTerminalState().create();
        this.graphicsComponent = graphicsComponent;
    }

    protected boolean hasListeners() {
        MinecraftServer server = world.getServer();
        Iterator<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList().iterator();

        while (players.hasNext()) {
            ServerPlayerEntity player = players.next();
            if (player.currentScreenHandler instanceof ComputerMenu
                    && ((ComputerMenu) player.currentScreenHandler).getComputer() == this) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean updateFrameBuffer() {
        if (!hasListeners()) {
            return false;
        }
        FrameBuffer frameBuffer = null;

        if (graphicsComponent.isGraphical()) {
            if (!graphicsComponent.pollChanged()) {
                return false;
            }
            frameBuffer = graphicsComponent.getFrameBuffer();
        } else if (graphicsComponent.isTerm()) {
            boolean cursor = (cursorBlink < CURSOR_BLINK_SWITCH) && terminal.getCursorBlink();
            
            boolean termChanged = termFrameBufferInvalid.getAndSet(false);
            boolean changed = termChanged || (cursor != cursorLast);
            if (!changed) {
                return false;
            }

            if(termChanged)
                getTerminalState().apply(terminal);

            frameBuffer = GraphicsTerminal.renderToFrame(cursor, terminal);

            cursorLast = cursor;
        }

        if (frameBuffer == null) {
            throw new IllegalStateException("Frame buffer was not set, how did we get here?");
        }

        MinecraftServer server = world.getServer();
        Iterator<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList().iterator();

        while (players.hasNext()) {
            ServerPlayerEntity player = players.next();
            if (player.currentScreenHandler instanceof ComputerMenu
                    && ((ComputerMenu) player.currentScreenHandler).getComputer() == this) {
                ServerPlayNetworking.send(player,
                        new ComputerFramePacket(frameBuffer, player.currentScreenHandler.syncId));
            }
        }
        lastFrame = frameBuffer;
        return true;
    }

    @Override
    protected void onTerminalChanged() {
        termFrameBufferInvalid.set(true);

        super.onTerminalChanged();
    }
    

    @Override
    protected void tickServer() {
        super.tickServer();

        if (!updateFrameBuffer()) {
            if (addedListeners.size() > 0) {
                for (ServerPlayerEntity player : addedListeners) {
                    ServerPlayNetworking.send(player,
                            new ComputerFramePacket(lastFrame, player.currentScreenHandler.syncId));
                }
            }
        }
        addedListeners.clear();

        cursorBlink = (cursorBlink + 1) % CURSOR_BLINK_MAX;
    }
    
    @Override
    public void shutdown() {
        graphicsComponent.graphicsMode = false;
        super.shutdown();
    }

    @Override
    protected void markTerminalChanged() { // This is literally just to expose the method
        super.markTerminalChanged();
    }

    public void addListener(ServerPlayerEntity player) {
        addedListeners.add(player);
    }
}
