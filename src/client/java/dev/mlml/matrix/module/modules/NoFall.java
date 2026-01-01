package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.event.Listener;
import dev.mlml.matrix.event.events.PacketEvent;
import dev.mlml.matrix.mixin.IPlayerMoveC2SPacketMixin;
import dev.mlml.matrix.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {
    // It appears that as of 1.20.4, the client is now longer aware of fallDistance.
    private final ListSetting<Mode> mode = config.add(new ListSetting<>("Mode", "Method", Mode.OnGround));

    public NoFall() {
        super("NoFall", "Prevents fall damage", GLFW.GLFW_KEY_N);

        MatrixMod.eventManager.register(this);
    }

    @Override
    public String getStatus() {
        return mode.getValue().name();
    }

    @Override
    public void onTick() {
        // No tick logic needed for OnGround mode
    }

    private boolean shouldApply() {
        if (MatrixMod.mc.player == null) return false;

        if (MatrixMod.mc.player.isGliding()) {
            return false;
        }

        return MatrixMod.mc.player.getVelocity().y < -0.5;
    }

    @Listener
    public void onPacketSend(PacketEvent.Sent packetEvent) {
        if (!isEnabled() || MatrixMod.mc.player == null || MatrixMod.mc.getNetworkHandler() == null) {
            return;
        }

        if (packetEvent.getPacket() instanceof PlayerMoveC2SPacket && mode.getValue() == Mode.OnGround) {
            if (shouldApply()) {
                ((IPlayerMoveC2SPacketMixin) packetEvent.getPacket()).setOnGround(true);
            }
        }
    }

    public enum Mode {
        OnGround,
    }
}