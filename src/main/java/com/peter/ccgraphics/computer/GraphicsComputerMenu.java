package com.peter.ccgraphics.computer;

import java.util.function.Predicate;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.ArrayFrameBuffer;
import com.peter.ccgraphics.lua.FrameBuffer;

import dan200.computercraft.shared.computer.inventory.AbstractComputerMenu;
import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import dan200.computercraft.shared.network.container.ContainerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GraphicsComputerMenu extends ComputerMenuWithoutInventory {

    public static final String NAME = "graphics_computer_screen";
    public static final Identifier ID = CCGraphics.id(NAME);

    public static final ScreenHandlerType<GraphicsComputerMenu> TYPE = Registry.register(Registries.SCREEN_HANDLER, ID,
            createMenu());

    public static void init() {
        CCGraphics.LOGGER.info("Initialized Graphics Computer Menu");
    };

    protected FrameBuffer frame = new ArrayFrameBuffer(GraphicsComputerBlockEntity.SCREEN_WIDTH, GraphicsComputerBlockEntity.SCREEN_HEIGHT);

    public GraphicsComputerMenu(ScreenHandlerType<? extends AbstractComputerMenu> type, int id, PlayerInventory player,
            ComputerContainerData menuData) {
        super(type, id, player, menuData);
    }

    public GraphicsComputerMenu(ScreenHandlerType<? extends AbstractComputerMenu> type, int id, PlayerInventory player,
            Predicate<PlayerEntity> canUse, ServerGraphicsComputer computer) {
        super(type, id, player, canUse, computer);
        computer.addListener((ServerPlayerEntity) player.player);
    }

    @Override
    public ServerGraphicsComputer getComputer() {
        return (ServerGraphicsComputer) super.getComputer();
    }

    public FrameBuffer getFrame() {
        // if (frame == null) {
        //     throw new IllegalStateException("Cannot update frame on the server");
        // } else {
            return frame;
        // }
    }

    public void updateFrame(FrameBuffer frame) {
        if (frame == null) {
            throw new IllegalArgumentException("Frame must be non-null");
        } else {
            this.frame = frame;
        }
    }

    public void setFrame(FrameBuffer frame) {
        this.frame = frame;
    }

    protected static ScreenHandlerType<GraphicsComputerMenu> createMenu() {
        return ContainerData.toType(ComputerContainerData.STREAM_CODEC, (id, inv, data) -> {
            return new GraphicsComputerMenu(TYPE, id, inv, data);
        });
    }
}
