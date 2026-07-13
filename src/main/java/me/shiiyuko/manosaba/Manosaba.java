package me.shiiyuko.manosaba;

import com.mojang.logging.LogUtils;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Manosaba.MODID)
public class Manosaba {

    public static final String MODID = "manosaba";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Manosaba(IEventBus modBus) {
        ManosabaSounds.SOUND_EVENTS.register(modBus);
        LOGGER.info("Manosaba title screen loaded (NeoForge 1.21.1).");
    }
}