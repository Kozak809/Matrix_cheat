package dev.mlml.matrix.mixin;

import dev.mlml.matrix.util.NickSpoofer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Session.class)
public class SessionMixin {
    @Inject(method = "getUsername", at = @At("HEAD"), cancellable = true)
    private void onGetUsername(CallbackInfoReturnable<String> info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.isInSingleplayer()) {
            return;
        }
        
        if (NickSpoofer.fakeName != null && !NickSpoofer.fakeName.isEmpty()) {
            info.setReturnValue(NickSpoofer.fakeName);
        }
    }
}
