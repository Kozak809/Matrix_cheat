package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.config.BooleanSetting;
import dev.mlml.matrix.module.Module;
import org.lwjgl.glfw.GLFW;

public class AntiKick extends Module {
    public final BooleanSetting autoReconnect = new BooleanSetting("AutoReconnect", "Automatically reconnects to the server upon kick", false);
    public final BooleanSetting silentMode = new BooleanSetting("SilentMode", "Suppresses connecting screens", true);

    public AntiKick() {
        super("AntiKick", "Prevents being kicked from the server (Ghost Mode)", GLFW.GLFW_KEY_UNKNOWN);
        this.setCategory(ModuleType.MISC);
        config.add(autoReconnect);
        config.add(silentMode);
    }
}
