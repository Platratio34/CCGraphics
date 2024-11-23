package com.peter.ccgraphics.networking;

import com.peter.ccgraphics.CCGraphics;
import com.peter.ccgraphics.lua.FrameBuffer;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ComputerFramePacket(int syncId, FrameBuffer frame) implements CustomPayload {

    public static final Id<ComputerFramePacket> ID = new Id<ComputerFramePacket>(CCGraphics.id("frame_buffer_computer"));
    public static final PacketCodec<ByteBuf, ComputerFramePacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ComputerFramePacket::syncId,
            FrameBuffer.PACKET_CODEC, ComputerFramePacket::frame,
            ComputerFramePacket::new
    );


    @Override
    public Id<ComputerFramePacket> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }
}
