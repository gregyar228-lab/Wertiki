package com.skyrotor.client;

import com.skyrotor.SkyRotorMod;
import com.skyrotor.entity.HelicopterEntity;
import com.skyrotor.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Клиентская логика пилота:
 *  - удержание K 2 секунды (40 тиков) = запуск/остановка двигателя;
 *  - отправка нажатых клавиш управления на сервер.
 */
@Mod.EventBusSubscriber(modid = SkyRotorMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private static final int HOLD_TICKS = 40;

    private static int holdTicks = 0;
    private static boolean toggled = false;
    private static int lastInput = -1;
    private static int resendTimer = 0;

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || !(player.getVehicle() instanceof HelicopterEntity heli)) {
            holdTicks = 0;
            toggled = false;
            lastInput = -1;
            return;
        }

        handleEngineKey(mc, player, heli);
        sendInput(mc);
    }

    private static void handleEngineKey(Minecraft mc, LocalPlayer player, HelicopterEntity heli) {
        boolean down = ClientSetup.ENGINE_KEY.isDown() && mc.screen == null;

        if (!down) {
            holdTicks = 0;
            toggled = false;
            return;
        }
        if (toggled) {
            return;
        }

        holdTicks++;
        if (holdTicks >= HOLD_TICKS) {
            toggled = true;
            holdTicks = 0;
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.ToggleEnginePacket());
        } else {
            int percent = holdTicks * 100 / HOLD_TICKS;
            String key = heli.isEngineOn() ? "message.skyrotor.stopping" : "message.skyrotor.starting";
            player.displayClientMessage(Component.translatable(key, percent), true);
        }
    }

    private static void sendInput(Minecraft mc) {
        Options o = mc.options;
        int flags = 0;
        if (mc.screen == null) {
            if (o.keyUp.isDown()) flags |= ModNetwork.InputPacket.FORWARD;
            if (o.keyDown.isDown()) flags |= ModNetwork.InputPacket.BACK;
            if (o.keyLeft.isDown()) flags |= ModNetwork.InputPacket.LEFT;
            if (o.keyRight.isDown()) flags |= ModNetwork.InputPacket.RIGHT;
            if (o.keyJump.isDown()) flags |= ModNetwork.InputPacket.UP;
            if (o.keySprint.isDown()) flags |= ModNetwork.InputPacket.DOWN;
        }

        resendTimer++;
        if (flags != lastInput || resendTimer >= 20) {
            lastInput = flags;
            resendTimer = 0;
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.InputPacket(flags));
        }
    }
}
