package dev.mlml.matrix.gui;

import dev.mlml.matrix.MatrixMod;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ChatHelper {
    public static final String PREFIX = TextFormatter.format("[%2%3Matrix%1] ", TextFormatter.Code.RESET, TextFormatter.Code.BOLD, TextFormatter.Code.GRAY);

    public static void message(String... message) {
        MutableText prefix = Text.literal(PREFIX);

        for (String m : message) {
            MatrixMod.mc.inGameHud.getChatHud().addMessage(prefix.append(Text.literal(m)));
        }
    }
}
