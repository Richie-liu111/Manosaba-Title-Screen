package me.shiiyuko.manosaba;

import com.mojang.logging.LogUtils;
import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.config.ManosabaConfigScreen;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(Manosaba.MODID)
public class Manosaba {

    public static final String MODID = "manosaba";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Manosaba(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ManosabaConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ManosabaConfigScreen(parent));

        ManosabaSounds.SOUND_EVENTS.register(modBus);
        LOGGER.info("Manosaba title screen loaded (NeoForge 1.21.1).");
    }
}