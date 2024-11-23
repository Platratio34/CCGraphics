package com.peter.ccgraphics.computer;

import java.util.Iterator;

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

    protected FrameBuffer frameBuffer;

    private ServerWorld world;

    protected NetworkedTerminal terminal;

    public ServerGraphicsComputer(ServerWorld level, BlockPos position, int computerID, @Nullable String label,
            ComputerFamily family, int terminalWidth, int terminalHeight, ComponentMap baseComponents) {
        super(level, position, computerID, label, family, terminalWidth / 6, terminalHeight / 9, baseComponents);
        this.pixelWidth = terminalWidth;
        this.pixelHeight = terminalHeight;
        frameBuffer = new ArrayFrameBuffer(pixelWidth, pixelHeight);
        world = level;
        terminal = getTerminalState().create();
    }

    @Override
    protected void onTerminalChanged() {
        MinecraftServer server = world.getServer();
        Iterator<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList().iterator();

        getTerminalState().apply(terminal);

        frameBuffer = GraphicsTerminal.renderToFrame(false, terminal);

        while (players.hasNext()) {
            ServerPlayerEntity player = players.next();
            if (player.currentScreenHandler instanceof ComputerMenu
                    && ((ComputerMenu) player.currentScreenHandler).getComputer() == this) {
                ServerPlayNetworking.send(player,
                        new ComputerFramePacket(player.currentScreenHandler.syncId, frameBuffer));
            }
        }

        super.onTerminalChanged();
    }
    
    @Override
    public void markTerminalChanged() {
        super.markTerminalChanged();
    }
}
