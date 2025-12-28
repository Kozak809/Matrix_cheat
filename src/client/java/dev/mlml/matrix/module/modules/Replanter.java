package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import org.lwjgl.glfw.GLFW;

public class Replanter extends Module {
    public Replanter() {
        super("Replanter", "Automatically replants crops", GLFW.GLFW_KEY_KP_2);
    }

    private record NeedToReplant(BlockState bs) {}

    @Override
    public void onTick() {
        if (MatrixMod.mc.world == null || MatrixMod.mc.player == null) {
            return;
        }

        BlockState bs = MatrixMod.mc.world.getBlockState(MatrixMod.mc.player.getBlockPos());
//        bs.get
        Block block = bs.getBlock();
        if (block == null) {
            return;
        }

        if (block instanceof CropBlock) {
            CropBlock crop = (CropBlock) block;
            if (crop.isMature(MatrixMod.mc.world.getBlockState(MatrixMod.mc.player.getBlockPos()))) {
            }
        }
    }
}
