package dev.mlml.matrix.mixin;

import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.FullBright;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.mlml.matrix.module.modules.Zoom;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        Zoom zoom = ModuleManager.getModule(Zoom.class);
        float multiplier = zoom.getZoomMultiplier(tickDelta);
        if (multiplier > 1.0f || zoom.isEnabled()) {
            float result = cir.getReturnValue() / multiplier;
            cir.setReturnValue(result);
        }
    }

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
