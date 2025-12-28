package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.event.Listener;
import dev.mlml.matrix.event.events.PacketEvent;
import dev.mlml.matrix.module.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class AtoB extends Module {
    private final ListSetting<Mode> mode = config.add(new ListSetting<>("Mode", "A to B mode", Mode.Delay));
    private final DoubleSetting maxSize = config.add(new DoubleSetting("Max Size", "Max size of the queue", 100d, 20d, 200d, 0));

    private final List<Packet<?>> queue = new ArrayList<>();

    public AtoB() {
        super("AtoB", "Drop packets from A to B", GLFW.GLFW_KEY_X);

        MatrixMod.eventManager.register(this);
    }

    @Override
    public String getStatus() {
        switch (mode.getValue()) {
            case Delay -> {
                return "DLY: " + queue.size();
            }
            case Drop -> {
                return "DRP";
            }
        }
        return "";
    }

    @Override
    public void onDisable() {
        if (MatrixMod.mc.player == null || MatrixMod.mc.getNetworkHandler() == null) {
            queue.clear();
            return;
        }
        for (Packet<?> packet : queue.toArray(new Packet<?>[0])) {
            MatrixMod.mc.getNetworkHandler().sendPacket(packet);
        }
        queue.clear();
    }

    @Listener
    public void onSendPacket(PacketEvent.Sent pe) {
        if (!isEnabled()) {
            return;
        }

        if (pe.getPacket() instanceof KeepAliveC2SPacket || pe.getPacket() instanceof CommonPongC2SPacket) {
            return;
        }
        pe.cancel();
        if (mode.getValue() == Mode.Delay) {
            queue.add(pe.getPacket());
            if (queue.size() > maxSize.getValue()) {
                setEnabled(false);
            }
        }
    }

    public enum Mode {
        Delay, Drop
    }
}
