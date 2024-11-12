package com.peter.ccgraphics.rendering;

import org.joml.Matrix4f;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.FrameBuffer;
import com.peter.ccgraphics.monitor.ClientGraphicsMonitor;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlockEntity;
import com.peter.ccgraphics.rendering.shaders.MonitorShader;

import dan200.computercraft.client.FrameInfo;
import dan200.computercraft.client.integration.ShaderMod;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class GraphicsMonitorBlockEntityRenderer implements BlockEntityRenderer<GraphicsMonitorBlockEntity> {

    private static final float MARGIN = 0.034375F;
    private ScreenTexture texture = new ScreenTexture(1,1);
    private static long lastFrame = -1L;

    public GraphicsMonitorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        
    }

    @Override
    public void render(GraphicsMonitorBlockEntity monitor, float tickDelta, MatrixStack transform,
            VertexConsumerProvider bufferSource, int lightmapCoord, int overlayLight) {
        ClientGraphicsMonitor clientMonitor = monitor.getOriginClientMonitor();
        if (clientMonitor == null) {
            CCGraphics.LOGGER.warn("Could not render monitor ... Missing client monitor");
            return;
        }
        FrameBuffer frameBuffer = clientMonitor.getFrame();
        GraphicsMonitorBlockEntity origin = clientMonitor.getOrigin();
        BlockPos monitorPos = monitor.getPos();
        long renderFrame = FrameInfo.getRenderFrame();

        if (clientMonitor.pollChanged()) {
            if (frameBuffer.getWidth() != texture.width || frameBuffer.getHeight() != texture.height) {
                texture.resize(frameBuffer.getWidth(), frameBuffer.getHeight());
            }
            texture.setFrame(frameBuffer);
        }

        if (lastFrame != renderFrame) {
            lastFrame = renderFrame;
            BlockPos originPos = origin.getPos();
            Direction dir = origin.getDirection();
            Direction front = origin.getFront();
            float yaw = dir.asRotation();
            float pitch = DirectionUtil.toPitchAngle(front);

            transform.push();
            transform.translate((double) (originPos.getX() - monitorPos.getX()) + 0.5,
                    (double) (originPos.getY() - monitorPos.getY()) + 0.5,
                    (double) (originPos.getZ() - monitorPos.getZ()) + 0.5);
            transform.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw));
            transform.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
            transform.translate(-0.34375, (double) origin.getHeight() - 0.5 - 0.15625, 0.5);
            double xSize = (double) origin.getWidth() - 0.3125;
            double ySize = (double) origin.getHeight() - 0.3125;

            if (texture != null && !ShaderMod.get().isRenderingShadowPass()) {
                int width = texture.width;
                int height = texture.height;
                double xScale = xSize / (double) width;
                double yScale = ySize / (double) height;

                transform.push();
                transform.scale((float) xScale, (float) (-yScale), 0.1f);

                Matrix4f matrix = transform.peek().getPositionMatrix();
                renderMonitor(matrix, clientMonitor, texture, (float) (MARGIN / xScale),
                        (float) (MARGIN / yScale));

                transform.pop();
            }

            transform.pop();
        }
    }

    private void renderMonitor(Matrix4f matrix, ClientGraphicsMonitor clientMonitor, ScreenTexture texture2, float xMargin,
            float yMargin) {
        
        MonitorShader.MONITOR_LAYER.startDrawing();

        MonitorShader.INSTANCE.setTexture(texture2);

        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_TEXTURE);
        addVertex(buffer, matrix, -xMargin, -yMargin, 0.0f, 0.0f);
        addVertex(buffer, matrix, -xMargin, texture2.height + yMargin, 0.0f, 1.0f);
        addVertex(buffer, matrix, texture2.width + xMargin, -yMargin, 1.0f, 0.0f);
        addVertex(buffer, matrix, texture2.width + xMargin, texture2.height + yMargin, 1.0f, 1.0f);
        MonitorShader.MONITOR_LAYER.draw(buffer.end());
    }

    private static void addVertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float u, float v) {
        builder.vertex(matrix, x, y, 0.01f).texture(u, v);
    }

    @Override
    public int getRenderDistance() {
        return Config.monitorDistance;
    }

}
