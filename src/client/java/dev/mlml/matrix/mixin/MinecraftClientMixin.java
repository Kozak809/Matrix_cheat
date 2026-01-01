package dev.mlml.matrix.mixin;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.gui.ClickGuiScreen;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.AutoRespawn;
import dev.mlml.matrix.module.modules.DropFPS;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "method_47599", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> info) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        DropFPS dropFPS = (DropFPS) ModuleManager.getModule(DropFPS.class);

        if (dropFPS != null && dropFPS.isEnabled()) {
             long handle = client.getWindow().getHandle();
             if (GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == 0) {
                 info.setReturnValue(dropFPS.fps.getValue().intValue());
             }
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInputEvents(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.currentScreen instanceof ClickGuiScreen) {
             while (client.options.attackKey.wasPressed()) {
                 ((IMinecraftClientMixin) client).invokeDoAttack();
             }
             while (client.options.useKey.wasPressed()) {
                 ((IMinecraftClientMixin) client).invokeDoItemUse();
             }
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) {
            AutoRespawn autoRespawn = (AutoRespawn) ModuleManager.getModule(AutoRespawn.class);
            if (autoRespawn != null && autoRespawn.isEnabled()) {
                if (MatrixMod.mc.player != null) {
                    MatrixMod.mc.player.requestRespawn();
                    ci.cancel();
                }
            }
        }
    }
}