package me.shiiyuko.manosaba.constant;

import me.shiiyuko.manosaba.Manosaba;
import me.shiiyuko.manosaba.config.ManosabaConfig;
import net.minecraft.resources.ResourceLocation;

/**
 * 所有拆分后独立 PNG 的纹理路径常量。每个精灵对应一张独立 PNG，
 * Layer/TitleScreenButton 通过 RenderUtils 画整张图（UV 0~1）。
 * 文件名必须全小写（MC 资源路径规范：仅允许 [a-z0-9/._-]）。
 * 1.21.1 使用 ResourceLocation.fromNamespaceAndPath。
 */
public final class TextureConst {

    private TextureConst() {
    }

    private static ResourceLocation ui(String name) {
        return ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "textures/gui/" + name + ".png");
    }

    // 背景（通过 config/manosaba-client.toml 切换 HIRO/EMA，默认 HIRO）
    public static ResourceLocation background() {
        return ManosabaConfig.backgroundCharacter() == ManosabaConfig.BackgroundCharacter.EMA
                ? ui("background_ema") : ui("background_hiro");
    }

    public static final ResourceLocation TITLE_OVERLAY = ui("titleoverlay");
    public static final ResourceLocation TITLE_LOGO_JA = ui("titlelogo_ja");
    public static final ResourceLocation TITLE_LOGO_ZH = ui("titlelogo_zhhans");

    public static final ResourceLocation BUTTON_LOAD_GAME_NORMAL = ui("button_loadgame_normal");
    public static final ResourceLocation BUTTON_LOAD_GAME_HIGHLIGHTED = ui("button_loadgame_highlighted");
    public static final ResourceLocation BUTTON_LOAD_GAME_LOCKED = ui("button_loadgame_locked");
    public static final ResourceLocation BUTTON_NEW_GAME_NORMAL = ui("button_newgame_normal");
    public static final ResourceLocation BUTTON_NEW_GAME_HIGHLIGHTED = ui("button_newgame_highlighted");
    public static final ResourceLocation BUTTON_GALLERY_NORMAL = ui("button_gallery_normal");
    public static final ResourceLocation BUTTON_GALLERY_HIGHLIGHTED = ui("button_gallery_highlighted");
    public static final ResourceLocation BUTTON_OPTIONS_NORMAL = ui("button_options_normal");
    public static final ResourceLocation BUTTON_OPTIONS_HIGHLIGHTED = ui("button_options_highlighted");
    public static final ResourceLocation BUTTON_EXIT_NORMAL = ui("button_exit_normal");
    public static final ResourceLocation BUTTON_EXIT_HIGHLIGHTED = ui("button_exit_highlighted");
    public static final ResourceLocation BUTTON_WITCH_BOOK_NORMAL = ui("button_witchbook_normal");
    public static final ResourceLocation BUTTON_WITCH_BOOK_HIGHLIGHTED = ui("button_witchbook_highlighted");

    // 按钮悬停中文标签（原游戏 Label@ZhHans 精灵）
    public static final ResourceLocation LABEL_LOAD_GAME = ui("label_loadgame_zhhans");
    public static final ResourceLocation LABEL_NEW_GAME = ui("label_newgame_zhhans");
    public static final ResourceLocation LABEL_GALLERY = ui("label_gallery_zhhans");
    public static final ResourceLocation LABEL_OPTIONS = ui("label_options_zhhans");
    public static final ResourceLocation LABEL_EXIT = ui("label_exit_zhhans");

    public static final ResourceLocation BRAND_LOGO = ui("brandlogo_acacia");
    public static final ResourceLocation COMPANY_LOGO = ui("companylogo_reaer");

    // 退出对话框
    public static final ResourceLocation DIALOG_BASE = ui("dialogbase");
    public static final ResourceLocation TOP_FRAME = ui("topframe");
    public static final ResourceLocation BOTTOM_FRAME = ui("bottomframe");
    public static final ResourceLocation BUTTON_BASE_DEFAULT = ui("buttonbase_default");
    public static final ResourceLocation BUTTON_BASE_HIGHLIGHTED = ui("buttonbase_highlighted");
    public static final ResourceLocation DIALOG_MESSAGE_ZH = ui("dialog_message_zh");
    public static final ResourceLocation DIALOG_MESSAGE_JA = ui("dialog_message_ja");

    /** 纯黑 2×2 纹理（黑幕/压暗层） */
    public static final ResourceLocation BLACK = ui("black");

    /** 对话框按钮纹理 */
    public static ResourceLocation dialogButton(String name, boolean highlighted, boolean zh) {
        return ui("dialog_button_" + name + (zh ? "_zh" : "_ja")
                + (highlighted ? "_highlighted" : "_default"));
    }
}