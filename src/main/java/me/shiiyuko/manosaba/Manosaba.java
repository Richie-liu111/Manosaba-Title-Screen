package me.shiiyuko.manosaba;

import com.mojang.logging.LogUtils;
import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.config.ManosabaConfigScreen;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(Manosaba.MODID)
public class Manosaba {

    public static final String MODID = "manosaba";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 启动 logo 序列是否已播放（会话内仅一次：首次进主界面时播放）。 */
    public static boolean bootSequencePlayed = false;

    /** 标题 BGM 是否正在播放（ManosabaTitleScreen 自行管理）。
     *  为 true 时 MusicManager 被抑制（含子界面期间，防止其 5s 后重启音乐造成叠加）。 */
    public static boolean titleMusicPlaying = false;

    /** 进入世界：停止标题 BGM，MusicManager 恢复游戏音乐。 */
    @SubscribeEvent
    public void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        titleMusicPlaying = false;
        Minecraft.getInstance().getSoundManager().stop();
    }

    public Manosaba() {
        ManosabaConfig.register();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ManosabaSounds.SOUND_EVENTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.registerConfigScreen(ManosabaConfigScreen::new);
            LOGGER.info("Manosaba title screen loaded (Forge 1.20.1).");
        }
    }
}