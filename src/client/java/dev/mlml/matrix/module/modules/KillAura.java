package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.BooleanSetting;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KillAura extends Module {
    final DoubleSetting range = config.add(new DoubleSetting("Range", "How far to attack", 5d, 1d, 7d, 1));
    final BooleanSetting multiMode = config.add(new BooleanSetting("Multi Mode", "Attack multiple entities", false));
    final DoubleSetting multiAmount = config.add(new DoubleSetting("Multi Amount", "How many entities to attack", 3d, 1d, 100d, 1));
    final BooleanSetting players = config.add(new BooleanSetting("Players", "Attack players", true));
    final BooleanSetting mobs = config.add(new BooleanSetting("Mobs", "Attack mobs", true));
    final BooleanSetting animals = config.add(new BooleanSetting("Animals", "Attack animals", true));
    final BooleanSetting others = config.add(new BooleanSetting("Others", "Attack others", true));
    final ListSetting<AttackPriority> attackPrio = config.add(new ListSetting<>("Mode", "How to order entities", AttackPriority.Near));
    final DoubleSetting additionalDelay = config.add(new DoubleSetting("Additional Delay", "Additional delay between hits", 0d, 0d, 1000d, 1));
    final BooleanSetting lookingCheck = config.add(new BooleanSetting("\"Looking at\" check", "Check if entity is looked at", false));

    public KillAura() {
        super("KillAura", "Automatically attacks entities", GLFW.GLFW_KEY_Y);
    }

    private long lastAttack = 0;

    @Override
    public String getStatus() {
        return String.format("<%.1f; %s; ", range.getValue(), multiMode.getValue() ? "Multi: " + multiAmount.getValue() : "Single") + (players.getValue() ? "P" : "") + (mobs.getValue() ? "M" : "") + (animals.getValue() ? "A" : "") + (others.getValue() ? "O" : "");
    }

    private double getYawDelta(LivingEntity le) {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return 999f;
        }

        double pYaw = MatrixMod.mc.player.getYaw();
        double deltaX = le.getX() - MatrixMod.mc.player.getX();
        double deltaZ = le.getZ() - MatrixMod.mc.player.getZ();
        double targetYaw = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90f;

        return Math.abs(targetYaw - pYaw);
    }

    private List<LivingEntity> sortTargets(List<LivingEntity> targets) {
        return switch (attackPrio.getValue()) {
            case Near ->
                    targets.stream().sorted(Comparator.comparingDouble(a -> a.distanceTo(MatrixMod.mc.player))).collect(Collectors.toList());
            case Far ->
                    targets.stream().sorted(Comparator.comparingDouble(a -> -a.distanceTo(MatrixMod.mc.player))).collect(Collectors.toList());
            case LowHealth ->
                    targets.stream().sorted(Comparator.comparingDouble(a -> a.getHealth())).collect(Collectors.toList());
            case HighHealth ->
                    targets.stream().sorted(Comparator.comparingDouble(a -> -a.getHealth())).collect(Collectors.toList());
            case Yaw ->
                    targets.stream().sorted(Comparator.comparingDouble(a -> getYawDelta(a))).collect(Collectors.toList());
            case YawInverse ->
                    targets.stream().sorted(Comparator.comparingDouble(a -> -getYawDelta(a))).collect(Collectors.toList());
            case Random ->
                    targets.stream().sorted(Comparator.comparingDouble(a -> Math.random())).collect(Collectors.toList());
        };
    }

    private List<LivingEntity> getTargets() {
        return new ArrayList<>(StreamSupport.stream(MatrixMod.mc.world.getEntities().spliterator(), false).filter(e -> e instanceof LivingEntity).filter(e -> !e.getUuid().equals(MatrixMod.mc.player.getUuid())).filter(Entity::isAlive).filter(Entity::isAttackable).filter(e -> {
            if (players.getValue() && e instanceof PlayerEntity) {
                return true;
            }

            if (mobs.getValue() && e instanceof Monster) {
                return true;
            }

            if (animals.getValue() && e instanceof PassiveEntity) {
                return true;
            }

            return others.getValue();
        }).filter(e -> {
            if (lookingCheck.getValue()) {
                Vec3d rayDir = MatrixMod.mc.player.getRotationVec(0).multiply(range.getValue());
                EntityHitResult ehr = ProjectileUtil.raycast(
                        MatrixMod.mc.player,
                        MatrixMod.mc.player.getCameraPosVec(0),
                        MatrixMod.mc.player.getCameraPosVec(0).add(rayDir),
                        MatrixMod.mc.player.getBoundingBox().stretch(rayDir).expand(1, 1, 1),
                        Entity::isAttackable,
                        range.getValue() * range.getValue()
                );
                if (Objects.isNull(ehr) || !ehr.getEntity().equals(e)) {
                    return false;
                }
            }
            return true;
        }).filter(e -> e.distanceTo(MatrixMod.mc.player) <= range.getValue()).map(e -> (LivingEntity) e).collect(Collectors.toList()));
    }

    @Override
    public void onTick() {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }

        List<LivingEntity> targets = getTargets();
        if (targets.isEmpty()) {
            return;
        }

        if (multiMode.getValue()) {
            targets = sortTargets(targets).stream().limit(multiAmount.getValue().intValue()).collect(Collectors.toList());
        } else {
            targets = sortTargets(targets).stream().limit(1).collect(Collectors.toList());
        }

        if (System.currentTimeMillis() - lastAttack < additionalDelay.getValue()) {
            return;
        }

        if (MatrixMod.mc.player.getAttackCooldownProgress(0) < 1) {
            return;
        }

        lastAttack = System.currentTimeMillis();
        for (LivingEntity target : targets) {
            MatrixMod.mc.interactionManager.attackEntity(MatrixMod.mc.player, target);
            MatrixMod.mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public enum AttackPriority {
        Near, Far, LowHealth, HighHealth, Yaw, YawInverse, Random;
    }
}
