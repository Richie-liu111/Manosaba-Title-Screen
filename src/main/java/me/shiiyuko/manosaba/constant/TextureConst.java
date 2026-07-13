package me.shiiyuko.manosaba.constant;

import me.shiiyuko.manosaba.Manosaba;
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

    public static final ResourceLocation BACKGROUND = ui("background");
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
    public static final ResourceLocation BUTTON_WITCH_BOOK_NORMAL = ui("button_witchbook_normal");
    public static final ResourceLocation BUTTON_WITCH_BOOK_HIGHLIGHTED = ui("button_witchbook_highlighted");

    public static final ResourceLocation BRAND_LOGO = ui("brandlogo_acacia");
    public static final ResourceLocation COMPANY_LOGO = ui("companylogo_reaer");
}