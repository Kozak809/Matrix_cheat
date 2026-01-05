package dev.mlml.matrix.config;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.Module;
import dev.mlml.matrix.module.ModuleManager;
import dev.mlml.matrix.module.modules.ClickGui;
import dev.mlml.matrix.gui.ClickGuiScreen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigWriter {
    public static final String SchemaVersion = "1";
    public static final String configFile = "matrix.txt";
    public static final String GROUP_SEPARATOR = "\u001D";
    public static final String RECORD_SEPARATOR = "\u001E";
    public static final String UNIT_SEPARATOR = "\u001F";

    public static String generateConfig() {
        StringBuilder config = new StringBuilder(String.format("Matrix %s\n", SchemaVersion));

        for (Module module : ModuleManager.getModules()) {
            config.append("m")
                    .append(RECORD_SEPARATOR)
                    .append(module.getName())
                    .append(RECORD_SEPARATOR)
                    .append(module.isEnabled())
                    .append(RECORD_SEPARATOR)
                    .append(module.getBind())
                    .append(GROUP_SEPARATOR);
            config.append(module.getConfig().serialize()).append(GROUP_SEPARATOR);
        }

        ClickGui clickGui = (ClickGui) ModuleManager.getModule(ClickGui.class);
        if (clickGui != null && clickGui.getScreen() != null) {
            for (ClickGuiScreen.CategoryPanel panel : clickGui.getScreen().getPanels()) {
                config.append("p")
                        .append(RECORD_SEPARATOR)
                        .append(panel.type.name())
                        .append(RECORD_SEPARATOR)
                        .append(panel.x)
                        .append(RECORD_SEPARATOR)
                        .append(panel.y)
                        .append(RECORD_SEPARATOR)
                        .append(panel.expanded)
                        .append(GROUP_SEPARATOR);
            }
        }

        return config.toString();
    }

    public static void deserializeConfig(String config) {
        String[] lines = config.split("\n");
        String[] segments = lines[1].split(GROUP_SEPARATOR);

        Module currentModule = null;
        List<String> moduleLines = new ArrayList<>();

        for (String segment : segments) {
            String[] parts = segment.split(RECORD_SEPARATOR);

            if (parts.length < 2) {
                continue;
            }

            if (parts[0].equals("m")) {
                if (currentModule != null) {
                    currentModule.getConfig().deserialize(moduleLines);
                }

                Module module = ModuleManager.getModuleByString(parts[1]);

                MatrixMod.LOGGER.info("Deserializing module: {}", parts[1]);

                if (module == null) {
                    continue;
                }

                module.setEnabled(Boolean.parseBoolean(parts[2]));
                if (parts.length >= 4) {
                    try {
                        module.setBind(Integer.parseInt(parts[3]));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }

                currentModule = module;
                moduleLines.clear();
            } else if (parts[0].equals("p")) {
                ClickGui clickGui = (ClickGui) ModuleManager.getModule(ClickGui.class);
                if (clickGui != null) {
                    if (clickGui.getScreen() == null) {
                        clickGui.setScreen(new ClickGuiScreen());
                    }
                    if (clickGui.getScreen() != null) {
                        for (ClickGuiScreen.CategoryPanel panel : clickGui.getScreen().getPanels()) {
                            if (panel.type.name().equals(parts[1])) {
                                panel.x = Integer.parseInt(parts[2]);
                                panel.y = Integer.parseInt(parts[3]);
                                panel.expanded = Boolean.parseBoolean(parts[4]);
                            }
                        }
                    }
                }
            } else {
                moduleLines.add(segment);
            }
        }

        if (!moduleLines.isEmpty() && currentModule != null) {
            currentModule.getConfig().deserialize(moduleLines);
        }
    }

    public static void readConfigFromFile() {
        readConfigFromFile(configFile);
    }

    public static void readConfigFromFile(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            MatrixMod.LOGGER.warn("Config file does not exist");
            return;
        }

        try {
            java.io.FileReader fr = new java.io.FileReader(file);
            StringBuilder config = new StringBuilder();
            int c;
            while ((c = fr.read()) != -1) {
                config.append((char) c);
            }
            fr.close();
            deserializeConfig(config.toString());
        } catch (Exception e) {
            MatrixMod.LOGGER.error("Failed to read config from file:", e);
        }
    }

    public static void writeConfigToFile() {
        writeConfigToFile(configFile);
    }

    public static void writeConfigToFile(String filename) {
        String config = generateConfig();

        File file = new File(filename);

        try {
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(config);
            fw.close();
        } catch (Exception e) {
            MatrixMod.LOGGER.error("Failed to write config to file: {}", e.getMessage());
        }
    }
}
