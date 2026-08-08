package me.shiiyuko.manosaba;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * 常量与静态标志（Fabric 纯客户端 mod，无 @Mod 入口）。
 */
public final class Manosaba {

    public static final String MODID = "manosaba";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 标题 BGM 是否正在播放（ManosabaTitleScreen 自行管理）。
     *  为 true 时 MusicManager 被抑制（含子界面期间，防止其 5s 后重启音乐造成叠加）。
     *  进入世界时由 {@link ManosabaClient} 的 JOIN 事件清位。 */
    public static boolean titleMusicPlaying = false;

    private Manosaba() {
    }
}
