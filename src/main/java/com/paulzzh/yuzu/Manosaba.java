package com.paulzzh.yuzu;

import com.paulzzh.yuzu.config.ManosabaConfig;
import com.paulzzh.yuzu.init.ManosabaSounds;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Manosaba.MODID,
        name = Manosaba.NAME,
        version = Manosaba.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        guiFactory = "com.paulzzh.yuzu.config.ManosabaConfigGuiFactory"
)
public class Manosaba {
    public static final String MODID = "manosaba";
    public static final String NAME = "Manosaba";
    public static final String VERSION = "1.0.4";

    public static Logger LOGGER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        ManosabaConfig.load(event.getSuggestedConfigurationFile());
        LOGGER.info("Manosaba loading...");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ManosabaSounds.registerSounds();
        LOGGER.info("Manosaba initialized with {} sounds!", 3);
    }
}
