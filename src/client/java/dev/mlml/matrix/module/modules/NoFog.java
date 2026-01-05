package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoFog extends Module {
    public NoFog() {
        super("NoFog", "Removes fog rendering", GLFW.GLFW_KEY_UNKNOWN);
    }
}
