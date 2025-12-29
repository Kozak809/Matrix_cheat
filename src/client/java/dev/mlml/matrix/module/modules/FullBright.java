package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.module.Module;
import org.lwjgl.glfw.GLFW;

public class FullBright extends Module {
    public static boolean shouldReturnNightVisionEffect = false;

    public FullBright() {
        super("FullBright", "Tricks the game into thinking you have night vision", GLFW.GLFW_KEY_B);
    }
}
