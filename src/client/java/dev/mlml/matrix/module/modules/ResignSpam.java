package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.module.Module;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ResignSpam extends Module {
    public final DoubleSetting leaveDelay = new DoubleSetting("LeaveDelay", "Delay before leaving (sec)", 3.0, 0.05, 10.0, 2);

    private long joinTime = 0;

    public ResignSpam() {
        super("ResignSpam", "Automatically leaves and rejoins the server loop", GLFW.GLFW_KEY_UNKNOWN);
        this.setCategory(ModuleType.MISC);
        config.add(leaveDelay);
    }

    @Override
    public void onEnable() {
        joinTime = System.currentTimeMillis();
    }

    @Override
    public void onTick() {
        // Only run if we are fully connected (player exists)
        if (MatrixMod.mc.player != null && MatrixMod.mc.getNetworkHandler() != null) {
            if (joinTime == 0) {
                 joinTime = System.currentTimeMillis();
            }
            
            // Check delay
            if (System.currentTimeMillis() - joinTime >= leaveDelay.getValue() * 1000) {
                // Disconnect
                MatrixMod.mc.getNetworkHandler().getConnection().disconnect(Text.literal("Resign Spam"));
                joinTime = 0; // Reset logic
            }
        } else {
            // If not connected, reset join time so it starts counting when we spawn
            joinTime = 0;
        }
    }
}
