package com.peter.ccgraphics.networking;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.monitor.FrameBuffer;
import com.peter.ccgraphics.monitor.GraphicsMonitorBlockEntity;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record FrambufferPacket(FrameBuffer frame, BlockPos pos) implements CustomPayload {

    public static final Id<FrambufferPacket> ID = new Id<FrambufferPacket>(CCGraphics.id("frame_buffer"));
    public static final PacketCodec<ByteBuf, FrambufferPacket> CODEC = PacketCodec.tuple(
            FrameBuffer.PACKET_CODEC, FrambufferPacket::frame,
            BlockPos.PACKET_CODEC, FrambufferPacket::pos,
            FrambufferPacket::new
    );


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void sendUpdate(ServerWorld world, GraphicsMonitorBlockEntity entity, FrameBuffer frame) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, new FrambufferPacket(frame, entity.getPos()));
        }
    }

}
