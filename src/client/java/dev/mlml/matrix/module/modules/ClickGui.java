package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.gui.ClickGuiScreen;
import dev.mlml.matrix.module.Module;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class ClickGui extends Module {
    public final Map<ModuleType, DoubleSetting> xSettings = new HashMap<>();
    public final Map<ModuleType, DoubleSetting> ySettings = new HashMap<>();

    public ClickGui() {
        super("ClickGUI", "Opens the Click GUI", GLFW.GLFW_KEY_RIGHT_SHIFT);

        int x = 10;
        for (ModuleType type : ModuleType.values()) {
            if (type == ModuleType.NONE) continue;

            DoubleSetting xSet = config.add(new DoubleSetting(type.name() + "X", "X position of " + type.name(), (double) x, 0.0, 3000.0, 0));
            DoubleSetting ySet = config.add(new DoubleSetting(type.name() + "Y", "Y position of " + type.name(), 10.0, 0.0, 3000.0, 0));

            xSettings.put(type, xSet);
            ySettings.put(type, ySet);

            x += 110;
        }
    }

    @Override
    public void onEnable() {
        if (MatrixMod.mc.currentScreen == null || !(MatrixMod.mc.currentScreen instanceof ClickGuiScreen)) {
            MatrixMod.mc.setScreen(new ClickGuiScreen());
        }
        this.setEnabled(false);
    }
}
