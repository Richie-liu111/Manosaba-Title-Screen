package me.shiiyuko.manosaba;

import com.mojang.logging.LogUtils;
import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.config.ManosabaConfigScreen;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Manosaba.MODID)
public class Manosaba {

    public static final String MODID = "manosaba";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 启动 logo 序列是否已播放（会话内仅一次：首次进主界面时播放）。 */
    public static boolean bootSequencePlayed = false;

    /** 标题 BGM 是否正在播放（ManosabaTitleScreen 自行管理）。
     *  为 true 时 MusicManager 被抑制（含子界面期间，防止其重启音乐造成叠加）。 */
    public static boolean titleMusicPlaying = false;

    /** 进入世界：停止标题 BGM，MusicManager 恢复游戏音乐。 */
    private void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        titleMusicPlaying = false;
        Minecraft.getInstance().getSoundManager().stop();
    }

    public Manosaba(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ManosabaConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ManosabaConfigScreen(parent));

        ManosabaSounds.SOUND_EVENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        LOGGER.info("Manosaba title screen loaded (NeoForge 1.21.1).");
    }
}
