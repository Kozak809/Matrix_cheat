package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.module.Module;
import lombok.Getter;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {
    @Getter
    private final ListSetting<Mode> mode = config.add(new ListSetting<>("Mode", "Speed mode", Mode.Vanilla));
    @Getter
    private final DoubleSetting vanillaSpeed = config.add(new DoubleSetting("Vanilla Speed", "Speed in vanilla mode", 2.0, 0.0, 20.0, 1));
    private final DoubleSetting bhopDistance = config.add(new DoubleSetting("BHop Distance", "Distance to jump", 2.0, 0.0, 20.0, 1));

    public Speed() {
        super("Speed", "Go fast", GLFW.GLFW_KEY_C);
    }

    @Override
    public void onTick() {
        if (MatrixMod.mc.player == null) {
            return;
        }

        switch (mode.getValue()) {
            case BHop -> doBhop();
            case Legit -> doLegit();
        }
    }

    @Override
    public String getStatus() {
        switch (mode.getValue()) {
            case Vanilla -> {
                return "SPD: " + vanillaSpeed.getValue();
            }
            case BHop -> {
                return "DST: " + bhopDistance.getValue();
            }
            case Legit -> {
                return "Legit";
            }
        }
        return "";
    }

    private void doBhop() {
        Vec3d movementVec = new Vec3d(0, 0, 0);

        if (MatrixMod.mc.options.forwardKey.isPressed()) {
            movementVec = movementVec.add(Vec3d.fromPolar(0, MatrixMod.mc.player.getYaw()));
        }
        if (MatrixMod.mc.options.backKey.isPressed()) {
            movementVec = movementVec.add(Vec3d.fromPolar(0, MatrixMod.mc.player.getYaw() - 180));
        }
        if (MatrixMod.mc.options.leftKey.isPressed()) {
            movementVec = movementVec.add(Vec3d.fromPolar(0, MatrixMod.mc.player.getYaw() - 90));
        }
        if (MatrixMod.mc.options.rightKey.isPressed()) {
            movementVec = movementVec.add(Vec3d.fromPolar(0, MatrixMod.mc.player.getYaw() + 90));
        }

        if (movementVec.lengthSquared() > 0) {
            movementVec = movementVec.normalize().multiply(bhopDistance.getValue() / 2);
        } else {
            return;
        }

        if (!MatrixMod.mc.player.isOnGround()) {
            if (MatrixMod.mc.player.getVelocity().y < 0) {
                MatrixMod.mc.player.setVelocity(MatrixMod.mc.player.getVelocity().multiply(1, 1.1, 1));
            }
            return;
        }

        Vec3d hop = new Vec3d(movementVec.x, 0.42, movementVec.z);
        MatrixMod.mc.player.setVelocity(hop);
    }

    private void doLegit() {
        if (!MatrixMod.mc.options.forwardKey.isPressed() || MatrixMod.mc.options.backKey.isPressed()) {
            return;
        }

        if (MatrixMod.mc.player.isSneaking() || MatrixMod.mc.player.isClimbing() || !MatrixMod.mc.player.isOnGround() || MatrixMod.mc.player.horizontalCollision) {
            return;
        }

        MatrixMod.mc.player.setSprinting(true);
        MatrixMod.mc.player.jump();
    }

    public enum Mode {
        Vanilla, BHop, Legit,
    }
}
