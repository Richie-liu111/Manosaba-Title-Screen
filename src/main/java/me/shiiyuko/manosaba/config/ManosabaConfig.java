package me.shiiyuko.manosaba.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Manosaba 模组客户端配置。通过 config/manosaba-client.toml 控制背景角色等设置。
 */
public final class ManosabaConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public enum BackgroundCharacter {
        HIRO,
        EMA
    }

    static final ModConfigSpec.EnumValue<BackgroundCharacter> BACKGROUND_CHARACTER =
            BUILDER.comment("标题画面背景角色：HIRO = 希罗, EMA = 艾玛")
                    .defineEnum("backgroundCharacter", BackgroundCharacter.HIRO);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static BackgroundCharacter backgroundCharacter() {
        return BACKGROUND_CHARACTER.get();
    }

    public static void toggleBackground() {
        BACKGROUND_CHARACTER.set(
                BACKGROUND_CHARACTER.get() == BackgroundCharacter.HIRO
                        ? BackgroundCharacter.EMA
                        : BackgroundCharacter.HIRO);
        SPEC.save();
    }
}