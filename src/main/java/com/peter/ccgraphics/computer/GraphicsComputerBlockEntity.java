package com.peter.ccgraphics.computer;

import org.jetbrains.annotations.Nullable;

import com.peter.ccgraphics.CCGraphics;

import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputer.Properties;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
            .register(Registries.BLOCK_ENTITY_TYPE, ID, FabricBlockEntityTypeBuilder
                    .create(GraphicsComputerBlockEntity::new, GraphicsComputerBlock.BLOCK).build());

    public static final int SCREEN_WIDTH = 51 * 6;
    public static final int SCREEN_HEIGHT = 21 * 9;

    protected ServerGraphicsComputer computer;

    public static void init() {
        GraphicsComputerMenu.init();
    }
                

    public GraphicsComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ENTITY_TYPE, pos, state, ComputerFamily.ADVANCED);
    }

    @Override
    protected ServerComputer createComputer(int id) {
        // Builder map = ComponentMap.builder();
        GraphicsComputerComponent graphicsComponent = new GraphicsComputerComponent(SCREEN_WIDTH, SCREEN_HEIGHT);
        Properties properties = ServerComputer.properties(id, ComputerFamily.ADVANCED);
        properties.addComponent(GraphicsComputerComponent.GRAPHICS_COMPONENT, graphicsComponent);
        computer = new ServerGraphicsComputer((ServerWorld) this.getWorld(), this.getPos(), SCREEN_WIDTH, SCREEN_HEIGHT, properties, graphicsComponent);
        graphicsComponent.computer = computer;
        return computer;
    }
    
    @Override
    @Nullable
    public ScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new GraphicsComputerMenu(GraphicsComputerMenu.TYPE, id, inventory, this::isUsableByPlayer,
                (ServerGraphicsComputer) this.createServerComputer());
    }
}
