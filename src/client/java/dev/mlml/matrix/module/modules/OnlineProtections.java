package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.module.Module;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

@Getter
public class OnlineProtections extends Module {
    private final boolean antiPacketKick = false;

    public OnlineProtections() {
        super("OnlineProtections", "Protects you from known exploits", GLFW.GLFW_KEY_UNKNOWN);
    }
}
