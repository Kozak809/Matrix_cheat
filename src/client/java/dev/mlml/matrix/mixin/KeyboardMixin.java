package dev.mlml.matrix.mixin;

import dev.mlml.matrix.gui.ClickGuiScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof ClickGuiScreen) {
            InputUtil.Key inputKey = InputUtil.fromKeyCode(key, scancode);
            boolean pressed = action == 1 || action == 2;
            KeyBinding.setKeyPressed(inputKey, pressed);
            if (pressed) {
                KeyBinding.onKeyPressed(inputKey);
            }
        }
    }
}
