package dev.mlml.matrix.mixin;

import dev.mlml.matrix.gui.ClickGuiScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {

    @Unique
    private Screen storedScreen;

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ClickGuiScreen) {
            this.storedScreen = client.currentScreen;
            client.currentScreen = null;
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void postTick(CallbackInfo ci) {
        if (this.storedScreen != null) {
            MinecraftClient.getInstance().currentScreen = this.storedScreen;
            this.storedScreen = null;
        }
    }
}
