package com.peter.ccgraphics.monitor;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.FrameBuffer;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorEdgeState;
import dan200.computercraft.shared.peripheral.monitor.XYPair;
import dan200.computercraft.shared.util.BlockEntityHelpers;
import dan200.computercraft.shared.util.TickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class GraphicsMonitorBlockEntity extends BlockEntity {

    public static final String NAME = "graphics_monitor_entity";
    public static final Identifier ID = CCGraphics.id(NAME);
    public static final BlockEntityType<GraphicsMonitorBlockEntity> BLOCK_ENTITY_TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, ID,
            BlockEntityType.Builder.create(GraphicsMonitorBlockEntity::new, GraphicsMonitorBlock.BLOCK).build());

    private static final Logger LOG = LoggerFactory.getLogger(GraphicsMonitorBlockEntity.class);
    public static final double RENDER_BORDER = 0.125;
    public static final double RENDER_MARGIN = 0.03125;
    public static final double RENDER_PIXEL_SCALE = 1d / (double)ServerGraphicsMonitor.DEFAULT_RESOLUTION;
    private static final String NBT_X = "XIndex";
    private static final String NBT_Y = "YIndex";
    private static final String NBT_WIDTH = "Width";
    private static final String NBT_HEIGHT = "Height";

    public static void register() {
        PeripheralLookup.get().registerForBlockEntities((e, d) -> {
            return ((GraphicsMonitorBlockEntity)e).peripheral();}, BLOCK_ENTITY_TYPE);
    }

    @Nullable
    private ServerGraphicsMonitor serverMonitor;
    @Nullable
    private ClientGraphicsMonitor clientMonitor;
    @Nullable
    private GraphicsMonitorPeripheral peripheral;
    private final Set<IComputerAccess> computers = Collections.newSetFromMap(new ConcurrentHashMap());
    private boolean needsUpdate = false;
    private boolean needsValidating = false;
    boolean enqueued;
    private int width = 1;
    private int height = 1;
    private int xIndex = 0;
    private int yIndex = 0;
    @Nullable
    private BlockPos bbPos;
    @Nullable
    private BlockState bbState;
    private int bbX;
    private int bbY;
    private int bbWidth;
    private int bbHeight;
    @Nullable
    private Box boundingBox;
    TickScheduler.Token tickToken = new TickScheduler.Token(this);

    public GraphicsMonitorBlockEntity(BlockPos pos, BlockState state) {
        // super(BLOCK_ENTITY_TYPE, pos, state);
        super(BLOCK_ENTITY_TYPE, pos, state);
    }

    public void cancelRemoval() {
        super.cancelRemoval();
        this.needsValidating = true;
        TickScheduler.schedule(this.tickToken);
    }

    void destroy() {
        if (!this.getWorld().isClient) {
            this.contractNeighbors();
        }

    }

    public void markRemoved() {
        super.markRemoved();
        if (this.clientMonitor != null) {
            this.clientMonitor.destroy();
        }

    }

    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putInt(NBT_X, this.xIndex);
        tag.putInt(NBT_Y, this.yIndex);
        tag.putInt(NBT_WIDTH, this.width);
        tag.putInt(NBT_HEIGHT, this.height);
        super.writeNbt(tag, registries);
    }

    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        int oldXIndex = this.xIndex;
        int oldYIndex = this.yIndex;
        this.xIndex = nbt.getInt(NBT_X);
        this.yIndex = nbt.getInt(NBT_Y);
        this.width = nbt.getInt(NBT_WIDTH);
        this.height = nbt.getInt(NBT_HEIGHT);
        if (this.world != null && this.world.isClient) {
            this.onClientLoad(oldXIndex, oldYIndex);
        }

    }

    void blockTick() {
        if (this.needsValidating) {
            this.needsValidating = false;
            this.validate();
        }

        if (this.needsUpdate) {
            this.needsUpdate = false;
            this.expand();
        }

        if (this.xIndex == 0 && this.yIndex == 0 && this.serverMonitor != null) {
            if (this.serverMonitor.pollResized()) {
                this.eachComputer((c) -> {
                    c.queueEvent("monitor_resize", new Object[] { c.getAttachmentName() });
                });
            }

            //TODO implement this?
            // if (this.serverMonitor.pollTerminalChanged()) {
            //     MonitorWatcher.enqueue(this);
            // }

        }
    }

    @Nullable
    private ServerGraphicsMonitor getServerMonitor() {
        if (this.serverMonitor != null) {
            return this.serverMonitor;
        } else {
            GraphicsMonitorBlockEntity origin = this.getOrigin();
            return origin == null ? null : (this.serverMonitor = origin.serverMonitor);
        }
    }

    @Nullable
    private ServerGraphicsMonitor createServerMonitor() {
        if (this.serverMonitor != null) {
            return this.serverMonitor;
        } else if (this.xIndex == 0 && this.yIndex == 0) {
            this.serverMonitor = new ServerGraphicsMonitor(this);

            for (int x = 0; x < this.width; ++x) {
                for (int y = 0; y < this.height; ++y) {
                    GraphicsMonitorBlockEntity monitor = this.getLoadedMonitor(x, y).getMonitor();
                    if (monitor != null) {
                        monitor.serverMonitor = this.serverMonitor;
                    }
                }
            }

            return this.serverMonitor;
        } else {
            BlockEntity te = this.getWorld().getBlockEntity(this.toWorldPos(0, 0));
            if (te instanceof GraphicsMonitorBlockEntity) {
                GraphicsMonitorBlockEntity monitor = (GraphicsMonitorBlockEntity) te;
                return this.serverMonitor = monitor.createServerMonitor();
            } else {
                return null;
            }
        }
    }

    @Nullable
    public ClientGraphicsMonitor getOriginClientMonitor() {
        if (this.clientMonitor != null) {
            return this.clientMonitor;
        } else {
            GraphicsMonitorBlockEntity origin = this.getOrigin();
            return origin == null ? null : origin.clientMonitor;
        }
    }

    public final BlockEntityUpdateS2CPacket getUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public final NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registries);
        nbt.putInt("XIndex", this.xIndex);
        nbt.putInt("YIndex", this.yIndex);
        nbt.putInt("Width", this.width);
        nbt.putInt("Height", this.height);
        return nbt;
    }

    private void onClientLoad(int oldXIndex, int oldYIndex) {
        if ((oldXIndex != this.xIndex || oldYIndex != this.yIndex) && this.clientMonitor != null) {
            this.clientMonitor.destroy();
            this.clientMonitor = null;
        }

        if (this.xIndex == 0 && this.yIndex == 0 && this.clientMonitor == null) {
            this.clientMonitor = new ClientGraphicsMonitor(this);
        }

    }

    // public final void read(@Nullable TerminalState state) {
    //     if (this.xIndex == 0 && this.yIndex == 0) {
    //         if (this.clientMonitor == null) {
    //             this.clientMonitor = new ClientGraphicsMonitor(this);
    //         }

    //         this.clientMonitor.read(state);
    //     } else {
    //         LOG.warn("Receiving monitor state for non-origin terminal at {}", this.getPos());
    //     }
    // }

    private void updateBlockState() {
        this.getWorld().setBlockState(this.getPos(),
                (BlockState) this.getCachedState().with(GraphicsMonitorBlock.STATE, MonitorEdgeState.fromConnections(
                        this.yIndex < this.height - 1, this.yIndex > 0, this.xIndex > 0, this.xIndex < this.width - 1)),
                2);
    }

    public Direction getDirection() {
        BlockState state = this.getCachedState();
        return state.contains(GraphicsMonitorBlock.FACING) ? (Direction) state.get(GraphicsMonitorBlock.FACING) : Direction.NORTH;
    }

    public Direction getOrientation() {
        BlockState state = this.getCachedState();
        return state.contains(GraphicsMonitorBlock.ORIENTATION) ? (Direction) state.get(GraphicsMonitorBlock.ORIENTATION)
                : Direction.NORTH;
    }

    public Direction getFront() {
        Direction orientation = this.getOrientation();
        return orientation == Direction.NORTH ? this.getDirection() : orientation;
    }

    public Direction getRight() {
        return this.getDirection().rotateYCounterclockwise();
    }

    public Direction getDown() {
        Direction orientation = this.getOrientation();
        if (orientation == Direction.NORTH) {
            return Direction.UP;
        } else {
            return orientation == Direction.DOWN ? this.getDirection() : this.getDirection().getOpposite();
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getXIndex() {
        return this.xIndex;
    }

    public int getYIndex() {
        return this.yIndex;
    }

    boolean isCompatible(GraphicsMonitorBlockEntity other) {
        return this.getOrientation() == other.getOrientation()
                && this.getDirection() == other.getDirection();
    }

    private MonitorState getLoadedMonitor(int x, int y) {
        if (x == this.xIndex && y == this.yIndex) {
            return MonitorState.present(this);
        } else {
            BlockPos pos = this.toWorldPos(x, y);
            World world = this.getWorld();
            if (world != null && world.canSetBlock(pos)) {
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof GraphicsMonitorBlockEntity) {
                    GraphicsMonitorBlockEntity monitor = (GraphicsMonitorBlockEntity) tile;
                    return this.isCompatible(monitor) ? MonitorState.present(monitor) : MonitorState.MISSING;
                } else {
                    return MonitorState.MISSING;
                }
            } else {
                return MonitorState.UNLOADED;
            }
        }
    }

    @Nullable
    private GraphicsMonitorBlockEntity getOrigin() {
        return this.getLoadedMonitor(0, 0).getMonitor();
    }

    BlockPos toWorldPos(int x, int y) {
        return this.xIndex == x && this.yIndex == y ? this.getPos()
                : this.getPos().offset(this.getRight(), -this.xIndex + x).offset(this.getDown(), -this.yIndex + y);
    }

    void resize(int width, int height) {
        if (this.xIndex != 0 || this.yIndex != 0) {
            this.serverMonitor = null;
        }

        this.xIndex = 0;
        this.yIndex = 0;
        this.width = width;
        this.height = height;
        boolean needsTerminal = false;

        label61: for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                GraphicsMonitorBlockEntity monitor = this.getLoadedMonitor(x, y).getMonitor();
                if (monitor != null && monitor.peripheral != null) {
                    needsTerminal = true;
                    break label61;
                }
            }
        }

        if (needsTerminal) {
            if (this.serverMonitor == null) {
                this.serverMonitor = new ServerGraphicsMonitor(this);
            }

            this.serverMonitor.rebuild();
            if (peripheral != null)
                peripheral.resize(serverMonitor.getPixelWidth(), serverMonitor.getPixelHeight());
        } else if (this.serverMonitor != null) {
            this.serverMonitor.reset();
        }

        BlockPos pos = this.getPos();
        Direction down = this.getDown();
        Direction right = this.getRight();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                BlockEntity other = this.getWorld().getBlockEntity(pos.offset(right, x).offset(down, y));
                if (other instanceof GraphicsMonitorBlockEntity monitor) {
                    if (this.isCompatible(monitor)) {
                        monitor.xIndex = x;
                        monitor.yIndex = y;
                        monitor.width = width;
                        monitor.height = height;
                        monitor.serverMonitor = this.serverMonitor;
                        monitor.needsUpdate = monitor.needsValidating = false;
                        monitor.updateBlockState();
                        BlockEntityHelpers.updateBlock(monitor);
                    }
                }
            }
        }

        this.assertInvariant();
    }

    void updateNeighborsDeferred() {
        this.needsUpdate = true;
    }

    void expand() {
        GraphicsMonitorBlockEntity monitor = this.getOrigin();
        if (monitor != null && monitor.xIndex == 0 && monitor.yIndex == 0) {
            (new Expander(monitor)).expand();
        }

    }

    private void contractNeighbors() {
        if (this.width != 1 || this.height != 1) {
            BlockPos pos = this.getPos();
            Direction down = this.getDown();
            Direction right = this.getRight();
            BlockPos origin = this.toWorldPos(0, 0);
            GraphicsMonitorBlockEntity toLeft = null;
            GraphicsMonitorBlockEntity toAbove = null;
            GraphicsMonitorBlockEntity toRight = null;
            GraphicsMonitorBlockEntity toBelow = null;
            if (this.xIndex > 0) {
                toLeft = this.tryResizeAt(pos.offset(right, -this.xIndex), this.xIndex, 1);
            }

            if (this.yIndex > 0) {
                toAbove = this.tryResizeAt(origin, this.width, this.yIndex);
            }

            if (this.xIndex < this.width - 1) {
                toRight = this.tryResizeAt(pos.offset(right, 1), this.width - this.xIndex - 1, 1);
            }

            if (this.yIndex < this.height - 1) {
                toBelow = this.tryResizeAt(origin.offset(down, this.yIndex + 1), this.width,
                        this.height - this.yIndex - 1);
            }

            if (toLeft != null) {
                toLeft.expand();
            }

            if (toAbove != null) {
                toAbove.expand();
            }

            if (toRight != null) {
                toRight.expand();
            }

            if (toBelow != null) {
                toBelow.expand();
            }

        }
    }

    @Nullable
    private GraphicsMonitorBlockEntity tryResizeAt(BlockPos pos, int width, int height) {
        BlockEntity tile = this.getWorld().getBlockEntity(pos);
        if (tile instanceof GraphicsMonitorBlockEntity monitor) {
            if (this.isCompatible(monitor)) {
                monitor.resize(width, height);
                return monitor;
            }
        }

        return null;
    }

    private boolean checkMonitorAt(int xIndex, int yIndex) {
        MonitorState state = this.getLoadedMonitor(xIndex, yIndex);
        if (state.isMissing()) {
            return false;
        } else {
            GraphicsMonitorBlockEntity monitor = state.getMonitor();
            if (monitor == null) {
                return true;
            } else {
                return monitor.xIndex == xIndex && monitor.yIndex == yIndex && monitor.width == this.width
                        && monitor.height == this.height;
            }
        }
    }

    private void validate() {
        if (this.xIndex != 0 || this.yIndex != 0 || this.width != 1 || this.height != 1) {
            if (this.xIndex < 0 || this.xIndex > this.width || this.width <= 0 || this.width > Config.monitorWidth
                    || this.yIndex < 0 || this.yIndex > this.height || this.height <= 0
                    || this.height > Config.monitorHeight || !this.checkMonitorAt(0, 0)
                    || !this.checkMonitorAt(0, this.height - 1) || !this.checkMonitorAt(this.width - 1, 0)
                    || !this.checkMonitorAt(this.width - 1, this.height - 1)) {
                LOG.warn("Monitor is malformed, resetting to 1x1.");
                this.resize(1, 1);
                this.needsUpdate = true;
            }
        }
    }

    void monitorTouched(float xPos, float yPos, float zPos) {
        XYPair pair = XYPair.of(xPos, yPos, zPos, this.getDirection(), this.getOrientation())
                .add((float) this.xIndex, (float) (this.height - this.yIndex - 1));
        if (!((double) pair.x() > (double) this.width - 0.125)
                && !((double) pair.y() > (double) this.height - 0.125) && !((double) pair.x() < 0.125)
                && !((double) pair.y() < 0.125)) {
            ServerGraphicsMonitor serverMonitor = this.getServerMonitor();
            if (serverMonitor != null) {
                double pixelSize = ((double) this.width - 0.3125) / (double) getPixelWidth();

                int xPixelPos = (int) Math.min((double) getPixelWidth(),
                            Math.max(((double) pair.x() - 0.125 - 0.03125) / pixelSize + 1.0, 1.0));
                int yPixelPos = (int) Math.min((double) getPixelHeight(),
                            Math.max(((double) pair.y() - 0.125 - 0.03125) / pixelSize + 1.0, 1.0));
                this.eachComputer((c) -> {
                    c.queueEvent("monitor_touch", new Object[] { c.getAttachmentName(), xPixelPos, yPixelPos });
                });
            }
        }
    }

    private void eachComputer(Consumer<IComputerAccess> fun) {
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                GraphicsMonitorBlockEntity monitor = this.getLoadedMonitor(x, y).getMonitor();
                if (monitor != null) {
                    Iterator<IComputerAccess> var5 = monitor.computers.iterator();

                    while (var5.hasNext()) {
                        IComputerAccess computer = var5.next();
                        fun.accept(computer);
                    }
                }
            }
        }

    }

    public IPeripheral peripheral() {
        ServerGraphicsMonitor monitor = this.createServerMonitor();
        if (monitor != null) {
            monitor.rebuild();
        }
        GraphicsMonitorPeripheral peripheral = this.peripheral != null ? this.peripheral
                : (this.peripheral = new GraphicsMonitorPeripheral(this));
        this.assertInvariant();
        return peripheral;
    }

    void addComputer(IComputerAccess computer) {
        this.computers.add(computer);
    }

    void removeComputer(IComputerAccess computer) {
        this.computers.remove(computer);
    }

    public Box getRenderBoundingBox() {
        if (this.boundingBox != null && this.getCachedState().equals(this.bbState) && this.getPos().equals(this.bbPos)
                && this.xIndex == this.bbX && this.yIndex == this.bbY && this.width == this.bbWidth
                && this.height == this.bbHeight) {
            return this.boundingBox;
        } else {
            this.bbState = this.getCachedState();
            this.bbPos = this.getPos();
            this.bbX = this.xIndex;
            this.bbY = this.yIndex;
            this.bbWidth = this.width;
            this.bbHeight = this.height;
            BlockPos startPos = this.toWorldPos(0, 0);
            BlockPos endPos = this.toWorldPos(this.width, this.height);
            return this.boundingBox = new Box((double) Math.min(startPos.getX(), endPos.getX()),
                    (double) Math.min(startPos.getY(), endPos.getY()),
                    (double) Math.min(startPos.getZ(), endPos.getZ()),
                    (double) (Math.max(startPos.getX(), endPos.getX()) + 1),
                    (double) (Math.max(startPos.getY(), endPos.getY()) + 1),
                    (double) (Math.max(startPos.getZ(), endPos.getZ()) + 1));
        }
    }

    private void assertInvariant() {
        assert this.checkInvariants() : "Monitor invariants failed. See logs.";

    }

    private boolean checkInvariants() {
        LOG.debug("Checking monitor invariants at {}", this.getPos());
        boolean okay = true;
        if (this.width <= 0 || this.height <= 0) {
            okay = false;
            LOG.error("Monitor {} has non-positive of {}x{}", new Object[] { this.getPos(), this.width, this.height });
        }

        boolean hasPeripheral = false;
        GraphicsMonitorBlockEntity origin = this.getOrigin();
        ServerGraphicsMonitor serverMonitor = origin != null ? origin.serverMonitor : this.serverMonitor;

        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                GraphicsMonitorBlockEntity monitor = this.getLoadedMonitor(x, y).getMonitor();
                if (monitor != null) {
                    hasPeripheral |= monitor.peripheral != null;
                    if (monitor.serverMonitor != null && monitor.serverMonitor != serverMonitor) {
                        okay = false;
                        LOG.error("Monitor {} expected to be have serverMonitor={}, but was {}",
                                new Object[] { monitor.getPos(), serverMonitor, monitor.serverMonitor });
                    }

                    if (monitor.xIndex != x || monitor.yIndex != y) {
                        okay = false;
                        LOG.error("Monitor {} expected to be at {},{}, but believes it is {},{}",
                                new Object[] { monitor.getPos(), x, y, monitor.xIndex, monitor.yIndex });
                    }

                    if (monitor.width != this.width || monitor.height != this.height) {
                        okay = false;
                        LOG.error("Monitor {} expected to be size {},{}, but believes it is {},{}", new Object[] {
                                monitor.getPos(), this.width, this.height, monitor.width, monitor.height });
                    }

                    BlockState expectedState = (BlockState) this.getCachedState().with(MonitorBlock.STATE,
                            MonitorEdgeState.fromConnections(y < this.height - 1, y > 0, x > 0, x < this.width - 1));
                    if (monitor.getCachedState() != expectedState) {
                        okay = false;
                        LOG.error("Monitor {} expected to have state {}, but has state {}",
                                new Object[] { monitor.getCachedState(), expectedState, monitor.getCachedState() });
                    }
                }
            }
        }

        if (hasPeripheral != (serverMonitor != null)) {
            okay = false;
            LOG.error("Peripheral is {}, but serverMonitor={}", new Object[] {
                    hasPeripheral, serverMonitor });
        }

        return okay;
    }

    public boolean setFrameBuffer(FrameBuffer buffer) {
        if (serverMonitor == null)
            return false;
        if (serverMonitor.setFrame(buffer)) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
            markDirty();
            
            return true;
        }
        return false;
    }

    public int getPixelWidth() {
        if (serverMonitor == null) {
            return -1;
        }
        return serverMonitor.getPixelWidth();
    }

    public int getPixelHeight() {
        if (serverMonitor == null) {
            return -1;
        }
        return serverMonitor.getPixelHeight();
    }
    
    public IPeripheral getPeripheral(Direction direction) {
        return peripheral();
    }
    
    protected void onTick(World world, BlockPos pos, BlockState state) {
        if(peripheral != null)
            peripheral.onUpdate();
    }

}
