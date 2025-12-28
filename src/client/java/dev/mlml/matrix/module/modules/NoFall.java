package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.config.BooleanSetting;
import dev.mlml.matrix.config.DoubleSetting;
import dev.mlml.matrix.config.ListSetting;
import dev.mlml.matrix.event.Listener;
import dev.mlml.matrix.event.events.PacketEvent;
import dev.mlml.matrix.gui.TextFormatter;
import dev.mlml.matrix.mixin.IPlayerMoveC2SPacketMixin;
import dev.mlml.matrix.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

import javax.swing.text.Utilities;

public class NoFall extends Module {
    // It appears that as of 1.20.4, the client is now longer aware of fallDistance.
    private final ListSetting<Mode> mode = config.add(new ListSetting<>("Mode", "Method", Mode.OnGround));
//    private final DoubleSetting fallDistance = config.add(new DoubleSetting("Fall Distance", "The distance you can fall before triggering", 2.0, 1.0, 10.0, 1));
//    private final BooleanSetting fastFall = config.add(new BooleanSetting("Fast Fall", "Intentionally take fall damage at 3 blocks (Packet only)", false));
//    private boolean hasSentOnGround = false;
//    private int ticksSinceSentOnGround = 0;

    public NoFall() {
        super("NoFall", "Prevents fall damage", GLFW.GLFW_KEY_N);

        MatrixMod.eventManager.register(this);
    }

    @Override
    public String getStatus() {
//        if (MatrixMod.mc.player == null || MatrixMod.mc.getNetworkHandler() == null || MatrixMod.mc.player.fallDistance <= 0.1f) {
//            hasSentOnGround = false;
            return mode.getValue().name();
//        }

//        String distanceString = String.format("%.2f", MatrixMod.mc.player.fallDistance);
//        TextFormatter.Code color = MatrixMod.mc.player.fallDistance - fallDistance.getValue() < 0 ? TextFormatter.Code.GREEN : TextFormatter.Code.RED;
//        if (hasSentOnGround && color == TextFormatter.Code.RED) {
//            color = TextFormatter.Code.YELLOW;
//        }
//
//        return mode.getValue().name() + " " + TextFormatter.format("%1%s", color, distanceString);
    }

    @Override
    public void onTick() {
        if (MatrixMod.mc.player == null || MatrixMod.mc.getNetworkHandler() == null) {
            return;
        }

//        if (hasSentOnGround) {
//            ticksSinceSentOnGround++;
//        } else {
//            ticksSinceSentOnGround = 0;
//        }

//        if (fastFall.getValue() && mode.getValue() == Mode.Packet) {
//            if (MatrixMod.mc.player.fallDistance > 3.0f) {
//                MatrixMod.mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, MatrixMod.mc.player.horizontalCollision));
//                hasSentOnGround = true;
//            }
//        } else {
//            if (MatrixMod.mc.player.fallDistance > fallDistance.getValue()) {
//                switch (mode.getValue()) {
//                    case Packet -> {
//                        MatrixMod.mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, MatrixMod.mc.player.horizontalCollision));
//                        hasSentOnGround = true;
//                    }
//                    case Velocity -> {
//                        MatrixMod.mc.player.setVelocity(0, 0.1, 0);
//                        MatrixMod.mc.player.fallDistance = 0;
//                    }
//                }
//            }
//        }
    }

    @Listener
    public void onPacketSend(PacketEvent.Sent packetEvent) {
        if (!isEnabled() || MatrixMod.mc.player == null || MatrixMod.mc.getNetworkHandler() == null) {
            return;
        }

        if (packetEvent.getPacket() instanceof PlayerMoveC2SPacket && mode.getValue() == Mode.OnGround) {
            ((IPlayerMoveC2SPacketMixin) packetEvent.getPacket()).setOnGround(true);
//            hasSentOnGround = true;
        }
    }

    public enum Mode {
//        Packet,
        OnGround,
//        Velocity
    }
}

