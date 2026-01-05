package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.StringSetting;
import dev.mlml.matrix.gui.ClickGuiScreen;
import dev.mlml.matrix.module.Module;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class ClickGui extends Module {

    public final StringSetting bgColor = new StringSetting("BgColor", "Background Color (Hex)", "222222");
    public final StringSetting activeColor = new StringSetting("ActiveColor", "Active Color (Hex)", "00AA00");
    public final StringSetting elementColor = new StringSetting("ElementColor", "Element Color (Hex)", "444444");
    public final StringSetting textColor = new StringSetting("TextColor", "Text Color (Hex)", "FFFFFF");

    @Getter
    @Setter
    private ClickGuiScreen screen;

    public ClickGui() {
        super("ClickGUI", "Manages the visual style of the Click GUI", GLFW.GLFW_KEY_UNKNOWN);
        config.add(bgColor);
        config.add(activeColor);
        config.add(elementColor);
        config.add(textColor);
    }

    @Override
    public boolean isBindable() {
        return false;
    }

    @Override
    public void onEnable() {
        if (screen == null) {
            screen = new ClickGuiScreen();
        }

        if (MatrixMod.mc.currentScreen instanceof ClickGuiScreen) {
            MatrixMod.mc.setScreen(null);
        } else {
            MatrixMod.mc.setScreen(screen);
        }
        this.setEnabled(false);
    }
}
