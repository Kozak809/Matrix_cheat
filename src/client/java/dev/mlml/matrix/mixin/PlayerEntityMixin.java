package dev.mlml.matrix.mixin;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.event.events.ShouldNoClip;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.Speed;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z", opcode = Opcodes.PUTFIELD))
    void noClip_(PlayerEntity playerEntity, boolean value) {
        ShouldNoClip snc = new ShouldNoClip(playerEntity, playerEntity.isSpectator());
        MatrixMod.eventManager.trigger(snc);
        playerEntity.noClip = snc.isShouldNoclip();
    }

    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    void writeSpeed(CallbackInfoReturnable<Float> cir) {
        Speed speed = ModuleManager.getModule(Speed.class);
        if (!speed.isEnabled() || !equals(MatrixMod.mc.player) || !speed.getMode().getValue().equals(Speed.Mode.Vanilla)) {
            return;
        }
        cir.setReturnValue((float) (cir.getReturnValue() * speed.getVanillaSpeed().getValue()));
    }
}
