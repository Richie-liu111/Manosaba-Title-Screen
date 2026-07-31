package me.shiiyuko.manosaba.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shiiyuko.manosaba.Manosaba;
import me.shiiyuko.manosaba.constant.TextureConst;
import me.shiiyuko.manosaba.gui.VirtualScreen;
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * 启动 Logo 画面：游戏加载完成后、首次进入主界面之前播放（用户方案：
 * 先播发行商/开发商 logo，再进主界面入场动画）。
 * <p>
 * 布局还原自原游戏 Boot.unity 场景：两个 logo 同时并排显示——
 * BrandLogo（Acacia，开发商）787×309 锚点中心 (−552, +24)、
 * CompanyLogo（REAER，发行商）1000×223 锚点中心 (+536, −32)，2560×1440 设计空间。
 * 时间线：淡入 500ms → 停留 2500ms → 淡出 500ms；任意键 / 点击跳过
 * （原游戏 Background 带 Button 可点击跳过）。
 * <p>
 * 继承 {@link Screen} 而非 {@link net.minecraft.client.gui.screens.TitleScreen}：
 * 菜单音乐由 MusicTicker 驱动且只在标题类屏幕运行，logo 阶段保持安静，
 * 与原游戏一致（BGM 在 System_Title 才响起）。
 */
public class BootLogoScreen extends Screen {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(2560, 1440);

    private static final int BRAND_W = 787;
    private static final int BRAND_H = 309;
    private static final int COMP_W = 1000;
    private static final int COMP_H = 223;
    // 原游戏 Boot.unity：锚点中心 (Y-up) BrandLogo(−552,+24) CompanyLogo(+536,−32)
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
    private boolean finished = false;

    public BootLogoScreen() {
        super(Component.literal(""));
        // 会话内只播一次：构造即置位，防止自然结束/跳过/异常路径重复播放
        Manosaba.bootSequencePlayed = true;
    }

    @Override
    public void tick() {
        if (!finished && System.currentTimeMillis() - startTime >= TOTAL_MS) {
            skip();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        skip();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        skip();
        return true;
    }

    /** 跳过 logo，直接进入主界面入场动画。 */
    private void skip() {
        if (!finished) {
            finished = true;
            this.minecraft.setScreen(new ManosabaTitleScreen());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int screenWidth = this.width;
        int screenHeight = this.height;

        // 16:9 letterbox（同主屏）
        int currentWidth;
        int currentHeight;
        if (screenWidth * 9 > screenHeight * 16) {
            currentWidth = screenHeight * 16 / 9;
            currentHeight = screenHeight;
        } else {
            currentWidth = screenWidth;
            currentHeight = screenWidth * 9 / 16;
        }
        int currentX = (screenWidth - currentWidth) / 2;
        int currentY = (screenHeight - currentHeight) / 2;
        VIRTUAL_SCREEN.setPracticalWidth(currentWidth);
        VIRTUAL_SCREEN.setPracticalHeight(currentHeight);
        VIRTUAL_SCREEN.setCurrentX(currentX);
        VIRTUAL_SCREEN.setCurrentY(currentY);

        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        long t = System.currentTimeMillis() - startTime;
        float alpha;
        if (t < FADE_IN_MS) {
            alpha = (float) t / FADE_IN_MS;
        } else if (t < FADE_IN_MS + HOLD_MS) {
            alpha = 1f;
        } else {
            alpha = Math.max(0f, 1f - (float) (t - FADE_IN_MS - HOLD_MS) / FADE_OUT_MS);
        }
        if (alpha <= 0f) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0f, 1f));

        RenderSystem.setShaderTexture(0, TextureConst.BRAND_LOGO);
        RenderUtils.blit(
                VIRTUAL_SCREEN.toPracticalX(BRAND_X),
                VIRTUAL_SCREEN.toPracticalY(BRAND_Y),
                VIRTUAL_SCREEN.toPracticalWidth(BRAND_W),
                VIRTUAL_SCREEN.toPracticalHeight(BRAND_H),
                guiGraphics.pose());

        RenderSystem.setShaderTexture(0, TextureConst.COMPANY_LOGO);
        RenderUtils.blit(
                VIRTUAL_SCREEN.toPracticalX(COMP_X),
                VIRTUAL_SCREEN.toPracticalY(COMP_Y),
                VIRTUAL_SCREEN.toPracticalWidth(COMP_W),
                VIRTUAL_SCREEN.toPracticalHeight(COMP_H),
                guiGraphics.pose());

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
