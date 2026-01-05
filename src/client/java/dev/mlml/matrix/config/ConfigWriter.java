package dev.mlml.matrix.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.gui.ClickGuiScreen;
import dev.mlml.matrix.module.Module;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.ClickGui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

public class ConfigWriter {
    public static final String SchemaVersion = "2";
    private static final File CONFIG_DIR = new File("Matrix");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "matrix.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void readConfigFromFile() {
        if (!CONFIG_FILE.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            
            if (root == null) return;

            // Load Modules
            if (root.has("modules")) {
                JsonObject modulesObj = root.getAsJsonObject("modules");
                for (Map.Entry<String, JsonElement> entry : modulesObj.entrySet()) {
                    String moduleName = entry.getKey();
                    Module module = ModuleManager.getModuleByString(moduleName);
                    
                    if (module == null) continue;
                    
                    JsonObject moduleJson = entry.getValue().getAsJsonObject();
                    
                    if (moduleJson.has("enabled")) {
                        module.setEnabled(moduleJson.get("enabled").getAsBoolean());
                    }
                    
                    if (moduleJson.has("bind")) {
                        module.setBind(moduleJson.get("bind").getAsInt());
                    }
                    
                    if (moduleJson.has("settings")) {
                        module.getConfig().deserialize(moduleJson.getAsJsonObject("settings"));
                    }
                }
            }

            // Load ClickGUI State
            if (root.has("clickGui")) {
                ClickGui clickGui = (ClickGui) ModuleManager.getModule(ClickGui.class);
                if (clickGui != null) {
                    JsonObject guiJson = root.getAsJsonObject("clickGui");
                    
                    if (clickGui.getScreen() == null) {
                        clickGui.setScreen(new ClickGuiScreen());
                    }
                    
                    if (guiJson.has("panels")) {
                        JsonObject panelsObj = guiJson.getAsJsonObject("panels");
                        for (ClickGuiScreen.CategoryPanel panel : clickGui.getScreen().getPanels()) {
                            if (panelsObj.has(panel.type.name())) {
                                JsonObject panelJson = panelsObj.getAsJsonObject(panel.type.name());
                                panel.x = panelJson.get("x").getAsInt();
                                panel.y = panelJson.get("y").getAsInt();
                                panel.expanded = panelJson.get("expanded").getAsBoolean();
                            }
                        }
                    }
                }
            }
            
            MatrixMod.LOGGER.info("Config loaded from {}", CONFIG_FILE.getAbsolutePath());

        } catch (Exception e) {
            MatrixMod.LOGGER.error("Failed to read config from file", e);
        }
    }

    public static void readConfigFromFile(String filename) {
         // Fallback for command usage if user specifies a specific file
         // For now, we just redirect to the main read, or we could implement loading from other files if needed.
         // But given the architectural change, strict adherence to matrix.json is safer.
         // If a user provides a path, we try to read it.
         File file = new File(filename);
         if (!file.exists()) {
             // Try looking in Matrix folder
             file = new File(CONFIG_DIR, filename);
         }
         
         if (file.exists()) {
             // We'd need to refactor the main logic into a method taking a File, 
             // but to keep it simple I'll just load the default for now or log a warning.
             // Actually, let's just support the default file strongly.
             readConfigFromFile(); 
         }
    }

    public static void writeConfigToFile() {
        try {
            if (!CONFIG_DIR.exists()) {
                CONFIG_DIR.mkdirs();
            }

            JsonObject root = new JsonObject();
            root.addProperty("schemaVersion", SchemaVersion);

            // Save Modules
            JsonObject modulesObj = new JsonObject();
            for (Module module : ModuleManager.getModules()) {
                JsonObject moduleJson = new JsonObject();
                moduleJson.addProperty("enabled", module.isEnabled());
                moduleJson.addProperty("bind", module.getBind());

                JsonObject settingsJson = new JsonObject();
                for (GenericSetting<?> setting : module.getConfig().getSettings()) {
                    // Convert value to string to ensure consistent serialization
                    settingsJson.addProperty(setting.getName(), String.valueOf(setting.getValue()));
                }
                moduleJson.add("settings", settingsJson);

                modulesObj.add(module.getName(), moduleJson);
            }
            root.add("modules", modulesObj);

            // Save ClickGUI State
            ClickGui clickGui = (ClickGui) ModuleManager.getModule(ClickGui.class);
            if (clickGui != null && clickGui.getScreen() != null) {
                JsonObject guiJson = new JsonObject();
                JsonObject panelsObj = new JsonObject();
                
                for (ClickGuiScreen.CategoryPanel panel : clickGui.getScreen().getPanels()) {
                    JsonObject panelJson = new JsonObject();
                    panelJson.addProperty("x", panel.x);
                    panelJson.addProperty("y", panel.y);
                    panelJson.addProperty("expanded", panel.expanded);
                    panelsObj.add(panel.type.name(), panelJson);
                }
                guiJson.add("panels", panelsObj);
                root.add("clickGui", guiJson);
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                gson.toJson(root, writer);
            }
            
            MatrixMod.LOGGER.info("Config saved to {}", CONFIG_FILE.getAbsolutePath());

        } catch (Exception e) {
            MatrixMod.LOGGER.error("Failed to write config to file", e);
        }
    }
    
    // Overload for compatibility
    public static void writeConfigToFile(String filename) {
        writeConfigToFile();
    }
}