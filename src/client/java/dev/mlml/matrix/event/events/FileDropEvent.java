package dev.mlml.matrix.event.events;

import dev.mlml.matrix.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDropEvent extends Event {
    private final String[] paths;
}
