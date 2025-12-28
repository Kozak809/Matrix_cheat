package dev.mlml.matrix.event.events;

import dev.mlml.matrix.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;


@Setter
@Getter
@AllArgsConstructor
public abstract class PacketEvent extends Event {
    Packet<?> packet;

    public static class Received extends PacketEvent {
        public Received(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Sent extends PacketEvent {
        public Sent(Packet<?> packet) {
            super(packet);
        }
    }
}
