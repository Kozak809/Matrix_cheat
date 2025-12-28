package dev.mlml.matrix.mixin;

import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.Passives;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    private int blockBreakingCooldown;

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;blockBreakingCooldown:I", opcode = Opcodes.GETFIELD, ordinal = 0))
    public int setBreakingCooldown(ClientPlayerInteractionManager manager) {
        Passives passives = (Passives) ModuleManager.getModule(Passives.class);
        if (passives != null && passives.isEnabled() && passives.getNoBreakDelay().getValue()) {
            return 0;
        }

        return this.blockBreakingCooldown;
    }
}
