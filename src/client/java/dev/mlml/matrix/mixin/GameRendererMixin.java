package dev.mlml.matrix.mixin;

import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.FullBright;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(at = @At("HEAD"), method = "renderWorld")
    private void onBeforeRenderWorld(RenderTickCounter tickCounter, CallbackInfo info) {
        if (ModuleManager.getModule(FullBright.class).isEnabled()) {
            FullBright.shouldReturnNightVisionEffect = true;
        }
    }

    @Inject(at = @At("TAIL"), method = "renderWorld")
    private void onAfterRenderWorld(RenderTickCounter tickCounter, CallbackInfo info) {
        FullBright.shouldReturnNightVisionEffect = false;
    }
}
