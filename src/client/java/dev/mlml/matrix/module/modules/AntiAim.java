package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.misc.Rotations;
import dev.mlml.matrix.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class AntiAim extends Module {
    private final ListSetting<Mode> yawMode = config.add(new ListSetting<>("Yaw Mode", "How to handle yaw", Mode.Static));
    private final ListSetting<Pitch> pitchMode = config.add(new ListSetting<>("Pitch Mode", "How to handle pitch", Pitch.Static));
    private final DoubleSetting yawOffset = config.add(new DoubleSetting("Yaw", "Yaw value for static", 0d, -180d, 180d, 0));
    private final DoubleSetting pitchOffset = config.add(new DoubleSetting("Pitch", "Pitch value for static", 0d, -90d, 90d, 0));

    public AntiAim() {
        super("AntiAim", "Hides your head rotation in silly ways", GLFW.GLFW_KEY_SEMICOLON);
    }

    private Perspective previousPerspective = Perspective.FIRST_PERSON;
    private float previousYaw = 0;
    private float previousPitch = 0;

    @Override
    public void onEnable() {
        if (MatrixMod.mc.player == null) {
            return;
        }

        previousPerspective = MatrixMod.mc.options.getPerspective();
        previousYaw = MatrixMod.mc.player.getYaw();
        previousPitch = MatrixMod.mc.player.getPitch();
    }

    @Override
    public void onDisable() {
        if (MatrixMod.mc.player == null) {
            return;
        }

        MatrixMod.mc.options.setPerspective(previousPerspective);

        MatrixMod.mc.player.setYaw(previousYaw);
        MatrixMod.mc.player.setPitch(previousPitch);
    }

    @Override
    public void onTick() {
        float desiredYaw;

        switch (yawMode.getValue()) {
            case Spin:
                desiredYaw = (float) (System.currentTimeMillis() % 360);
                break;
            case Jitter:
                if (System.currentTimeMillis() % 1000 < 500) {
                    desiredYaw = 180;
                } else {
                    desiredYaw = -180;
                }
                break;
            default:
                desiredYaw = 0;
                break;
        }
        desiredYaw = MathHelper.wrapDegrees(desiredYaw + yawOffset.getValue().floatValue());
        Rotations.setClientYaw(desiredYaw);

        float desiredPitch;
        float lastPitch = Rotations.getClientPitch();
        desiredPitch = switch (pitchMode.getValue()) {
            case Decrease -> lastPitch - 5;
            case Increase -> lastPitch + 5;
            case Random -> (float) (Math.random() * 180 - 90);
            default -> 0;
        };
        desiredPitch = MathHelper.wrapDegrees(desiredPitch + pitchOffset.getValue().floatValue());
        Rotations.setClientPitch(desiredPitch);

        if (MatrixMod.mc.player == null) {
            return;
        }

        Objects.requireNonNull(MatrixMod.mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(desiredYaw, desiredPitch, Objects.requireNonNull(MatrixMod.mc.player).isOnGround(), MatrixMod.mc.player.horizontalCollision));
    }

    @Override
    public void onWorldRender(WorldRenderContext wrc) {
        MatrixMod.mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    public enum Mode {
        Spin, Jitter, Random, Static;
    }

    public enum Pitch {
        Decrease, Increase, Random, Static;
    }
}
