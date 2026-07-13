package me.shiiyuko.manosaba.gui.splash;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shiiyuko.manosaba.constant.TextureConst;
import me.shiiyuko.manosaba.gui.VirtualScreen;
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

/**
 * Manosaba 启动画面渲染器：黑色背景 + 两个 logo 水平居中。
 * 设计空间 1920×1080，BrandLogo_Acacia 和 CompanyLogo_ReAER 水平居中，
 * CompanyLogo 偏下 32 虚拟像素。整体 alpha 由 LoadingOverlayMixin 传入。
 * 单例，资源在首次渲染时加载。
 */
public final class SplashOverlayRenderer {

    private static final int DESIGN_W = 1920;
    private static final int DESIGN_H = 1080;
    private static final int BRAND_W = 787;
    private static final int BRAND_H = 309;
    private static final int COMP_W = 1000;
    private static final int COMP_H = 223;
    private static final float LOGO_SCALE = 0.5f;
    private static final int GAP = 64;
    private static final int COMP_Y_OFFSET = 32;

    private static volatile SplashOverlayRenderer instance;

    private SplashOverlayRenderer() {
    }

    public static SplashOverlayRenderer get() {
        SplashOverlayRenderer local = instance;
        if (local == null) {
            synchronized (SplashOverlayRenderer.class) {
                local = instance;
                if (local == null) {
                    local = new SplashOverlayRenderer();
                    instance = local;
                }
            }
        }
        return local;
    }

    public void render(GuiGraphics g, float logoAlpha) {
        int sw = g.guiWidth();
        int sh = g.guiHeight();

        g.fill(0, 0, sw, sh, 0xFF000000);
        if (logoAlpha <= 0f) {
            return;
        }

        VirtualScreen vs = new VirtualScreen(DESIGN_W, DESIGN_H);
        int currentWidth;
        int currentHeight;
        if (sw * 9 > sh * 16) {
            currentWidth = sh * 16 / 9;
            currentHeight = sh;
        } else {
            currentWidth = sw;
            currentHeight = sw * 9 / 16;
        }
        vs.setPracticalWidth(currentWidth);
        vs.setPracticalHeight(currentHeight);
        vs.setCurrentX((sw - currentWidth) / 2);
        vs.setCurrentY((sh - currentHeight) / 2);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Mth.clamp(logoAlpha, 0f, 1f));

        float brandDisplayW = BRAND_W * LOGO_SCALE;
        float brandDisplayH = BRAND_H * LOGO_SCALE;
        float compDisplayW = COMP_W * LOGO_SCALE;
        float compDisplayH = COMP_H * LOGO_SCALE;
        float totalW = brandDisplayW + compDisplayW + GAP;
        float cursorX = (DESIGN_W - totalW) / 2;
        float centerY = DESIGN_H / 2f;

        float brandY = centerY - brandDisplayH / 2;
        RenderSystem.setShaderTexture(0, TextureConst.BRAND_LOGO);
        RenderUtils.blit(
                vs.toPracticalX(cursorX),
                vs.toPracticalY(brandY),
                vs.toPracticalWidth(brandDisplayW),
                vs.toPracticalHeight(brandDisplayH),
                g.pose()
        );

        float compX = cursorX + brandDisplayW + GAP;
        float compY = centerY - compDisplayH / 2 + COMP_Y_OFFSET;
        RenderSystem.setShaderTexture(0, TextureConst.COMPANY_LOGO);
        RenderUtils.blit(
                vs.toPracticalX(compX),
                vs.toPracticalY(compY),
                vs.toPracticalWidth(compDisplayW),
                vs.toPracticalHeight(compDisplayH),
                g.pose()
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public void cleanup() {
    }
}