package com.peter.ccgraphics.computer;

import javax.annotation.Nullable;

import com.peter.ccgraphics.CCGraphics;

import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.ComponentMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class GraphicsComputerBlockEntity extends ComputerBlockEntity {

    public static final String NAME = "graphics_computer";
    public static final Identifier ID = CCGraphics.id(NAME);

    public static final BlockEntityType<GraphicsComputerBlockEntity> ENTITY_TYPE = Registry
            .register(Registries.BLOCK_ENTITY_TYPE, ID, BlockEntityType.Builder
                    .create(GraphicsComputerBlockEntity::new, GraphicsComputerBlock.BLOCK).build());

    public static final int SCREEN_WIDTH = 51 * 6;
    public static final int SCREEN_HEIGHT = 21 * 9;

    public static void init() {
        GraphicsComputerMenu.init();
    }
                

    public GraphicsComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ENTITY_TYPE, pos, state, ComputerFamily.ADVANCED);
    }

    @Override
    protected ServerComputer createComputer(int id) {
        return new ServerGraphicsComputer((ServerWorld) this.getWorld(), this.getPos(), id, this.label,
                this.getFamily(), SCREEN_WIDTH, SCREEN_HEIGHT, ComponentMap.empty());
    }
    
    @Override
    @Nullable
    public ScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new GraphicsComputerMenu(GraphicsComputerMenu.TYPE, id, inventory, this::isUsableByPlayer, (ServerGraphicsComputer)this.createServerComputer());
    }
}
