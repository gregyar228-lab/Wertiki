package com.skyrotor.network;

import com.skyrotor.SkyRotorMod;
import com.skyrotor.entity.HelicopterEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SkyRotorMod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, InputPacket.class, InputPacket::encode, InputPacket::decode, InputPacket::handle);
        CHANNEL.registerMessage(id++, ToggleEnginePacket.class, ToggleEnginePacket::encode, ToggleEnginePacket::decode, ToggleEnginePacket::handle);
    }

    /** Клиент -> сервер: какие клавиши сейчас нажаты (биты). */
    public static class InputPacket {
        public static final int FORWARD = 1;
        public static final int BACK = 2;
        public static final int LEFT = 4;
        public static final int RIGHT = 8;
        public static final int UP = 16;
        public static final int DOWN = 32;

        private final int flags;

        public InputPacket(int flags) {
            this.flags = flags;
        }

        public static void encode(InputPacket msg, FriendlyByteBuf buf) {
            buf.writeByte(msg.flags);
        }

        public static InputPacket decode(FriendlyByteBuf buf) {
            return new InputPacket(buf.readByte());
        }

        public static void handle(InputPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
            NetworkEvent.Context ctx = ctxSup.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player != null && player.getVehicle() instanceof HelicopterEntity heli) {
                    heli.setInput(msg.flags);
                }
            });
            ctx.setPacketHandled(true);
        }
    }

    /** Клиент -> сервер: клавиша K удержана 2 секунды, переключить двигатель. */
    public static class ToggleEnginePacket {
        public static void encode(ToggleEnginePacket msg, FriendlyByteBuf buf) {
        }

        public static ToggleEnginePacket decode(FriendlyByteBuf buf) {
            return new ToggleEnginePacket();
        }

        public static void handle(ToggleEnginePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
            NetworkEvent.Context ctx = ctxSup.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player != null && player.getVehicle() instanceof HelicopterEntity heli) {
                    heli.toggleEngine(player);
                }
            });
            ctx.setPacketHandled(true);
        }
    }
}
