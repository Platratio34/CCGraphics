package com.peter.ccgraphics.monitor;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dan200.computercraft.shared.config.Config;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Expander {

    private static final Logger LOG = LoggerFactory.getLogger(Expander.class);
    private final World level;
    private final Direction down;
    private final Direction right;
    private GraphicsMonitorBlockEntity origin;
    private int width;
    private int height;

    Expander(GraphicsMonitorBlockEntity origin) {
        this.origin = origin;
        this.width = origin.getWidth();
        this.height = origin.getHeight();
        this.level = (World) Objects.requireNonNull(origin.getWorld(), "level cannot be null");
        this.down = origin.getDown();
        this.right = origin.getRight();
    }

    void expand() {
        int changedCount = 0;
        int changeLimit = Config.monitorWidth * Config.monitorHeight + 1;

        while (this.expandIn(true, false) || this.expandIn(true, true) || this.expandIn(false, false)
                || this.expandIn(false, true)) {
            ++changedCount;
            if (changedCount > changeLimit) {
                LOG.error("Monitor has grown too much. This suggests there's an empty monitor in the world.");
                break;
            }
        }

        if (changedCount > 0) {
            this.origin.resize(this.width, this.height);
        }

    }

    private boolean expandIn(boolean useXAxis, boolean isPositive) {
        BlockPos pos = this.origin.getPos();
        int height = this.height;
        int width = this.width;
        int otherOffset = isPositive ? (useXAxis ? width : height) : -1;
        BlockPos otherPos = useXAxis ? pos.offset(this.right, otherOffset) : pos.offset(this.down, otherOffset);
        BlockEntity other = this.level.getBlockEntity(otherPos);
        if (other instanceof GraphicsMonitorBlockEntity otherMonitor) {
            if (this.origin.isCompatible(otherMonitor)) {
                if (useXAxis) {
                    if (otherMonitor.getYIndex() != 0 || otherMonitor.getHeight() != height) {
                        return false;
                    }

                    width += otherMonitor.getWidth();
                    if (width > Config.monitorWidth) {
                        return false;
                    }
                } else {
                    if (otherMonitor.getXIndex() != 0 || otherMonitor.getWidth() != width) {
                        return false;
                    }

                    height += otherMonitor.getHeight();
                    if (height > Config.monitorHeight) {
                        return false;
                    }
                }

                if (!isPositive) {
                    BlockEntity otherOrigin = this.level.getBlockEntity(otherMonitor.toWorldPos(0, 0));
                    if (!(otherOrigin instanceof GraphicsMonitorBlockEntity)) {
                        return false;
                    }

                    GraphicsMonitorBlockEntity originMonitor = (GraphicsMonitorBlockEntity) otherOrigin;
                    if (!this.origin.isCompatible(originMonitor)) {
                        return false;
                    }

                    this.origin = originMonitor;
                }

                this.width = width;
                this.height = height;
                return true;
            }
        }

        return false;
    }
}
