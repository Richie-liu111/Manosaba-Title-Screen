package me.shiiyuko.manosaba.config;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * Manosaba 模组配置，使用 GTNHLib 注解驱动配置系统。
 */
@Config(modid = "manosaba")
public class ManosabaConfig {

    @Config.Comment("使用艾玛背景（关闭则为希罗）")
    @Config.DefaultBoolean(false)
    public static boolean useEmaBackground;

    @Config.Comment("背景音乐")
    @Config.DefaultBoolean(true)
    public static boolean bgm;

    @Config.Comment("直接退出游戏（否则返回原版主菜单）")
    @Config.DefaultBoolean(false)
    public static boolean justExit;
}
