package dev.mlml.matrix.mixin;

import com.mojang.authlib.GameProfile;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.Passives;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.modules.Speed;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world,
                                   GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!((Object) this instanceof ClientPlayerEntity player) || !player.equals(MatrixMod.mc.player)) return;

        Speed speed = ModuleManager.getModule(Speed.class);
        if (!speed.isEnabled()) return;

        switch (speed.getMode().getValue()) {
            case VanillaAlt -> {
                if (!player.isOnGround()) {
                    Vec3d movementVec = speed.getMovementVec(speed.getJumpLength().getValue() / 2);
                    if (movementVec != null) {
                        player.setVelocity(movementVec.x, player.getVelocity().y, movementVec.z);
                    }
                }
            }
            case BHop -> {
                if (player.isOnGround()) {
                    if (speed.getMovementVec(1) != null) {
                        player.jump();
                    }
                } else {
                    if (player.getVelocity().y < 0) {
                        player.setVelocity(player.getVelocity().multiply(1, 1.1, 1));
                    }
                }
            }
            case Legit -> {
                if (!MatrixMod.mc.options.forwardKey.isPressed() || MatrixMod.mc.options.backKey.isPressed()) return;
                if (player.isSneaking() || player.isClimbing() || !player.isOnGround() || player.horizontalCollision) return;
                player.setSprinting(true);
                player.jump();
            }
        }
    }

    @Override
    public double getBlockInteractionRange() {
        Passives passives = (Passives) ModuleManager.getModule(Passives.class);
        if (passives != null && passives.isEnabled() && passives.getReach().getValue()) {
            return passives.getBlockReachDistance().getValue();
        }
        return super.getBlockInteractionRange();
    }

    @Override
    public double getEntityInteractionRange() {
        Passives passives = ModuleManager.getModule(Passives.class);
        if (passives != null && passives.isEnabled() && passives.getReach().getValue()) {
            return passives.getEntityReachDistance().getValue();
        }
        return super.getEntityInteractionRange();
    }
}
