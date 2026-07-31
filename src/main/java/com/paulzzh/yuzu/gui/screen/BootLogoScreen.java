package com.paulzzh.yuzu.gui.screen;

import com.img.gui.VirtualScreen;
import com.paulzzh.yuzu.Manosaba;
import com.paulzzh.yuzu.constant.TextureConst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * 启动 Logo 画面：游戏加载完成后、首次进入主界面之前播放（先播发行商/开发商 logo，
 * 再进主界面入场动画）。
 * <p>
 * 布局还原自原游戏 Boot.unity 场景：两个 logo 同时并排显示——
 * BrandLogo（Acacia，开发商）787×309 锚点中心 (−552, +24)、
 * CompanyLogo（REAER，发行商）1000×223 锚点中心 (+536, −32)，2560×1440 设计空间。
 * 时间线：淡入 500ms → 停留 2500ms → 淡出 500ms，不可跳过。
 * <p>
 * 继承 {@link GuiScreen}：logo 阶段不触发菜单音乐（BGM 在标题序列才开始）。
 */
public class BootLogoScreen extends GuiScreen {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(2560, 1440);

    private static final int BRAND_W = 787;
    private static final int BRAND_H = 309;
    private static final int COMP_W = 1000;
    private static final int COMP_H = 223;

    // 原游戏 Boot.unity：锚点中心（Y-up）BrandLogo(−552,+24) CompanyLogo(+536,−32)
    // → Y-down 左上角：X 不变，Y = 720 − 24/±32 − 高/2
    private static final float BRAND_X = (2560 - BRAND_W) / 2f - 552; // 334.5
    private static final float BRAND_Y = (1440 - BRAND_H) / 2f - 24;  // 541.5
    private static final float COMP_X = (2560 - COMP_W) / 2f + 536;   // 1316
    private static final float COMP_Y = (1440 - COMP_H) / 2f + 32;    // 640.5

    private static final long FADE_IN_MS = 500L;
    private static final long HOLD_MS = 2500L;
    private static final long FADE_OUT_MS = 500L;
    private static final long TOTAL_MS = FADE_IN_MS + HOLD_MS + FADE_OUT_MS;

    private final long startTime = System.currentTimeMillis();
    public BootLogoScreen() {
        // 会话内只播一次：构造即置位
        Manosaba.bootSequencePlayed = true;
    }

    @Override
    public void updateScreen() {
        if (System.currentTimeMillis() - startTime >= TOTAL_MS) {
            this.mc.displayGuiScreen(new ManosabaTitleScreen());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        int sw = width, sh = height;
        int cw, ch;
        if (sw * 9 > sh * 16) { cw = sh * 16 / 9; ch = sh; }
        else { cw = sw; ch = sw * 9 / 16; }
        int cx = (sw - cw) / 2, cy = (sh - ch) / 2;
        VIRTUAL_SCREEN.setPracticalWidth(cw);
        VIRTUAL_SCREEN.setPracticalHeight(ch);
        VIRTUAL_SCREEN.setCurrentX(cx);
        VIRTUAL_SCREEN.setCurrentY(cy);

        drawRect(0, 0, sw, sh, 0xFF000000);

        long t = System.currentTimeMillis() - startTime;
        float alpha;
        if (t < FADE_IN_MS) alpha = (float) t / FADE_IN_MS;
        else if (t < FADE_IN_MS + HOLD_MS) alpha = 1f;
        else alpha = Math.max(0f, 1f - (float) (t - FADE_IN_MS - HOLD_MS) / FADE_OUT_MS);
        if (alpha <= 0f) return;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, Math.min(alpha, 1f));

        // BrandLogo（Acacia）
        this.mc.getTextureManager().bindTexture(TextureConst.BRAND_LOGO);
        drawTexQuad(
                VIRTUAL_SCREEN.toPracticalX(BRAND_X),
                VIRTUAL_SCREEN.toPracticalY(BRAND_Y),
                VIRTUAL_SCREEN.toPracticalWidth(BRAND_W),
                VIRTUAL_SCREEN.toPracticalHeight(BRAND_H));

        // CompanyLogo（REAER）
        this.mc.getTextureManager().bindTexture(TextureConst.COMPANY_LOGO);
        drawTexQuad(
                VIRTUAL_SCREEN.toPracticalX(COMP_X),
                VIRTUAL_SCREEN.toPracticalY(COMP_Y),
                VIRTUAL_SCREEN.toPracticalWidth(COMP_W),
                VIRTUAL_SCREEN.toPracticalHeight(COMP_H));

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableBlend();
    }

    private static void drawTexQuad(float x, float y, float w, float h) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x, y + h, 0).tex(0, 1).endVertex();
        buf.pos(x + w, y + h, 0).tex(1, 1).endVertex();
        buf.pos(x + w, y, 0).tex(1, 0).endVertex();
        buf.pos(x, y, 0).tex(0, 0).endVertex();
        tess.draw();
    }
}
