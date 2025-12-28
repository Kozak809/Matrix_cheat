package dev.mlml.matrix.mixin;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.event.events.ShouldChunkRender;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean setupTerrain_(boolean spectator) {
        ShouldChunkRender scr = new ShouldChunkRender(spectator);
        MatrixMod.eventManager.trigger(scr);
        return scr.isShouldRender();
    }
}
