package dev.mlml.matrix.module;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.modules.*;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    @Getter
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        add(new ClickGui(), Module.ModuleType.RENDER);
        add(new HeadsUpDisplay(), Module.ModuleType.RENDER);
        add(new Flight(), Module.ModuleType.MOVEMENT);
        add(new Speed(), Module.ModuleType.MOVEMENT);
//        add(new AntiAim(), Module.ModuleType.COMBAT);
        add(new OnlineProtections(), Module.ModuleType.MISC);
        add(new NoFall(), Module.ModuleType.PLAYER);
        add(new EdgeJump(), Module.ModuleType.MOVEMENT);
        add(new Backtrack(), Module.ModuleType.COMBAT);
        add(new Freecam(), Module.ModuleType.RENDER);
        add(new FastMine(), Module.ModuleType.PLAYER);
        add(new PingSpoof(), Module.ModuleType.MISC);
        add(new LongJump(), Module.ModuleType.MOVEMENT);
        add(new Passives(), Module.ModuleType.PLAYER);
        add(new Meta(), Module.ModuleType.META);
        add(new WallHack(), Module.ModuleType.RENDER);
        add(new FullBright(), Module.ModuleType.RENDER);
        add(new KillAura(), Module.ModuleType.COMBAT);
        add(new AttackManip(), Module.ModuleType.COMBAT);
        add(new TPRange(), Module.ModuleType.COMBAT);
//        add(new Replanter(), Module.ModuleType.WORLD);
        add(new Logger(), Module.ModuleType.MISC);
        add(new AtoB(), Module.ModuleType.MOVEMENT);
        add(new DropFPS(), Module.ModuleType.MISC);

        for (Module m : modules) {
            KeyBindingHelper.registerKeyBinding(m.getKeybind());
            ClientTickEvents.END_CLIENT_TICK.register(m::update);
            HudRenderCallback.EVENT.register(m::renderDraw);
            WorldRenderEvents.LAST.register(m::worldDraw);
        }

        MatrixMod.LOGGER.info("Initialized " + modules.size() + " modules");
    }

    private static void add(Module module, Module.ModuleType category) {
        module.setCategory(category);
        modules.add(module);
    }

    public static boolean doNotSendPackets() {
        Meta meta = (Meta) getModule(Meta.class);
        return meta == null || !meta.getSendPackets().getValue();
    }

    public static String getCommandPrefix() {
        Meta meta = (Meta) getModule(Meta.class);
        return meta != null ? meta.getPrefix().getValue() : ";";
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(Class<T> moduleClass) {
        for (Module module : modules) {
            if (module.getClass().equals(moduleClass)) {
                return (T) module;
            }
        }
        return null;
    }

    public static Module getModuleByStringIgnoreCase(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public static Module getModuleByString(String name) {
        for (Module module : modules) {
            if (module.getName().equals(name)) {
                return module;
            }
        }
        return null;
    }
}
