package dev.mlml.matrix.config;

import dev.mlml.matrix.gui.ConfigScreen;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

    @Getter
    @Setter
    public abstract class GenericSetting<V> {
    protected final String name;
    protected final String tooltip;
    final V defaultValue;
    final List<Consumer<V>> callbacks;
    protected String label;
    V value;
    @Getter @Setter
    protected java.util.function.Supplier<Boolean> isVisible = () -> true;
    protected final List<Runnable> observers = new java.util.concurrent.CopyOnWriteArrayList<>();

    public GenericSetting(String name, String tooltip, V defaultValue, List<Consumer<V>> callbacks) {
        this.name = name;
        this.tooltip = tooltip;
        this.label = name;

        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.callbacks = callbacks;
    }

    public void addObserver(Runnable observer) {
        observers.add(observer);
    }

    public void removeObserver(Runnable observer) {
        observers.remove(observer);
    }

    @SuppressWarnings("unchecked")
    public void deserialize(String value) {
        this.value = (V) value;
    }
    public Text asText() {
        return Text.literal(String.format("%s: %s", name, value));
    }

    public ClickableWidget getAsWidget() {
        return ButtonWidget.builder(Text.literal(name), button -> {
                               System.out.printf("%s clicked%n", name);
                           })
                           .dimensions(0, 0, ConfigScreen.DEFAULT_WIDTH, ConfigScreen.DEFAULT_HEIGHT)
                           .tooltip(Tooltip.of(Text.literal(tooltip)))
                           .build();
    }

    public void setValue(V value) {
        this.value = value;
        callbacks.forEach(c -> c.accept(value));
        observers.forEach(Runnable::run);
    }
}
