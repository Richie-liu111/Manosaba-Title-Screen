package me.shiiyuko.manosaba.gui.splash;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shiiyuko.manosaba.constant.TextureConst;
import me.shiiyuko.manosaba.gui.VirtualScreen;
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

/**
 * Manosaba 启动画面渲染器：黑色背景 + 两个 logo 并排显示。
 * <ul>
 *   <li>设计空间 2560×1440（原游戏 Boot.unity 场景布局），通过
 *       {@link VirtualScreen} letterbox；</li>
 *   <li>BrandLogo_Acacia（787×309）锚点中心 (−552, +24)、
 *       CompanyLogo_ReAER（1000×223）锚点中心 (+536, −32)；
 *       与原游戏 Boot 场景两 logo 同时并排的布局一致；</li>
 *   <li>整体 alpha 由 {@code LoadingOverlayMixin} 根据淡入淡出时序传入。</li>
 * </ul>
 * 单例，资源在首次渲染时加载，{@link #cleanup()} 后重置。
 */
public final class SplashOverlayRenderer {

    private static final int DESIGN_W = 2560;
    private static final int DESIGN_H = 1440;
    private static final int BRAND_W = 787;
    private static final int BRAND_H = 309;
    private static final int COMP_W = 1000;
    private static final int COMP_H = 223;
    // 原游戏 Boot.unity：锚点中心 (Y-up) BrandLogo(−552,+24) CompanyLogo(+536,−32)
    // → Y-down 左上角：X 不变，Y = 720 − 24/±32 − 高/2
    private static final float BRAND_X = (DESIGN_W - BRAND_W) / 2f - 552; // 334.5
    private static final float BRAND_Y = (DESIGN_H - BRAND_H) / 2f - 24;  // 541.5
    private static final float COMP_X = (DESIGN_W - COMP_W) / 2f + 536;   // 1316
    private static final float COMP_Y = (DESIGN_H - COMP_H) / 2f + 32;    // 640.5

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

    /**
     * 渲染启动画面。
     *
     * @param g         GuiGraphics
     * @param logoAlpha 整体 logo 透明度（0..1），由 mixin 根据淡入淡出计算
     */
    public void render(GuiGraphics g, float logoAlpha) {
        int sw = g.guiWidth();
        int sh = g.guiHeight();

        // 全屏黑色背景。
        g.fill(0, 0, sw, sh, 0xFF000000);
        if (logoAlpha <= 0f) {
            return;
        }

        // 16:9 letterbox 到设计空间。
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

        // 原游戏 Boot.unity 布局：两个 logo 并排，原生尺寸，原生坐标。
        RenderSystem.setShaderTexture(0, TextureConst.BRAND_LOGO);
        RenderUtils.blit(
                vs.toPracticalX(BRAND_X),
                vs.toPracticalY(BRAND_Y),
                vs.toPracticalWidth(BRAND_W),
                vs.toPracticalHeight(BRAND_H),
                g.pose()
        );

        RenderSystem.setShaderTexture(0, TextureConst.COMPANY_LOGO);
        RenderUtils.blit(
                vs.toPracticalX(COMP_X),
                vs.toPracticalY(COMP_Y),
                vs.toPracticalWidth(COMP_W),
                vs.toPracticalHeight(COMP_H),
                g.pose()
        );

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    /**
     * 重置状态。当前无缓存资源，保留方法供 mixin 调用。
     */
    public void cleanup() {
    }
}