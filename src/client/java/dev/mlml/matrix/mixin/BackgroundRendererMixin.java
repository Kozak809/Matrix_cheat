package dev.mlml.matrix.mixin;

import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.NoFog;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Fog;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (ModuleManager.getModule(NoFog.class).isEnabled()) {
            cir.setReturnValue(Fog.DUMMY);
        }
    }
}
