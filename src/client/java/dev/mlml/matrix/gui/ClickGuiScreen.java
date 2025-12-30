package dev.mlml.matrix.gui;

import dev.mlml.matrix.module.Module;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        if (panels.isEmpty()) {
            int x = 10;
            for (Module.ModuleType type : Module.ModuleType.values()) {
                if (type == Module.ModuleType.NONE) continue;
                
                panels.add(new CategoryPanel(type, x, 10));
                x += 110;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
         for (CategoryPanel panel : panels) {
             if (panel.isDragging) {
                 panel.x += (int) deltaX;
                 panel.y += (int) deltaY;
                 return true;
             }
         }
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryPanel panel : panels) {
             if (panel.keyPressed(keyCode, scanCode, modifiers)) {
                 return true;
             }
        }
        
        if (ModuleManager.getModule(ClickGui.class).getKeybind().matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int parseColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() == 3) {
            StringBuilder sb = new StringBuilder();
            for (char c : hex.toCharArray()) {
                sb.append(c).append(c);
            }
            hex = sb.toString();
        }
        try {
            int rgb = Integer.parseInt(hex, 16);
            return 0xFF000000 | rgb;
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF; // Fallback to white
        }
    }

    private class CategoryPanel {
        Module.ModuleType type;
        int x, y, width, height;
        boolean expanded = true;
        boolean isDragging = false;
        List<ModuleButton> buttons = new ArrayList<>();

        public CategoryPanel(Module.ModuleType type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = 100;
            this.height = 18;

            for (Module m : ModuleManager.getModules()) {
                if (m.getCategory() == type) {
                    buttons.add(new ModuleButton(m, this));
                }
            }
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            ClickGui clickGui = (ClickGui) ModuleManager.getModule(ClickGui.class);
            int bgColor = parseColor(clickGui.bgColor.getValue());
            int textColor = parseColor(clickGui.textColor.getValue());

            context.fill(x, y, x + width, y + height, bgColor);
            context.drawCenteredTextWithShadow(textRenderer, type.name(), x + width / 2, y + 5, textColor);

            if (expanded) {
                int yOffset = height;
                for (ModuleButton button : buttons) {
                    button.x = x;
                    button.y = y + yOffset;
                    button.render(context, mouseX, mouseY, delta);
                    yOffset += button.height;
                }
                // Draw outline
                context.drawBorder(x, y, width, yOffset, 0xFF000000);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHovered(mouseX, mouseY)) {
                if (button == 0) {
                    isDragging = true;
                    return true;
                } else if (button == 1) {
                    expanded = !expanded;
                    return true;
                }
            }
            if (expanded) {
                for (ModuleButton b : buttons) {
                    if (b.mouseClicked(mouseX, mouseY, button)) return true;
                }
            }
            return false;
        }

        public void mouseReleased(double mouseX, double mouseY, int button) {
            isDragging = false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
             if (expanded) {
                 for (ModuleButton b : buttons) {
                     if (b.keyPressed(keyCode, scanCode, modifiers)) return true;
                 }
             }
             return false;
        }

        private boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private class ModuleButton {
        Module module;
        CategoryPanel parent;
        int x, y, width, height;
        boolean binding = false;

        public ModuleButton(Module module, CategoryPanel parent) {
            this.module = module;
            this.parent = parent;
            this.width = parent.width;
            this.height = 16;
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            ClickGui clickGui = (ClickGui) ModuleManager.getModule(ClickGui.class);
            int activeColor = parseColor(clickGui.activeColor.getValue());
            int elemColor = parseColor(clickGui.elementColor.getValue());
            int textColor = parseColor(clickGui.textColor.getValue());

            int color = module.isEnabled() ? activeColor : elemColor;
            if (binding) color = 0xFFAAAA00;
            
            context.fill(x, y, x + width, y + height, color);
            context.drawTextWithShadow(textRenderer, module.getName(), x + 4, y + 4, textColor);
            
            if (binding) {
                context.drawTextWithShadow(textRenderer, "...", x + width - 15, y + 4, textColor);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHovered(mouseX, mouseY)) {
                if (button == 0) {
                    module.toggle();
                    return true;
                } else if (button == 1) {
                    // Right click could open settings, but for now let's use it for binding or create a new "Settings" window
                    // Since "configuring individual settings" is required, we should open the ConfigScreen for this module
                    client.setScreen(new ConfigScreen(ClickGuiScreen.this, module)); 
                    return true;
                } else if (button == 2) {
                    if (module.isBindable()) {
                        binding = !binding;
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (binding) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                    module.setBind(GLFW.GLFW_KEY_UNKNOWN);
                } else {
                    module.setBind(keyCode);
                }
                binding = false;
                return true;
            }
            return false;
        }

        private boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}
