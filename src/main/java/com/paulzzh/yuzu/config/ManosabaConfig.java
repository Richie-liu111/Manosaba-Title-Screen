package com.paulzzh.yuzu.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Manosaba 模组客户端配置。通过 config/manosaba.cfg 控制背景角色等设置。
 * 参考 forge-1 的 ManosabaConfig + 1.12.2 Forge Configuration API。
 */
public final class ManosabaConfig {

    private static Configuration config;

    public enum BackgroundCharacter {
        HIRO,
        EMA
    }

    private static BackgroundCharacter background = BackgroundCharacter.HIRO;

    private ManosabaConfig() {
    }

    /** 在 mod preInit 时调用 */
    public static void load(File configFile) {
        config = new Configuration(configFile);

        String bg = config.getString("backgroundCharacter", Configuration.CATEGORY_GENERAL,
                "HIRO", "标题画面背景角色：HIRO = 希罗, EMA = 艾玛\n点击 Mod Options 按钮或在配置界面修改");

        try {
            background = BackgroundCharacter.valueOf(bg.toUpperCase());
        } catch (Exception e) {
            background = BackgroundCharacter.HIRO;
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static BackgroundCharacter getBackground() {
        return background;
    }

    public static void toggleBackground() {
        background = background == BackgroundCharacter.HIRO ? BackgroundCharacter.EMA : BackgroundCharacter.HIRO;
        config.get(Configuration.CATEGORY_GENERAL, "backgroundCharacter", "HIRO").set(background.name());
        config.save();
    }
}
