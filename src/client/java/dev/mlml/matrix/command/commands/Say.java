package dev.mlml.matrix.command.commands;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.command.Command;

public class Say extends Command {
    public Say() {
        super("say", "Say something in chat", "s");
    }

    @Override
    public void execute(String[] args) {
        StringBuilder message = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        if (MatrixMod.mc.getNetworkHandler() == null) {
            return;
        }

        MatrixMod.mc.getNetworkHandler().sendChatMessage(message.toString());
    }
}
