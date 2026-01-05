package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.config.BooleanSetting;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.module.Module;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class Zoom extends Module {
    @Getter
    private final ListSetting<Mode> mode = config.add(new ListSetting<>("Mode", "Zoom mode", Mode.Instant));
    @Getter
    private final DoubleSetting zoomLevel = config.add(new DoubleSetting("Zoom Level", "Zoom amount", 4.0, 1.0, 100.0, 1));
    @Getter
    private final BooleanSetting hold = config.add(new BooleanSetting("Hold", "Zoom only while key is pressed", false, List.of(this::setHoldMode)));

    private float currentZoom = 1.0f;

    public Zoom() {
        super("Zoom", "Zoom in", GLFW.GLFW_KEY_C);
    }

    public float getZoomMultiplier(float tickDelta) {
        float target = isEnabled() ? zoomLevel.getValue().floatValue() : 1.0f;
        
        if (mode.getValue() == Mode.Instant) {
            currentZoom = target;
        } else {
            // Smooth transition like spyglass
            currentZoom += (target - currentZoom) * 0.1f * tickDelta * 10;
        }
        
        return currentZoom;
    }

    public enum Mode {
        Instant, Smooth
    }
}
