package com.peter.ccgraphics.monitor;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.peter.ccgraphics.CCGraphics;

import dan200.computercraft.shared.peripheral.monitor.MonitorEdgeState;
import dan200.computercraft.shared.platform.PlatformHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GraphicsMonitorBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    public static final String NAME = "graphics_monitor";
    public static final Identifier ID = CCGraphics.id(NAME);

    public static final DirectionProperty ORIENTATION = DirectionProperty.of("orientation", new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH});
   public static final EnumProperty<MonitorEdgeState> STATE = EnumProperty.of("state", MonitorEdgeState.class);

    public static final MapCodec<GraphicsMonitorBlock> CODEC = createCodec(GraphicsMonitorBlock::new);
    
    public static final GraphicsMonitorBlock BLOCK = Registry.register(Registries.BLOCK, ID,
            new GraphicsMonitorBlock(Settings.create().solid()));
    public static final BlockItem ITEM = Registry.register(Registries.ITEM, ID,
            new BlockItem(BLOCK, new Item.Settings()));

    protected GraphicsMonitorBlock(Settings settings) {
        super(settings);
    }

    public static void register() { }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GraphicsMonitorBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
    

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ORIENTATION, FACING, STATE});
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext context) {
      float pitch = context.getPlayer() == null ? 0.0F : context.getPlayer().getPitch();
      Direction orientation;
      if (pitch > 66.5F) {
         orientation = Direction.UP;
      } else if (pitch < -66.5F) {
         orientation = Direction.DOWN;
      } else {
         orientation = Direction.NORTH;
      }

      return (BlockState)((BlockState)this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite())).with(ORIENTATION, orientation);
   }

   protected final void onStateReplaced(BlockState block, World world, BlockPos pos, BlockState replace, boolean bool) {
      if (block.getBlock() != replace.getBlock()) {
         BlockEntity tile = world.getBlockEntity(pos);
         super.onStateReplaced(block, world, pos, replace, bool);
         if (tile instanceof GraphicsMonitorBlockEntity) {
            GraphicsMonitorBlockEntity generic = (GraphicsMonitorBlockEntity)tile;
            generic.destroy();
         }

      }
   }

   protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
      BlockEntity te = world.getBlockEntity(pos);
      if (te instanceof GraphicsMonitorBlockEntity monitor) {
         monitor.blockTick();
      }

   }

   protected final ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
      if (!player.isInSneakingPose()) {
         BlockEntity var7 = level.getBlockEntity(pos);
         if (var7 instanceof GraphicsMonitorBlockEntity) {
            GraphicsMonitorBlockEntity monitor = (GraphicsMonitorBlockEntity)var7;
            if (monitor.getFront() == hit.getSide()) {
               if (!level.isClient) {
                  monitor.monitorTouched((float)(hit.getPos().x - (double)hit.getBlockPos().getX()), (float)(hit.getPos().y - (double)hit.getBlockPos().getY()), (float)(hit.getPos().z - (double)hit.getBlockPos().getZ()));
               }

               return ActionResult.success(level.isClient);
            }
         }
      }

      return ActionResult.PASS;
   }

   public void onPlaced(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity livingEntity,
           ItemStack itemStack) {
       GraphicsMonitorBlockEntity monitor;
       label20: {
           super.onPlaced(world, pos, blockState, livingEntity, itemStack);
           BlockEntity entity = world.getBlockEntity(pos);
           if (entity instanceof GraphicsMonitorBlockEntity) {
               monitor = (GraphicsMonitorBlockEntity) entity;
               if (!world.isClient) {
                   if (livingEntity == null) {
                       break label20;
                   }

                   if (livingEntity instanceof ServerPlayerEntity) {
                       ServerPlayerEntity player = (ServerPlayerEntity) livingEntity;
                       if (PlatformHelper.get().isFakePlayer(player)) {
                           break label20;
                       }
                   }

                   monitor.expand();
               }
           }

           return;
       }

       monitor.updateNeighborsDeferred();
   }
   
   @Override
   protected BlockRenderType getRenderType(BlockState state) {
       return BlockRenderType.MODEL;
   }

}
