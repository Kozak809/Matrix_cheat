package dev.mlml.matrix.module.modules;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.event.Listener;
import dev.mlml.matrix.event.events.FileDropEvent;
import dev.mlml.matrix.gui.ImageToMapartScreen;
import dev.mlml.matrix.module.Module;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class ImageToMapart extends Module {

    public ImageToMapart() {
        super("ImageToMapart", "Convert images to map art schematics", GLFW.GLFW_KEY_UNKNOWN);
        MatrixMod.eventManager.register(this);
    }

    @Override
    public void onEnable() {
        if (MatrixMod.mc.player == null) {
            this.setEnabled(false);
            return;
        }
        MatrixMod.mc.setScreen(new ImageToMapartScreen());
        this.setEnabled(false); // Disable immediately after opening screen, or keep enabled? 
        // Usually GUI modules toggle themselves off. 
        // But if we want to support drag and drop WHILE the module is "active" (and maybe screen is closed but we want to open it on drop? No, drop only works when window is focused).
        // Let's just open the screen.
    }

    @Listener
    public void onFileDrop(FileDropEvent event) {
        MatrixMod.LOGGER.info("ImageToMapart received FileDropEvent with " + event.getPaths().length + " paths.");
        if (MatrixMod.mc.currentScreen instanceof ImageToMapartScreen) {
            MatrixMod.LOGGER.info("Passing files to ImageToMapartScreen.");
            ((ImageToMapartScreen) MatrixMod.mc.currentScreen).onFilesDropped(event.getPaths());
        } else {
             MatrixMod.LOGGER.info("ImageToMapartScreen is not open. Current screen: " + MatrixMod.mc.currentScreen);
        }
    }
}
