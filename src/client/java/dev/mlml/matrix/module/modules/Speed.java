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
    private final DoubleSetting speedIncrease = config.add(new DoubleSetting("Speed Increase", "Speed multiplier while running", 1.0, 0.0, 20.0, 1));
    @Getter
    private final DoubleSetting jumpLength = config.add(new DoubleSetting("Jump Length", "Jump length (Distance/Air)", 2.0, 0.0, 20.0, 1));
    
    @Getter
    private final DoubleSetting bhopDistance = config.add(new DoubleSetting("BHop Distance", "Distance to jump in BHop", 2.0, 0.0, 20.0, 1));

    public Speed() {
        super("Speed", "Go fast", GLFW.GLFW_KEY_C);
        
        speedIncrease.setIsVisible(() -> mode.getValue() == Mode.Vanilla || mode.getValue() == Mode.VanillaAlt);
        jumpLength.setIsVisible(() -> mode.getValue() == Mode.Vanilla || mode.getValue() == Mode.VanillaAlt);
        bhopDistance.setIsVisible(() -> mode.getValue() == Mode.BHop);
    }

    @Override
    public void onTick() {
        // All logic is handled in Mixins (LivingEntityMixin and ClientPlayerEntityMixin) 
        // for better responsiveness and to fix the "first jump" issue.
    }

    @Override
    public String getStatus() {
        switch (mode.getValue()) {
            case Vanilla -> {
                return "V: " + speedIncrease.getValue() + "/" + jumpLength.getValue();
            }
            case VanillaAlt -> {
                return "V-A: " + speedIncrease.getValue() + "/" + jumpLength.getValue();
            }
            case BHop -> {
                return "B: " + bhopDistance.getValue();
            }
            case Legit -> {
                return "Legit";
            }
        }
        return "";
    }

    public Vec3d getMovementVec(double speed) {
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
            return movementVec.normalize().multiply(speed);
        }
        return null;
    }

    public enum Mode {
        Vanilla, VanillaAlt, BHop, Legit,
    }
}