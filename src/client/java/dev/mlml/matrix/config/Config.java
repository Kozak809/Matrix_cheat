package dev.mlml.matrix.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class Config {
    protected final List<GenericSetting<?>> settings = new ArrayList<>();

    public void deserialize(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            GenericSetting<?> setting = get(entry.getKey());
            if (setting != null) {
                // We convert the JSON primitive to a string so the specific setting can parse it
                // (e.g. BooleanSetting parses "true", DoubleSetting parses "0.5", ListSetting parses "MODE")
                setting.deserialize(entry.getValue().getAsString());
            }
        }
    }

    public <S extends GenericSetting<?>> S add(S ta) {
        settings.add(ta);
        return ta;
    }

    @SuppressWarnings("unchecked")
    public <S extends GenericSetting<?>> S get(String name) {
        for (GenericSetting<?> setting : settings) {
            if (setting.getName().equals(name)) {
                return (S) setting;
            }
        }

        return null;
    }

    public List<ClickableWidget> getAsWidgets() {
        List<ClickableWidget> widgets = new ArrayList<>();

        for (GenericSetting<?> setting : settings) {
            widgets.add(setting.getAsWidget());
        }

        return widgets;
    }
}
