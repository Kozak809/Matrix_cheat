package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.module.Module;
import org.lwjgl.glfw.GLFW;

public class DropFPS extends Module {
    public final DoubleSetting fps = config.add(new DoubleSetting("Target FPS", "FPS limit when unfocused", 12.0, 1.0, 60.0, 0));

    public DropFPS() {
        super("DropFPS", "Lowers FPS when the game is unfocused", GLFW.GLFW_KEY_UNKNOWN);
    }
}
