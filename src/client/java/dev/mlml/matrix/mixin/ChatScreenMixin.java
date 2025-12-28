package dev.mlml.matrix.mixin;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.event.events.ChatSendEvent;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(at = @At("HEAD"), method = "sendMessage(Ljava/lang/String;Z)V", cancellable = true)
    public void sendMessage_(String chatText, boolean addToHistory, CallbackInfo ci) {
        ChatSendEvent chatSendEvent = new ChatSendEvent(chatText);
        MatrixMod.eventManager.trigger(chatSendEvent);

        if (chatSendEvent.isCancelled()) {
            ci.cancel();
            return;
        }

        String newMessage = chatSendEvent.getMessage();
        if (addToHistory) {
            MatrixMod.mc.inGameHud.getChatHud().addToMessageHistory(newMessage);
        }

        if (MatrixMod.mc.player == null) {
            return;
        }

        if (newMessage.startsWith("/")) {
            MatrixMod.mc.player.networkHandler.sendChatCommand(newMessage.substring(1));
        } else {
            MatrixMod.mc.player.networkHandler.sendChatMessage(newMessage);
        }

        ci.cancel();
    }
}
