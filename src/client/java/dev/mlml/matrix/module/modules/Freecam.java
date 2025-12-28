package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.BooleanSetting;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.event.Listener;
import dev.mlml.matrix.event.events.PacketEvent;
import dev.mlml.matrix.event.events.ShouldChunkRender;
import dev.mlml.matrix.event.events.ShouldNoClip;
import dev.mlml.matrix.misc.FakePlayerEntity;
import dev.mlml.matrix.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module {
    private final DoubleSetting speed = config.add(new DoubleSetting("Fly Speed", "The speed at which you fly", 0.05d, 0.01d, 5d, 1));
    private final ListSetting<Mode> mode = config.add(new ListSetting<>("Mode", "The mode of freecam", Mode.Spectator));
    private final BooleanSetting restrictInteractions = config.add(new BooleanSetting("Restrict Interactions", "Restrict interactions with the world", true));

    Vec3d previous;
    float pitch = 0f;
    float yaw = 0f;

    private FakePlayerEntity fakePlayer;

    public Freecam() {
        super("Freecam", "Allows you to move your camera freely", GLFW.GLFW_KEY_G);

        MatrixMod.eventManager.register(this);
    }

    @Override
    public String getStatus() {
        return String.format("%s, %s", mode.getValue()
                                           .name()
                                           .charAt(0), restrictInteractions.getValue() ? "Restrict" : "Unbound");
    }

    @Override
    public void onEnable() {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }

        previous = MatrixMod.mc.player.getPos();
        yaw = MatrixMod.mc.player.getYaw();
        pitch = MatrixMod.mc.player.getPitch();

        fakePlayer = new FakePlayerEntity();


    }

    @Override
    public void onDisable() {
        if (fakePlayer != null) {
            fakePlayer.resetPlayerPosition();
            fakePlayer.despawn();
        }

        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }

        if (previous == null) {
            return;
        }

        MatrixMod.mc.player.updatePositionAndAngles(previous.x, previous.y, previous.z, yaw, pitch);
        previous = null;
        yaw = 0f;
        pitch = 0f;

        if (mode.getValue() == Mode.Spectator) {
            MatrixMod.mc.player.getAbilities().flying = false;
            MatrixMod.mc.player.getAbilities().setFlySpeed(0.05f);
        }

        MatrixMod.mc.player.setVelocity(0, 0, 0);
    }

    @Override
    public void onTick() {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null || MatrixMod.mc.getNetworkHandler() == null) {
            return;
        }

        if (mode.getValue() == Mode.Spectator) {
            MatrixMod.mc.player.getAbilities().flying = true;
            MatrixMod.mc.player.getAbilities().setFlySpeed((float) (speed.getValue() + 0f));
            MatrixMod.mc.player.setSwimming(false);
        }
    }

    @Listener
    public void onPacketSend(PacketEvent.Sent pe) {
        if (!isEnabled()) {
            return;
        }

        if (pe.getPacket() instanceof PlayerMoveC2SPacket) {
            pe.setCancelled(true);
        }
        if (pe.getPacket() instanceof PlayerInputC2SPacket && restrictInteractions.getValue()) {
            pe.setCancelled(true);
        }
    }

    public void onShouldChunkRender(ShouldChunkRender scre) {
        if (!isEnabled() || mode.getValue() != Mode.Spectator) {
            return;
        }

        scre.setShouldRender(true);
    }

    public void onShouldNoclip(ShouldNoClip snce) {
        if (!isEnabled() || mode.getValue() != Mode.Spectator) {
            return;
        }

        snce.setShouldNoclip(true);
    }

    public enum Mode {
        Spectator,
        Phantom
    }
}
