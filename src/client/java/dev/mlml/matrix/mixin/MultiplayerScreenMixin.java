package dev.mlml.matrix.mixin;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.ResignSpam;
import dev.mlml.matrix.util.NickSpoofer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {

    @Unique
    private TextFieldWidget nicknameField;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        int fieldWidth = 100;
        int fieldHeight = 20;
        int x = this.width - fieldWidth - 5; // 5px padding from right
        int y = 5; // 5px padding from top

        this.nicknameField = new TextFieldWidget(this.textRenderer, x, y, fieldWidth, fieldHeight, Text.literal("Nickname"));
        this.nicknameField.setMaxLength(16);
        this.nicknameField.setText(this.client.getSession().getUsername());
        
        // Callback to update session nickname
        this.nicknameField.setChangedListener(newNick -> {
            NickSpoofer.fakeName = (newNick == null || newNick.isEmpty()) ? null : newNick;
        });

        this.addDrawableChild(this.nicknameField);

        // ResignSpam Stop Button
        ResignSpam resignSpam = (ResignSpam) ModuleManager.getModule(ResignSpam.class);
        if (resignSpam != null && resignSpam.isEnabled()) {
            int btnWidth = 100;
            int btnX = x - btnWidth - 5;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("§cStop Spamming"), button -> {
                resignSpam.setEnabled(false);
                this.client.setScreen(this); // Refresh UI to hide button
            }).dimensions(btnX, y, btnWidth, fieldHeight).build());
        }
    }
}
