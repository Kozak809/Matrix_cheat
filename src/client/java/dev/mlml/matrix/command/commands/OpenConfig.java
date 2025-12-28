package dev.mlml.matrix.command.commands;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.command.Command;
import dev.mlml.matrix.gui.ChatHelper;
import dev.mlml.matrix.gui.ConfigScreen;
import dev.mlml.matrix.module.Module;
import dev.mlml.matrix.module.ModuleManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OpenConfig extends Command {
    public OpenConfig() {
        super("config", "Manage config for a module", "cfg");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatHelper.message(String.format("Usage: %sconfig <module>", ModuleManager.getCommandPrefix()));
            return;
        }

        Module module = ModuleManager.getModuleByStringIgnoreCase(args[0]);

        if (module == null) {
            ChatHelper.message("Module not found.");
            return;
        }

        CompletableFuture.delayedExecutor(5, TimeUnit.MILLISECONDS)
                         .execute(() -> MatrixMod.mc.setScreen(new ConfigScreen(module)));
    }
}
