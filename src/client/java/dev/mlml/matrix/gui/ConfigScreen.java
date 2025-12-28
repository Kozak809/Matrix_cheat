package dev.mlml.matrix.gui;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.GenericSetting;
import dev.mlml.matrix.module.Module;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int GAPS = 4;

    private static final int MAX_WIDTH = MatrixMod.mc.getWindow().getScaledWidth() / 2;

    @Getter
    private final Module module;
    private final Screen parent;
    List<SettingLabel> texts = new ArrayList<>();
    
    private boolean binding = false;
    private ButtonWidget bindButton;

    public ConfigScreen(Screen parent, Module module) {
        super(Text.literal(module.getName()));
        this.parent = parent;
        this.module = module;
    }
    
    public ConfigScreen(Module module) {
        this(null, module);
    }

    @Override
    public void close() {
        if (parent != null) {
            client.setScreen(parent);
        } else {
            super.close();
        }
    }

    @Override
    public void init() {
        super.init();

        int x = GAPS;
        int y = GAPS;
        
        // Bind button
        bindButton = ButtonWidget.builder(getBindText(), (button) -> {
            binding = !binding;
            button.setMessage(getBindText());
        }).dimensions(width - 100 - GAPS, GAPS, 100, 20).build();
        addDrawableChild(bindButton);

        List<GenericSetting<?>> settings = module.getConfig().getSettings();

        int maxHeight = 0;
        for (GenericSetting<?> s : settings) {
            ClickableWidget e = s.getAsWidget();

            if (x + e.getWidth() > MAX_WIDTH) {
                x = GAPS;
                y += maxHeight + GAPS;
                maxHeight = 0;
            }

            texts.add(new SettingLabel(s.getLabel(), x, y));

            if (e.getHeight() + textRenderer.fontHeight + GAPS / 2 > maxHeight) {
                maxHeight = e.getHeight() + textRenderer.fontHeight + GAPS / 2;
            }

            e.setPosition(x, y + textRenderer.fontHeight + GAPS / 2);

            x += e.getWidth() + GAPS;

            addDrawableChild(e);
        }
    }
    
    private Text getBindText() {
        if (binding) {
            return Text.literal("Listening...");
        }
        return Text.literal("Bind: " + module.getKeybind().getBoundKeyLocalizedText().getString());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setBind(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                module.setBind(keyCode);
            }
            binding = false;
            bindButton.setMessage(getBindText());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (SettingLabel t : texts) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(t.text), t.x + textRenderer.getWidth(t.text) / 2, t.y, 0xffffff);
        }
    }

    private static class SettingLabel {
        String text;
        int x;
        int y;

        public SettingLabel(String text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }
}
