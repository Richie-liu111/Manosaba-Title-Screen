package me.shiiyuko.manosaba.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Manosaba 模组客户端配置。通过 config/manosaba-client.toml 控制背景角色等设置。
 */
public final class ManosabaConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public enum BackgroundCharacter {
        HIRO,
        EMA
    }

    static final ForgeConfigSpec.EnumValue<BackgroundCharacter> BACKGROUND_CHARACTER =
            BUILDER.comment("标题画面背景角色：HIRO = 希罗, EMA = 艾玛")
                    .defineEnum("backgroundCharacter", BackgroundCharacter.HIRO);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

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

    /** 在模组构造时调用一次，注册配置文件。 */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }
}