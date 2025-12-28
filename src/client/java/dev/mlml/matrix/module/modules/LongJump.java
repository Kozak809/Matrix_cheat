package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.Module;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

public class LongJump extends Module {
    public LongJump() {
        super("LongJump", "Jumps longer", GLFW.GLFW_KEY_K);
    }

    @Override
    public void onTick() {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }

       if (MatrixMod.mc.options.jumpKey.isPressed()) {
           MatrixMod.mc.player.jump();
       }
    }
}
