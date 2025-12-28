package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.gui.TextFormatter;
import dev.mlml.matrix.module.Module;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

public class EdgeJump extends Module {
    private int displayTicks = 10;

    public EdgeJump() {
        super("EdgeJump", "Jumps on the edge of blocks", GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (displayTicks < 10) {
            displayTicks++;
        }
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }
        if (!MatrixMod.mc.player.isOnGround() || MatrixMod.mc.player.isSneaking()) {
            return;
        }

        Box bounding = MatrixMod.mc.player.getBoundingBox();
        bounding = bounding.offset(0, -0.5, 0);
        bounding = bounding.expand(-0.001, 0, -0.001);
        if (!MatrixMod.mc.world.getBlockCollisions(MatrixMod.mc.player, bounding).iterator().hasNext()) {
            MatrixMod.mc.player.jump();
            displayTicks = 0;
        }
    }

    @Override
    public String getStatus() {
        return displayTicks < 10 ? TextFormatter.format("%1Last: %s", TextFormatter.Code.WHITE, displayTicks) : "";
    }
}
