package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityPose;
import org.lwjgl.glfw.GLFW;

public class Crawl extends Module {
    private final ListSetting<Mode> mode = config.add(new ListSetting<>("Mode", "Activation mode", Mode.Hold));

    public Crawl() {
        super("Crawl", "Forces the player to crawl", GLFW.GLFW_KEY_K);
        setHoldMode(true);
    }

    @Override
    public void update(MinecraftClient mc) {
        setHoldMode(mode.getValue() == Mode.Hold);
        super.update(mc);
    }

    @Override
    public void onTick() {
        if (MatrixMod.mc.player != null) {
            MatrixMod.mc.player.setSwimming(true);
            MatrixMod.mc.player.setPose(EntityPose.SWIMMING);
        }
    }

    @Override
    public void onDisable() {
        if (MatrixMod.mc.player != null) {
            MatrixMod.mc.player.setSwimming(false);
        }
    }

    @Override
    public String getStatus() {
        return mode.getValue().name();
    }

    public enum Mode {
        Toggle,
        Hold
    }
}