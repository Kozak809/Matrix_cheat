package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.module.Module;
import org.lwjgl.glfw.GLFW;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Automatically respawns when you die", GLFW.GLFW_KEY_UNKNOWN);
    }

    // Logic is handled in MinecraftClientMixin to be instant
}