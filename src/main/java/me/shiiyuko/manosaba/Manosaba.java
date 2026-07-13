package me.shiiyuko.manosaba;

import com.mojang.logging.LogUtils;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(Manosaba.MODID)
public class Manosaba {

    public static final String MODID = "manosaba";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Manosaba() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ManosabaSounds.SOUND_EVENTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            LOGGER.info("Manosaba title screen loaded (Forge 1.20.1).");
        }
    }
}