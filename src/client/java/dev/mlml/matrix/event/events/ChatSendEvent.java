package dev.mlml.matrix.event.events;

import dev.mlml.matrix.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatSendEvent extends Event {
    String message;
}
