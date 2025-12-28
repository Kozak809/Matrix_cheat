package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.BooleanSetting;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.event.Listener;
import dev.mlml.matrix.event.events.PacketEvent;
import dev.mlml.matrix.gui.TextFormatter;
import dev.mlml.matrix.misc.FakePlayerEntity;
import dev.mlml.matrix.module.Module;
import dev.mlml.matrix.module.ModuleManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Backtrack extends Module {
    final List<PlayerMoveC2SPacket> packetBacklog = new ArrayList<>();
    private final DoubleSetting maxItems = config.add(new DoubleSetting("Max States", "The maximum amount of states to store, trigger with Ctrl+Key", 50d, 1d, 200d, 0));
    private final DoubleSetting seekBackAmount = config.add(new DoubleSetting("Seek Back Amount", "The amount of states to seek back, trigger with Alt+Key", 10d, 1d, 100d, 0));
    private final BooleanSetting restoreVelocity = config.add(new BooleanSetting("Restore Velocity", "Attempt to restore velocity when possible", true));
    private final BooleanSetting onlySavePositionUpdates = config.add(new BooleanSetting("Only Save Position Updates", "Ignore \"Look Only\" packets", true));
    private final Set<Integer> sentPackets = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private boolean catchingUp = false;
    private boolean dontRestoreOnDisable = false;

    private FakePlayerEntity fakePlayer;

    public Backtrack() {
        super("Backtrack", "Backtrack your movement", GLFW.GLFW_KEY_B);

        MatrixMod.eventManager.register(this);
    }

    public static Vec3d lerpVelocity(PlayerMoveC2SPacket last, PlayerMoveC2SPacket now) {
        if (last == null || now == null || MatrixMod.mc.player == null) {
            return Vec3d.ZERO;
        }

        double x = last.getX(MatrixMod.mc.player.getX()) - now.getX(MatrixMod.mc.player.getX());
        double y = last.getY(MatrixMod.mc.player.getY()) - now.getY(MatrixMod.mc.player.getY());
        double z = last.getZ(MatrixMod.mc.player.getZ()) - now.getZ(MatrixMod.mc.player.getZ());
        return new Vec3d(x, y, z);
    }

    @Override
    public void onEnable() {
        String key = getKeybind().getBoundKeyLocalizedText().getString();

        TextFormatter.format("%1Backtrack:%4 Press %2Alt+%3%s%4 to seek back. Press %2Ctrl+%3%s%4 to begin playback. Press %3%s%4 to stop playback.", TextFormatter.Code.YELLOW, TextFormatter.Code.GREEN, TextFormatter.Code.BOLD, TextFormatter.Code.RESET, key, key, key);

        fakePlayer = new FakePlayerEntity();
    }

    private void seekBack() {
        if (packetBacklog.isEmpty() || seekBackAmount.getValue() > packetBacklog.size()) {
            return;
        }

        PlayerMoveC2SPacket secondLast = null, last = null;
        for (int i = 0; i < seekBackAmount.getValue(); i++) {
            secondLast = last;
            last = packetBacklog.remove(packetBacklog.size() - 1);
        }
        moveTo(secondLast, last);
    }

    private void moveTo(PlayerMoveC2SPacket last, PlayerMoveC2SPacket now) {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }

        MatrixMod.mc.player.updatePositionAndAngles(now.getX(MatrixMod.mc.player.getX()), now.getY(MatrixMod.mc.player.getY()), now.getZ(MatrixMod.mc.player.getZ()), now.getYaw(MatrixMod.mc.player.getYaw()), now.getPitch(MatrixMod.mc.player.getPitch()));

        if (!restoreVelocity.getValue()) {
            return;
        }

        Vec3d velocity = lerpVelocity(last, now);
        MatrixMod.mc.player.setVelocity(velocity);
    }

    private void moveTo(PlayerMoveC2SPacket packet) {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }

        MatrixMod.mc.player.updatePositionAndAngles(packet.getX(MatrixMod.mc.player.getX()), packet.getY(MatrixMod.mc.player.getY()), packet.getZ(MatrixMod.mc.player.getZ()), packet.getYaw(MatrixMod.mc.player.getYaw()), packet.getPitch(MatrixMod.mc.player.getPitch()));
    }

    @Override
    public void onDisable() {
        if (fakePlayer != null) {
            fakePlayer.resetPlayerPosition();
            fakePlayer.despawn();
        }

        if (dontRestoreOnDisable) {
            dontRestoreOnDisable = false;
            packetBacklog.clear();
            return;
        }

        catchingUp = false;

        if (packetBacklog.isEmpty()) {
            return;
        }

        PlayerMoveC2SPacket first = packetBacklog.get(0);
        packetBacklog.clear();

        if (MatrixMod.mc.player == null || first == null) {
            return;
        }

        moveTo(first);
    }

    @Override
    public void onTick() {
        if (MatrixMod.mc.player == null || MatrixMod.mc.world == null) {
            return;
        }

        if (getModifierKeyStates().get(GLFW.GLFW_KEY_LEFT_ALT)) {
            seekBack();
            return;
        }

        if (getModifierKeyStates().get(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            catchingUp = true;
            fakePlayer.updatePositionAndAngles(MatrixMod.mc.player.getX(), MatrixMod.mc.player.getY(), MatrixMod.mc.player.getZ(), MatrixMod.mc.player.getYaw(), MatrixMod.mc.player.getPitch());
        }

        if (catchingUp) {
            if (packetBacklog.isEmpty()) {
                catchingUp = false;
                dontRestoreOnDisable = true;
                setEnabled(false);
                return;
            }

            PlayerMoveC2SPacket packet = packetBacklog.remove(0);
            if (!packetBacklog.isEmpty()) {
                moveTo(packetBacklog.get(0), packet);
            }
            sendPacket(packet);
        }
    }

    private int generatePacketIdentifier(PlayerMoveC2SPacket packet) {
        return packet.hashCode();
    }

    private void sendPacket(PlayerMoveC2SPacket packet) {
        if (ModuleManager.doNotSendPackets()) {
            return;
        }

        ClientConnection connection = Objects.requireNonNull(MatrixMod.mc.getNetworkHandler()).getConnection();

        if (connection == null) {
            return;
        }

        int packetId = generatePacketIdentifier(packet);
        sentPackets.add(packetId);

        if (!catchingUp) {
            fakePlayer.updatePositionAndAngles(packet.getX(fakePlayer.getX()), packet.getY(fakePlayer.getY()), packet.getZ(fakePlayer.getZ()), packet.getYaw(fakePlayer.getYaw()), packet.getPitch(fakePlayer.getPitch()));
        }

        connection.send(packet);
    }

    @Override
    public String getStatus() {
        TextFormatter.Code color = catchingUp ? TextFormatter.Code.BLUE : TextFormatter.Code.YELLOW;
        String countStr = String.valueOf(packetBacklog.size());
        String message = "Idle";
        if (catchingUp) {
            message = "Replaying";
        } else {
            if (isEnabled()) {
                if (packetBacklog.size() < maxItems.getValue()) {
                    message = "Standby";
                } else {
                    if (packetBacklog.size() == maxItems.getValue()) {
                        message = "Trailing";
                    }
                }
            }
        }
        return TextFormatter.format("%1%s [%s]", color, message, countStr);
    }

    @Listener
    public void onPacket(PacketEvent.Sent event) {
        if (!isEnabled() || MatrixMod.mc.player == null || MatrixMod.mc.getNetworkHandler() == null || catchingUp) {
            return;
        }

        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (sentPackets.contains(generatePacketIdentifier(packet))) {
                sentPackets.remove(generatePacketIdentifier(packet));
                return;
            }

            event.setCancelled(true);

            if (onlySavePositionUpdates.getValue() && !packet.changesPosition()) {
                return;
            }

            packetBacklog.add(packet);

            while (packetBacklog.size() > maxItems.getValue()) {
                PlayerMoveC2SPacket removed = packetBacklog.remove(0);

                sendPacket(removed);
            }

        }
    }
}
