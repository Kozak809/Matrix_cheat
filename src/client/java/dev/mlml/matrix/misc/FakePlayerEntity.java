package dev.mlml.matrix.misc;

import dev.mlml.matrix.MatrixMod;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class FakePlayerEntity extends OtherClientPlayerEntity {
    public FakePlayerEntity() {
        super(Objects.requireNonNull(MatrixMod.mc.world), Objects.requireNonNull(MatrixMod.mc.player).getGameProfile());
        copyPositionAndRotation(MatrixMod.mc.player);

        copyInventory();
        copyPlayerModel(MatrixMod.mc.player, this);
        copyRotation();
        resetCapeMovement();

        spawn();
    }

    private void copyInventory() {
        if (MatrixMod.mc.player == null) {
            return;
        }

        getInventory().clone(MatrixMod.mc.player.getInventory());
    }

    private void copyPlayerModel(Entity from, Entity to) {
        DataTracker fromTracker = from.getDataTracker();
        DataTracker toTracker = to.getDataTracker();
        Byte playerModel = fromTracker.get(PlayerEntity.PLAYER_MODEL_PARTS);
        toTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
    }

    private void copyRotation() {
        if (MatrixMod.mc.player == null) {
            return;
        }

        headYaw = MatrixMod.mc.player.headYaw;
        bodyYaw = MatrixMod.mc.player.bodyYaw;
    }

    private void resetCapeMovement() {
        capeX = getX();
        capeY = getY();
        capeZ = getZ();
    }

    private void spawn() {
        if (MatrixMod.mc.world == null) {
            return;
        }

        MatrixMod.mc.world.addEntity(this);
    }

    public void despawn() {
        discard();
    }

    public void resetPlayerPosition() {
        if (MatrixMod.mc.player == null) {
            return;
        }
        MatrixMod.mc.player.refreshPositionAndAngles(getX(), getY(), getZ(), getYaw(), getPitch());
    }
}
