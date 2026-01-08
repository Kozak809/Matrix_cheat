package dev.mlml.matrix.mixin;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.gui.ChatHelper;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.AntiKick;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onDisconnect", at = @At("HEAD"), cancellable = true)
    private void onDisconnect(DisconnectS2CPacket packet, CallbackInfo ci) {
        AntiKick antiKick = (AntiKick) ModuleManager.getModule(AntiKick.class);
        if (antiKick != null && antiKick.isEnabled()) {
            if (antiKick.autoReconnect.getValue()) {
                ServerInfo server = MatrixMod.mc.getCurrentServerEntry();
                if (server != null) {
                    ChatHelper.message("§c[AntiKick] §fReconnecting to " + server.name + "...");
                    MatrixMod.mc.execute(() -> {
                        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), MatrixMod.mc, ServerAddress.parse(server.address), server, false, null);
                    });
                    ci.cancel();
                    return;
                }
            }
            
            ChatHelper.message("§c[AntiKick] §fServer attempted to kick you. Reason:");
            ChatHelper.message("§7" + packet.reason().getString());
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnected", at = @At("HEAD"), cancellable = true)
    private void onDisconnected(DisconnectionInfo info, CallbackInfo ci) {
        AntiKick antiKick = (AntiKick) ModuleManager.getModule(AntiKick.class);
        if (antiKick != null && antiKick.isEnabled()) {
            if (antiKick.autoReconnect.getValue()) {
                ServerInfo server = MatrixMod.mc.getCurrentServerEntry();
                if (server != null) {
                    ChatHelper.message("§c[AntiKick] §fReconnecting to " + server.name + "...");
                    MatrixMod.mc.execute(() -> {
                        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), MatrixMod.mc, ServerAddress.parse(server.address), server, false, null);
                    });
                    ci.cancel();
                    return;
                }
            }

            ChatHelper.message("§c[AntiKick] §fConnection lost/closed. Reason:");
            ChatHelper.message("§7" + info.reason().getString());
            ci.cancel();
        }
    }
}
