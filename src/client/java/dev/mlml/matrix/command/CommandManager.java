package dev.mlml.matrix.command;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.command.commands.OpenConfig;
import dev.mlml.matrix.command.commands.ReadConfig;
import dev.mlml.matrix.command.commands.SaveConfig;
import dev.mlml.matrix.command.commands.Say;
import dev.mlml.matrix.event.Listener;
import dev.mlml.matrix.event.events.ChatSendEvent;
import dev.mlml.matrix.gui.ChatHelper;
import dev.mlml.matrix.gui.TextFormatter;
import dev.mlml.matrix.module.ModuleManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager {
    @Getter
    private static final List<Command> commands = new ArrayList<>();

    public static void init() {
        commands.add(new SaveConfig());
        commands.add(new ReadConfig());
        commands.add(new OpenConfig());
        commands.add(new Say());

        MatrixMod.eventManager.register(CommandManager.class);
        MatrixMod.LOGGER.info("Initialized " + commands.size() + " commands");
    }

    public static Command getCommand(Class<? extends Command> commandClass) {
        for (Command command : commands) {
            if (command.getClass().equals(commandClass)) {
                return command;
            }
        }

        return null;
    }

    public static Command getCommandByAlias(String alias) {
        for (Command command : commands) {
            for (String commandAlias : command.getAliases()) {
                if (commandAlias.equalsIgnoreCase(alias)) {
                    return command;
                }
            }
        }

        return null;
    }

    public static void execute(String commandString) {
        if (commandString.isEmpty() || commandString.isBlank()) {
            return;
        }

        String[] args = commandString.split(" +");

        String commandName = args[0];
        String[] commandArgs = new String[args.length - 1];

        System.arraycopy(args, 1, commandArgs, 0, commandArgs.length);

        Command command = getCommandByAlias(commandName);

        if (Objects.isNull(command)) {
            ChatHelper.message(TextFormatter.format("%1Error:%4 Command \"%3%s%4\" not found. Use %2say %3%s%4 to send it in chat.", TextFormatter.Code.RED, TextFormatter.Code.GREEN, TextFormatter.Code.YELLOW, TextFormatter.Code.RESET, commandName, ModuleManager.getCommandPrefix(), commandName));
            return;
        }

        command.execute(commandArgs);
    }

    @Listener
    public static void onMessage(ChatSendEvent event) {
        String message = event.getMessage();

        MatrixMod.LOGGER.debug("Received chat message: " + message);

        if (message.startsWith(ModuleManager.getCommandPrefix())) {
            event.cancel();
            execute(message.substring(ModuleManager.getCommandPrefix().length()));
        }
    }
}
