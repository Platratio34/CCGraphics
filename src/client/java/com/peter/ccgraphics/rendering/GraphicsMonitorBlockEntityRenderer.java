package com.peter.ccgraphics.rendering;

import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.FrameBuffer;
import com.peter.ccgraphics.monitor.ClientGraphicsMonitor;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlockEntity;

import dan200.computercraft.client.FrameInfo;
import dan200.computercraft.client.integration.ShaderMod;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class GraphicsMonitorBlockEntityRenderer implements BlockEntityRenderer<GraphicsMonitorBlockEntity> {

    private static final float MARGIN = 0.034375F;
    private final BlockEntityRendererFactory.Context context;

    private ScreenTexture texture;

    private RenderPhase.TextureBase textureBase = new RenderPhase.TextureBase(() -> {
        RenderSystem.setShaderTexture(0, texture.getTextureView());
    }, () -> {
    });
    private static final RenderPipeline MONITOR_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.TRANSFORMS_PROJECTION_FOG_SNIPPET).withLocation("pipeline/graphics_monitor")
		.withVertexShader("core/ccgraphics/graphics_monitor")
        .withFragmentShader("core/ccgraphics/graphics_monitor")
		.withSampler("Sampler0")
            .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS)
		.build());
    private RenderLayer renderLayer = RenderLayer.of(
		"monitor",
		1536,
		true,
		false,
		RenderPipelines.CUTOUT,
		RenderLayer.MultiPhaseParameters.builder().texture(textureBase).build(true)
	);

    public GraphicsMonitorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }
    
    @Override
    public void render(GraphicsMonitorBlockEntity monitor, float tickDelta, MatrixStack transform,
            VertexConsumerProvider bufferSource, int lightmapCoord, int overlayLight, Vec3d cameraPos) {
                
        ClientGraphicsMonitor clientMonitor = monitor.getOriginClientMonitor();
        if (clientMonitor == null) {
            CCGraphics.LOGGER.warn("Could not render monitor ... Missing client monitor");
            return;
        }
        FrameBuffer frameBuffer = clientMonitor.getFrame();
        GraphicsMonitorBlockEntity origin = clientMonitor.getOrigin();
        BlockPos monitorPos = monitor.getPos();
        long renderFrame = FrameInfo.getRenderFrame();
        GraphicsMonitorRenderState renderState = (GraphicsMonitorRenderState) clientMonitor
                .getRenderState(GraphicsMonitorRenderState::new);
        texture = renderState.getOrCreateBuffer(clientMonitor);

        if (clientMonitor.pollChanged()) {
            if (frameBuffer.getWidth() != texture.width || frameBuffer.getHeight() != texture.height) {
                texture.resize(frameBuffer.getWidth(), frameBuffer.getHeight());
            }
            texture.setFrame(frameBuffer);
        }

        if (renderState.lastFrame != renderFrame) {
            renderState.lastFrame = renderFrame;
            BlockPos originPos = origin.getPos();
            Direction dir = origin.getDirection();
            Direction front = origin.getFront();
            float yaw = dir.getPositiveHorizontalDegrees();
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
                
        renderLayer.startDrawing();
        BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        addVertex(buffer, matrix, -xMargin, -yMargin, 0.0f, 0.0f);
        addVertex(buffer, matrix, -xMargin, texture2.height + yMargin, 0.0f, 1.0f);
        addVertex(buffer, matrix, texture2.width + xMargin, texture2.height + yMargin, 1.0f, 1.0f);
        addVertex(buffer, matrix, texture2.width + xMargin, -yMargin, 1.0f, 0.0f);
        renderLayer.draw(buffer.end());
        renderLayer.endDrawing();
    }

    private static void addVertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float u, float v) {
        builder.vertex(matrix, x, y, 0.01f).texture(u, v).color(255, 255, 255, 255).light(255, 255).normal(0, 0, 1);
    }

    @Override
    public int getRenderDistance() {
        return Config.monitorDistance;
    }

}
