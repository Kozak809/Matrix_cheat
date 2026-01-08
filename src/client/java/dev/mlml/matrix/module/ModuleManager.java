package dev.mlml.matrix.module;

import dev.mlml.matrix.MatrixMod;
import dev.mlml.matrix.module.modules.*;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    @Getter
    private static final List<Module> modules = new ArrayList<>();
    private static final Map<Class<? extends Module>, Module> moduleMap = new HashMap<>();

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
        add(new NoFog(), Module.ModuleType.RENDER);
        add(new Zoom(), Module.ModuleType.RENDER);
        add(new KillAura(), Module.ModuleType.COMBAT);
        add(new AttackManip(), Module.ModuleType.COMBAT);
        add(new TPRange(), Module.ModuleType.COMBAT);
//        add(new Replanter(), Module.ModuleType.WORLD);
        add(new Logger(), Module.ModuleType.MISC);
        add(new AutoRespawn(), Module.ModuleType.MISC);
        add(new AtoB(), Module.ModuleType.MOVEMENT);
        add(new DropFPS(), Module.ModuleType.MISC);
        add(new ImageToMapart(), Module.ModuleType.MISC);
        add(new AntiKick(), Module.ModuleType.MISC);
        add(new ResignSpam(), Module.ModuleType.MISC);

        for (Module m : modules) {
            if (m instanceof ClickGui) {
                KeyBindingHelper.registerKeyBinding(m.getKeybind());
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Module m : modules) {
                m.update(client);
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            for (Module m : modules) {
                m.renderDraw(drawContext, tickDelta);
            }
        });

        WorldRenderEvents.LAST.register(context -> {
            for (Module m : modules) {
                m.worldDraw(context);
            }
        });

        MatrixMod.LOGGER.info("Initialized " + modules.size() + " modules");
    }

    private static void add(Module module, Module.ModuleType category) {
        module.setCategory(category);
        modules.add(module);
        moduleMap.put(module.getClass(), module);
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
        return (T) moduleMap.get(moduleClass);
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
