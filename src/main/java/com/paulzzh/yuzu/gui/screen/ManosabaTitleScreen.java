package com.paulzzh.yuzu.gui.screen;

import com.img.gui.Layer;
import com.img.gui.TitleScreenButton;
import com.img.gui.VirtualScreen;
import com.paulzzh.yuzu.constant.TextureConst;
import com.paulzzh.yuzu.init.ManosabaSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manosaba 标题画面。布局还原自原游戏（魔法少女ノ魔女裁判）TitleUI.prefab：
 * 设计空间 2560×1440，背景 ContentScale.Crop（2:1→16:9），5 按钮原位置。
 * 1.12.2 移植版。
 *
 * 动画周期：RESIZE 不重置动画，ESC/游戏退出回来重置。
 * BGM：由本界面管理，离开时停止，回来时重置 → 不会重疊播放。
 */
public class ManosabaTitleScreen extends GuiMainMenu {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(2560, 1440);

    private static final int LOGO_W = 1039;
    private static final int LOGO_H = 622;

    private static final long BG_DURATION = 2500L;
    private static final long UI_DELAY = BG_DURATION + 250L;
    private static final long UI_DURATION = 500L;
    /** BGM 延迟：对应原游戏开场白 − 约 1.2s */
    private static final long MUSIC_DELAY = 1200L;

    private Layer backgroundLayer;
    private Layer overlayLayer;
    private Layer logoLayer;
    private final List<TitleScreenButton> buttons = new ArrayList<>();
    /** 当前播放的 BGM ISound，用于离开界面时精确停止 */
    private static ISound currentBGM = null;

    // 按钮数据（原游戏 anchoredPosition + forge-1 换算）
    private static final String[] BUTTON_NAMES = {"LoadGame", "NewGame", "Gallery", "Options", "Exit"};
    private static final float[] BUTTON_CX = {249, 562, 836, 1086, 1299};
    private static final float[] BUTTON_CY = {174, 221, 145, 187, 129};
    private static final int[] BUTTON_W = {498, 437, 362, 338, 277};
    private static final int[] BUTTON_H = {323, 301, 251, 230, 190};
    private static final float[] NORMAL_OX = {0, -10, -8, -10, -11};
    private static final float[] NORMAL_OY = {4, 8, 8, 2, -7};
    private static final int[] COL_W = {290, 260, 246, 248, 164};
    private static final int[] COL_H = {230, 206, 122, 118, 106};
    private static final int[] LABEL_W = {149, 111, 60, 125, 59};
    private static final int[] LABEL_H = {35, 34, 30, 29, 28};
    private static final float[] LABEL_OX = {216.5f, 191f, 168f, 120.5f, 120f};
    private static final float[] LABEL_OY = {243f, 217.5f, 150.5f, 152.5f, 135f};

    // 动画时钟
    private static long animStart = 0;
    private static boolean firstInit = true;
    private static int prevW = -1, prevH = -1;

    public ManosabaTitleScreen() {
        animStart = System.currentTimeMillis();
        currentBGM = null; // 新实例 → BGM 从头播（游戏退出回来时）
        initLayers();
    }

    // ==================== 初始化 ====================

    @Override
    public void initGui() {
        super.initGui();
        boolean reShow = !firstInit && width == prevW && height == prevH;
        prevW = width;
        prevH = height;
        firstInit = false;

        // resize 或 ESC 回来：不重置动画/BGM，仅重建按钮
        // 游戏退出回来：新实例 → 构造时已设 animStart → 全流程重播

        buttons.clear();
        initButtons();
    }

    private void initLayers() {
        backgroundLayer = new Layer(TextureConst.background(),
                0, 0, 2560, 1440, 1.1f, 1f, VIRTUAL_SCREEN);
        overlayLayer = new Layer(TextureConst.TITLE_OVERLAY,
                0, 0, 2560, 1440, 1f, 0f, VIRTUAL_SCREEN);
        float logoCx = 1280 + 747, logoCy = 1440 - (720 + 381);
        logoLayer = new Layer(isChinese() ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA,
                logoCx - LOGO_W / 2f, logoCy - LOGO_H / 2f,
                LOGO_W, LOGO_H, 1f, 0f, VIRTUAL_SCREEN);
    }

    private void initButtons() {
        for (int i = 0; i < BUTTON_NAMES.length; i++) {
            String name = BUTTON_NAMES[i];
            float x = BUTTON_CX[i] - BUTTON_W[i] / 2f + NORMAL_OX[i];
            float y = (1440 - BUTTON_CY[i]) - BUTTON_H[i] / 2f + NORMAL_OY[i];

            TitleScreenButton b = new TitleScreenButton(x, y, BUTTON_W[i], BUTTON_H[i],
                    btnTex(name, false), btnTex(name, true),
                    VIRTUAL_SCREEN, 1f);
            b.setClickSound(clickSound(name));
            b.setCollisionSize(COL_W[i], COL_H[i]);
            b.setLabelTexture(lblTex(name), LABEL_W[i], LABEL_H[i], LABEL_OX[i], LABEL_OY[i]);
            b.setOnClick(bb -> onButtonClick(name));
            buttons.add(b);
        }
    }

    // ==================== 动画时钟 ====================

    private static long elapsed() { return System.currentTimeMillis() - animStart; }
    private static float bgProg() { return Math.min((float) elapsed() / BG_DURATION, 1f); }
    private static float uiProg() {
        long e = elapsed();
        return e < UI_DELAY ? 0f : Math.min((float) (e - UI_DELAY) / UI_DURATION, 1f);
    }

    /** 更新所有元素的状态（基于 animStart 时钟，不依赖 per-instance startTime） */
    private void tickAnim() {
        float bg = bgProg(), ui = uiProg();
        backgroundLayer.setScale(1.1f - 0.1f * bg);
        overlayLayer.setAlpha(ui);
        logoLayer.setAlpha(ui);
        for (TitleScreenButton b : buttons) b.setAlpha(ui);
    }

    // ==================== BGM 管理 ====================

    /** 停止 BGM（离开界面时 / 重新播放前） */
    private static void stopBGM() {
        if (currentBGM != null) {
            Minecraft.getMinecraft().getSoundHandler().stopSound(currentBGM);
            currentBGM = null;
        }
    }

    /** 播放 BGM（先停旧的再播新的，防止叠加） */
    private static void startBGM(SoundEvent event) {
        stopBGM();
        currentBGM = PositionedSoundRecord.getMasterRecord(event, 1.0f);
        Minecraft.getMinecraft().getSoundHandler().playSound(currentBGM);
    }

    // ==================== 游戏循环 ====================

    @Override
    public void updateScreen() {
        super.updateScreen();
        tickAnim();

        // 延迟启动 BGM（不打断开场白）
        if (currentBGM == null && elapsed() > MUSIC_DELAY) {
            startBGM(ManosabaSounds.TITLE_MUSIC);
        }
    }

    @Override
    public void onGuiClosed() {
        // 切到子界面（Options/世界选择）时不停止 BGM，让它继续播放。
        // BGM 只会在退出游戏（shutdown）时自然停止。
    }

    // ==================== 渲染 ====================

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int sw = width, sh = height;
        int cw, ch;
        if (sw * 9 > sh * 16) { cw = sh * 16 / 9; ch = sh; }
        else { cw = sw; ch = sw * 9 / 16; }
        int cx = (sw - cw) / 2, cy = (sh - ch) / 2;
        VIRTUAL_SCREEN.setPracticalWidth(cw);
        VIRTUAL_SCREEN.setPracticalHeight(ch);
        VIRTUAL_SCREEN.setCurrentX(cx);
        VIRTUAL_SCREEN.setCurrentY(cy);

        logoLayer.setTexture(isChinese() ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA);

        // 背景黑色
        drawRect(0, 0, sw, sh, 0xFF000000);

        // Scissor
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int sf = new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        GL11.glScissor(cx * sf, Minecraft.getMinecraft().displayHeight - (cy + ch) * sf, cw * sf, ch * sf);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 背景
        renderBgCrop();

        // Overlay + Logo
        overlayLayer.render();
        logoLayer.render();

        // 按钮
        for (TitleScreenButton b : buttons) b.render(mouseX, mouseY);

        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.color(1, 1, 1, 1);

        // 版本号
        float ui = uiProg();
        if (ui > 0f) {
            int a = Math.round(ui * 255);
            String v = "Ver. 1.12.2";
            fontRenderer.drawString(v,
                    cx + cw - 60 - fontRenderer.getStringWidth(v) / 2,
                    cy + ch - 24, (a << 24) | 0xFFFFFF);
        }
    }

    /** 背景 ContentScale.Crop */
    private void renderBgCrop() {
        float zoom = backgroundLayer.getScale();
        float alpha = backgroundLayer.getAlpha();
        float baseU = 2048f * (16f / 9f) / 4096f; // 0.8889
        float uFrac = baseU / zoom, vFrac = 1f / zoom;
        float u0 = (1f - uFrac) / 2f, v0 = (1f - vFrac) / 2f;

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureConst.background());
        GlStateManager.color(1, 1, 1, alpha);

        float px = VIRTUAL_SCREEN.toPracticalX(0);
        float py = VIRTUAL_SCREEN.toPracticalY(0);
        float pw = VIRTUAL_SCREEN.toPracticalWidth(2560);
        float ph = VIRTUAL_SCREEN.toPracticalHeight(1440);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder buf = t.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(px, py, 0).tex(u0, v0).endVertex();
        buf.pos(px, py + ph, 0).tex(u0, v0 + vFrac).endVertex();
        buf.pos(px + pw, py + ph, 0).tex(u0 + uFrac, v0 + vFrac).endVertex();
        buf.pos(px + pw, py, 0).tex(u0 + uFrac, v0).endVertex();
        t.draw();
    }

    // ==================== 交互 ====================

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        for (TitleScreenButton b : buttons) if (b.mouseClicked(x, y, btn)) return;
        super.mouseClicked(x, y, btn);
    }

    private void onButtonClick(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        switch (name) {
            case "LoadGame" -> mc.displayGuiScreen(new GuiWorldSelection(this));
            case "NewGame" -> mc.displayGuiScreen(new GuiCreateWorld(this));
            case "Gallery" -> mc.displayGuiScreen(new GuiMultiplayer(this));
            case "Options" -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
            case "Exit" -> mc.shutdown();
        }
    }

    // ==================== 工具 ====================

    private static ResourceLocation btnTex(String name, boolean hi) {
        return hi ? switch (name) {
            case "LoadGame" -> TextureConst.BUTTON_LOAD_GAME_HIGHLIGHTED;
            case "NewGame" -> TextureConst.BUTTON_NEW_GAME_HIGHLIGHTED;
            case "Gallery" -> TextureConst.BUTTON_GALLERY_HIGHLIGHTED;
            case "Options" -> TextureConst.BUTTON_OPTIONS_HIGHLIGHTED;
            case "Exit" -> TextureConst.BUTTON_EXIT_HIGHLIGHTED;
            default -> throw new IllegalArgumentException(name);
        } : switch (name) {
            case "LoadGame" -> TextureConst.BUTTON_LOAD_GAME_NORMAL;
            case "NewGame" -> TextureConst.BUTTON_NEW_GAME_NORMAL;
            case "Gallery" -> TextureConst.BUTTON_GALLERY_NORMAL;
            case "Options" -> TextureConst.BUTTON_OPTIONS_NORMAL;
            case "Exit" -> TextureConst.BUTTON_EXIT_NORMAL;
            default -> throw new IllegalArgumentException(name);
        };
    }

    private static boolean isChinese() {
        String l = Minecraft.getMinecraft().gameSettings.language;
        return l != null && l.startsWith("zh");
    }

    private static ResourceLocation lblTex(String name) {
        return switch (name) {
            case "LoadGame" -> TextureConst.LABEL_LOAD_GAME;
            case "NewGame" -> TextureConst.LABEL_NEW_GAME;
            case "Gallery" -> TextureConst.LABEL_GALLERY;
            case "Options" -> TextureConst.LABEL_OPTIONS;
            case "Exit" -> TextureConst.LABEL_EXIT;
            default -> null;
        };
    }

    private static SoundEvent clickSound(String name) {
        return "NewGame".equals(name) ? ManosabaSounds.BUTTON_CLICK_START_GAME : ManosabaSounds.BUTTON_CLICK_SUBMIT;
    }
}
