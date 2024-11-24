package com.peter.ccgraphics.computer;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import com.peter.ccgraphics.lua.ArrayFrameBuffer;
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

    protected FrameBuffer termFrameBuffer;
    protected AtomicBoolean termFrameBufferChanged = new AtomicBoolean(true);

    private ServerWorld world;

    protected NetworkedTerminal terminal;

    protected GraphicsComputerComponent graphicsComponent;

    public ServerGraphicsComputer(ServerWorld level, BlockPos position, int computerID, @Nullable String label,
            ComputerFamily family, int terminalWidth, int terminalHeight, ComponentMap baseComponents,
            GraphicsComputerComponent graphicsComponent) {
        super(level, position, computerID, label, family, terminalWidth / 6, terminalHeight / 9, baseComponents);
        this.pixelWidth = terminalWidth;
        this.pixelHeight = terminalHeight;
        termFrameBuffer = new ArrayFrameBuffer(pixelWidth, pixelHeight);
        world = level;
        terminal = getTerminalState().create();
        this.graphicsComponent = graphicsComponent;
    }
    
    protected void updateFrameBuffer(FrameBuffer frameBuffer) {
        if (frameBuffer == null) {
            throw new IllegalArgumentException("`frameBuffer` must be non-null");
        }
        this.termFrameBuffer = frameBuffer;

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
    }

    @Override
    protected void onTerminalChanged() {
        getTerminalState().apply(terminal);
        
        termFrameBuffer = GraphicsTerminal.renderToFrame(true, terminal);
        termFrameBufferChanged.set(true);

        super.onTerminalChanged();
    }
    
    @Override
    protected void tickServer() {
        super.tickServer();
        if (graphicsComponent.isGraphical()) {
            if (graphicsComponent.pollChanged()) {
                // CCGraphics.LOGGER.info("Updating computer frame");
                updateFrameBuffer(graphicsComponent.getFrameBuffer());
                termFrameBufferChanged.set(true);
            }
        } else if (graphicsComponent.isTerm()) {
            if (termFrameBufferChanged.getAndSet(false)) {
                // CCGraphics.LOGGER.info("Updating computer frame");
                updateFrameBuffer(termFrameBuffer);
            }
        }
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
}
