package dev.mlml.matrix.module;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.Config;
import dev.mlml.matrix.gui.ConfigScreen;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final KeyBinding keybind;
    @Getter
    private final Map<Integer, Boolean> modifierKeyStates;
    @Getter
    protected Config config;
    @Getter
    private boolean enabled;
    @Getter
    @Setter
    private ModuleType category = ModuleType.NONE;

    public Module(String name, String description, int key) {
        this.name = name;
        this.description = description;

        keybind = new KeyBinding("key.MatrixMod." + name.replaceAll(" ", "").toLowerCase() + "_toggle", key, "category.MatrixMod");

        config = new Config();

        modifierKeyStates = new HashMap<>();
        initializeStates();
    }

    public void setBind(int keyCode) {
        this.keybind.setBoundKey(InputUtil.fromKeyCode(keyCode, 0));
    }

    private void initializeStates() {
        int[] keys = new int[]{GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT};

        for (int key : keys) {
            modifierKeyStates.put(key, false);
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void update(MinecraftClient mc) {
        initializeStates();
        if (keybind.wasPressed()) {
            toggle();
        }


        if (!isEnabled()) {
            return;
        }
        onTick();
    }

    public void onTick() {
    }

    public void onWorldTick(ClientWorld clientWorld) {
    }

    public void onFastTick() {
    }

    public void worldDraw(WorldRenderContext worldRenderContext) {
        if (!isEnabled()) {
            return;
        }
        onWorldRender(worldRenderContext);
    }

    public void onWorldRender(WorldRenderContext worldRenderContext) {
    }

    public void renderDraw(DrawContext drawContext, RenderTickCounter tickDelta) {
        if (!isEnabled()) {
            return;
        }
        onRender(drawContext, tickDelta);
    }

    public void onRender(DrawContext drawContext, RenderTickCounter tickDelta) {
    }

    public String getStatus() {
        return "";
    }

    public enum ModuleType {
        COMBAT,
        MOVEMENT,
        RENDER,
        PLAYER,
        WORLD,
        MISC,
        META,
        NONE
    }
}
