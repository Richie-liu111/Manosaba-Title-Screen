package me.shiiyuko.manosaba.constant;

import me.shiiyuko.manosaba.config.ManosabaConfig;
import net.minecraft.util.ResourceLocation;

/**
 * 所有拆分后独立 PNG 的纹理路径常量。
 * 1.7.10 使用 net.minecraft.util.ResourceLocation。
 */
public final class TextureConst {

    private TextureConst() {
    }

    private static ResourceLocation ui(String name) {
        return new ResourceLocation("manosaba", "textures/gui/" + name + ".png");
    }

    // 背景（根据 config/manosaba.cfg 中 backgroundCharacter 切换 HIRO/EMA）
    public static ResourceLocation background() {
        return ManosabaConfig.useEmaBackground
                ? ui("background_ema") : ui("background_hiro");
    }

    public static final ResourceLocation TITLE_OVERLAY = ui("titleoverlay");
    public static final ResourceLocation TITLE_LOGO_JA = ui("titlelogo_ja");
    public static final ResourceLocation TITLE_LOGO_ZH = ui("titlelogo_zhhans");

    public static final ResourceLocation BUTTON_LOAD_GAME_NORMAL = ui("button_loadgame_normal");
    public static final ResourceLocation BUTTON_LOAD_GAME_HIGHLIGHTED = ui("button_loadgame_highlighted");
    public static final ResourceLocation BUTTON_NEW_GAME_NORMAL = ui("button_newgame_normal");
    public static final ResourceLocation BUTTON_NEW_GAME_HIGHLIGHTED = ui("button_newgame_highlighted");
    public static final ResourceLocation BUTTON_GALLERY_NORMAL = ui("button_gallery_normal");
    public static final ResourceLocation BUTTON_GALLERY_HIGHLIGHTED = ui("button_gallery_highlighted");
    public static final ResourceLocation BUTTON_OPTIONS_NORMAL = ui("button_options_normal");
    public static final ResourceLocation BUTTON_OPTIONS_HIGHLIGHTED = ui("button_options_highlighted");
    public static final ResourceLocation BUTTON_EXIT_NORMAL = ui("button_exit_normal");
    public static final ResourceLocation BUTTON_EXIT_HIGHLIGHTED = ui("button_exit_highlighted");

    // 按钮悬停中文标签
    public static final ResourceLocation LABEL_LOAD_GAME = ui("label_loadgame_zhhans");
    public static final ResourceLocation LABEL_NEW_GAME = ui("label_newgame_zhhans");
    public static final ResourceLocation LABEL_GALLERY = ui("label_gallery_zhhans");
    public static final ResourceLocation LABEL_OPTIONS = ui("label_options_zhhans");
    public static final ResourceLocation LABEL_EXIT = ui("label_exit_zhhans");

    public static final ResourceLocation BRAND_LOGO = ui("brandlogo_acacia");
    public static final ResourceLocation COMPANY_LOGO = ui("companylogo_reaer");
}
