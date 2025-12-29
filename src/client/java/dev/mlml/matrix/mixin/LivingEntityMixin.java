package dev.mlml.matrix.mixin;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.Passives;
import dev.mlml.matrix.module.modules.FullBright;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"), require = 0)
    boolean hasStatusEffect_(LivingEntity ent, RegistryEntry<StatusEffect> effect) {
        if (ent.equals(MatrixMod.mc.player) && ModuleManager.getModule(Passives.class).getNoLevitation()
                .getValue() && effect.matches(StatusEffects.LEVITATION)) {
            return false;
        } else {
            return ent.hasStatusEffect(effect);
        }
    }

    @Inject(at = @At("HEAD"), method = "hasStatusEffect", cancellable = true)
    private void onHasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> info) {
        if (FullBright.shouldReturnNightVisionEffect && (Object) this == MatrixMod.mc.player && effect.matches(StatusEffects.NIGHT_VISION)) {
            info.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "getStatusEffect", cancellable = true)
    private void onGetStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> info) {
        if (FullBright.shouldReturnNightVisionEffect && (Object) this == MatrixMod.mc.player && effect.matches(StatusEffects.NIGHT_VISION)) {
            info.setReturnValue(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000));
        }
    }
}
