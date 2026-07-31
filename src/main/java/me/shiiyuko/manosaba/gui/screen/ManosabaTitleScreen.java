package me.shiiyuko.manosaba.gui.screen;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.shiiyuko.manosaba.Manosaba;
import me.shiiyuko.manosaba.constant.TextureConst;
import me.shiiyuko.manosaba.gui.Layer;
import me.shiiyuko.manosaba.gui.TitleScreenButton;
import me.shiiyuko.manosaba.gui.VirtualScreen;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Manosaba 标题画面。布局还原自原游戏（魔法少女ノ魔女裁判）TitleUI.prefab。
 * 适配 1.21.1 NeoForge。
 */
public class ManosabaTitleScreen extends TitleScreen {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(2560, 1440);

    private static final int LOGO_W = 1039;
    private static final int LOGO_H = 622;

    // 动画时序（毫秒），对齐原游戏 System_Title.nani
    private static final long BG_DURATION = 2700L;
    private static final long UI_DELAY = BG_DURATION + 250L;
    private static final long UI_DURATION = 500L;
    private static final long BLACK_FADE_MS = 1800L;

    // 入场模糊
    private static final int BLUR_W = 640;
    private static final int BLUR_H = 360;

    private final List<Tickable> tickables = Lists.newArrayList();
    private final ExitDialog exitDialog = new ExitDialog(VIRTUAL_SCREEN);

    private Layer backgroundLayer;
    private Layer logoLayer;
    private RenderTarget blurTarget;

    // 标题 BGM 自行管理（MusicManager 在标题屏被抑制）
    private boolean musicStarted = false;
    private SoundInstance titleMusic;

    // 主界面按钮
    private final List<TitleScreenButton> titleButtons = Lists.newArrayList();

    public ManosabaTitleScreen() {
        super(true);
    }

    @Override
    protected void init() {
        this.tickables.clear();
        this.clearWidgets();
        initLayers();
        initButtons();
    }

    private void initLayers() {
        // 背景：1.05×→1.0× 缩放，EaseOutQuad，2700ms
        backgroundLayer = new Layer(TextureConst.background(),
                0, 0, 2560, 1440, 1.05f, 1f, VIRTUAL_SCREEN);
        backgroundLayer.setDelay(0L);
        backgroundLayer.setDuration(BG_DURATION);
        backgroundLayer.setScaleFunction((t, now) -> 1.0f + 0.05f * (1f - t) * (1f - t));

        Layer overlayLayer = new Layer(TextureConst.TITLE_OVERLAY,
                0, 0, 2560, 1440, 1f, 0f, VIRTUAL_SCREEN);
        overlayLayer.setDelay(UI_DELAY);
        overlayLayer.setDuration(UI_DURATION);
        overlayLayer.setAlphaFunction((t, now) -> t);

        float logoCenterX = 1280 + 747;
        float logoCenterY = 1440 - (720 + 381);
        ResourceLocation logoTex = isChineseLocale()
                ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA;
        logoLayer = new Layer(logoTex, logoCenterX - LOGO_W / 2f, logoCenterY - LOGO_H / 2f,
                LOGO_W, LOGO_H, 1f, 0f, VIRTUAL_SCREEN);
        logoLayer.setDelay(UI_DELAY);
        logoLayer.setDuration(UI_DURATION);
        logoLayer.setAlphaFunction((t, now) -> t);

        addChild(backgroundLayer);
        addChild(overlayLayer);
        addChild(logoLayer);
    }

    private void initButtons() {
        boolean loadLocked = Minecraft.getInstance().getLevelSource()
                .findLevelCandidates().isEmpty();
        String[] names = {"LoadGame", "NewGame", "Gallery", "Options", "Exit"};

        float[] cx = {249, 562, 836, 1086, 1299};
        float[] cy = {174, 221, 145, 187, 129};
        int[] widths = {498, 437, 362, 338, 277};
        int[] heights = {323, 301, 251, 230, 190};
        float[] normalOx = {0, -10, -8, -10, -11};
        float[] normalOy = {4, 8, 8, 2, -7};
        int[] colW = {290, 260, 246, 248, 164};
        int[] colH = {230, 206, 122, 118, 106};
        int[] labelW = {149, 111, 60, 125, 59};
        int[] labelH = {35, 34, 30, 29, 28};
        float[] labelOx = {216.5f, 191f, 168f, 120.5f, 120f};
        float[] labelOy = {243f, 217.5f, 150.5f, 152.5f, 135f};

        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            float x = cx[i] - widths[i] / 2f + normalOx[i];
            float y = (1440 - cy[i]) - heights[i] / 2f + normalOy[i];

            boolean locked = "LoadGame".equals(name) && loadLocked;
            ResourceLocation normal = locked ? TextureConst.BUTTON_LOAD_GAME_LOCKED : buttonTexture(name, false);
            ResourceLocation highlighted = locked ? TextureConst.BUTTON_LOAD_GAME_LOCKED : buttonTexture(name, true);

            TitleScreenButton button = new TitleScreenButton(x, y, widths[i], heights[i],
                    normal, highlighted, VIRTUAL_SCREEN, 0f);
            button.setDelay(UI_DELAY);
            button.setDuration(UI_DURATION);
            button.setAlphaFunction((t, now) -> t);
            button.setCollisionSize(colW[i], colH[i]);
            if (!locked) {
                button.setClickSound(clickSoundFor(name));
                button.setLabelTexture(labelTexture(name), labelW[i], labelH[i], labelOx[i], labelOy[i]);
                button.setOnClick(b -> onButtonClick(name));
            } else {
                button.setHoverable(false);
            }

            addChild(button);
            addWidget(button);
            titleButtons.add(button);
        }
    }

    private static ResourceLocation buttonTexture(String name, boolean highlighted) {
        return switch (name) {
            case "LoadGame" -> highlighted ? TextureConst.BUTTON_LOAD_GAME_HIGHLIGHTED : TextureConst.BUTTON_LOAD_GAME_NORMAL;
            case "NewGame" -> highlighted ? TextureConst.BUTTON_NEW_GAME_HIGHLIGHTED : TextureConst.BUTTON_NEW_GAME_NORMAL;
            case "Gallery" -> highlighted ? TextureConst.BUTTON_GALLERY_HIGHLIGHTED : TextureConst.BUTTON_GALLERY_NORMAL;
            case "Options" -> highlighted ? TextureConst.BUTTON_OPTIONS_HIGHLIGHTED : TextureConst.BUTTON_OPTIONS_NORMAL;
            case "Exit" -> highlighted ? TextureConst.BUTTON_EXIT_HIGHLIGHTED : TextureConst.BUTTON_EXIT_NORMAL;
            default -> throw new IllegalArgumentException(name);
        };
    }

    private static boolean isChineseLocale() {
        String code = Minecraft.getInstance().options.languageCode;
        return code != null && code.startsWith("zh");
    }

    private static ResourceLocation labelTexture(String name) {
        return switch (name) {
            case "LoadGame" -> TextureConst.LABEL_LOAD_GAME;
            case "NewGame" -> TextureConst.LABEL_NEW_GAME;
            case "Gallery" -> TextureConst.LABEL_GALLERY;
            case "Options" -> TextureConst.LABEL_OPTIONS;
            case "Exit" -> TextureConst.LABEL_EXIT;
            default -> null;
        };
    }

    private static SoundEvent clickSoundFor(String name) {
        return switch (name) {
            case "LoadGame" -> ManosabaSounds.SFX_SYSTEM_LOADDATA.get();
            case "NewGame" -> ManosabaSounds.SFX_SYSTEM_STARTGAME.get();
            default -> ManosabaSounds.SFX_SYSTEM_SUBMIT.get();
        };
    }

    private void onButtonClick(String name) {
        Minecraft mc = this.minecraft;
        switch (name) {
            case "LoadGame" -> mc.setScreen(new SelectWorldScreen(this));
            case "NewGame" -> CreateWorldScreen.openFresh(mc, this);
            case "Gallery" -> {
                Screen next = mc.options.skipMultiplayerWarning
                        ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
                mc.setScreen(next);
            }
            case "Options" -> mc.setScreen(new OptionsScreen(this, mc.options));
            case "Exit" -> exitDialog.open();
        }
    }

    @Override
    public void tick() {
        super.tick();
        // BGM 在首 tick 即播放
        if (!musicStarted) {
            musicStarted = true;
            this.titleMusic = SimpleSoundInstance.forMusic(ManosabaSounds.TITLE_MUSIC.get());
            Minecraft.getInstance().getSoundManager().play(this.titleMusic);
            Manosaba.titleMusicPlaying = true;
        } else if (this.titleMusic != null
                && !this.exitDialog.isQuitting()
                && Minecraft.getInstance().getOverlay() == null
                && !Minecraft.getInstance().getSoundManager().isActive(this.titleMusic)) {
            // 切语言 SoundManager 重建后重播
            Minecraft.getInstance().getSoundManager().play(this.titleMusic);
        }
        // 对话框打开时底层按钮不响应悬停
        boolean dialogOpen = this.exitDialog.isOpen();
        for (TitleScreenButton b : this.titleButtons) {
            b.setHoverable(!dialogOpen);
        }
        this.tickables.forEach(Tickable::tick);
        this.exitDialog.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (exitDialog.isOpen()) return exitDialog.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (exitDialog.isOpen()) return exitDialog.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int sw = width, sh = height;
        int cw, ch;
        if (sw * 9 > sh * 16) { cw = sh * 16 / 9; ch = sh; }
        else { cw = sw; ch = sw * 9 / 16; }
        int cx = (sw - cw) / 2, cy = (sh - ch) / 2;
        VIRTUAL_SCREEN.setPracticalWidth(cw);
        VIRTUAL_SCREEN.setPracticalHeight(ch);
        VIRTUAL_SCREEN.setCurrentX(cx);
        VIRTUAL_SCREEN.setCurrentY(cy);

        logoLayer.setTexture(isChineseLocale()
                ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA);

        guiGraphics.fill(0, 0, sw, sh, 0xFF000000);
        guiGraphics.enableScissor(cx, cy, cw + cx, ch + cy);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        renderBackgroundCrop(guiGraphics);

        // 入场模糊
        long elapsed = System.currentTimeMillis() - getStartTime();
        float blurPower = 1f - Mth.clamp((float) elapsed / BG_DURATION, 0f, 1f);
        if (blurPower > 0.001f) {
            renderBlurredBackground(guiGraphics, blurPower, cx, cy, cw, ch);
        }

        // 退出确认后主界面 UI 随黑幕同步淡出
        float uiFade = 1f - exitDialog.getQuitFade();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (int i = 1; i < this.renderables.size(); i++) {
            Renderable r = this.renderables.get(i);
            if (uiFade < 1f) {
                if (r instanceof Layer l) l.setAlpha(l.getAlpha() * uiFade);
                else if (r instanceof TitleScreenButton b) b.setAlpha(b.getAlpha() * uiFade);
            }
            r.render(guiGraphics, mouseX, mouseY, delta);
        }
        RenderSystem.disableBlend();
        guiGraphics.disableScissor();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 版本号
        float uiAlpha = computeUiAlpha() * uiFade;
        if (uiAlpha > 0f) {
            String version = "Ver. " + SharedConstants.getCurrentVersion().getName();
            int alpha = Math.round(uiAlpha * 255f);
            int color = (alpha << 24) | 0xFFFFFF;
            guiGraphics.drawCenteredString(this.font, Component.literal(version),
                    cx + cw - 60, cy + ch - 24, color);
        }

        // 全屏黑幕淡出
        float blackAlpha = 1f - Mth.clamp((float) elapsed / BLACK_FADE_MS, 0f, 1f);
        if (blackAlpha > 0f) {
            guiGraphics.fill(0, 0, sw, sh, (Math.round(blackAlpha * 255f) << 24) | 0x000000);
        }

        // 退出确认对话框
        this.exitDialog.render(guiGraphics, mouseX, mouseY, delta);

        // 退出黑幕淡入
        float quitFade = this.exitDialog.getQuitFade();
        if (quitFade > 0f) {
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, quitFade);
            RenderSystem.setShaderTexture(0, TextureConst.BLACK);
            RenderUtils.blit(0, 0, sw, sh, guiGraphics.pose());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void renderBackgroundCrop(GuiGraphics guiGraphics) {
        RenderSystem.setShaderTexture(0, TextureConst.background());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, backgroundLayer.getAlpha());

        float px = VIRTUAL_SCREEN.toPracticalX(0);
        float py = VIRTUAL_SCREEN.toPracticalY(0);
        float pw = VIRTUAL_SCREEN.toPracticalWidth(2560);
        float ph = VIRTUAL_SCREEN.toPracticalHeight(1440);
        drawBackgroundQuad(guiGraphics.pose(), px, py, pw, ph, backgroundLayer.getScale());
    }

    private void drawBackgroundQuad(PoseStack poseStack, float px, float py,
                                    float pw, float ph, float zoomScale) {
        final float BASE_U = 2048f * (16f / 9f) / 4096f;
        final float BASE_V = 1.0f;
        float uFrac = BASE_U / zoomScale;
        float vFrac = BASE_V / zoomScale;
        float texU0 = (1f - uFrac) / 2f;
        float texV0 = (1f - vFrac) / 2f;
        float texU1 = texU0 + uFrac;
        float texV1 = texV0 + vFrac;

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, px, py, 0).setUv(texU0, texV0);
        buffer.addVertex(matrix, px, py + ph, 0).setUv(texU0, texV1);
        buffer.addVertex(matrix, px + pw, py + ph, 0).setUv(texU1, texV1);
        buffer.addVertex(matrix, px + pw, py, 0).setUv(texU1, texV0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    // ---- 入场模糊 ----

    private void renderBlurredBackground(GuiGraphics guiGraphics, float blurPower,
                                         int scissorX, int scissorY, int scissorW, int scissorH) {
        if (blurTarget == null) {
            blurTarget = new TextureTarget(BLUR_W, BLUR_H, true, Minecraft.ON_OSX);
            blurTarget.setFilterMode(GL11.GL_LINEAR);
        }

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        RenderSystem.disableScissor();
        blurTarget.bindWrite(false);
        RenderSystem.viewport(0, 0, BLUR_W, BLUR_H);
        Matrix4f prevProjection = RenderSystem.getProjectionMatrix();
        VertexSorting prevSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, BLUR_W, BLUR_H, 0.0F, 1000.0F, 3000.0F),
                VertexSorting.DISTANCE_TO_ORIGIN);
        PoseStack fboPose = new PoseStack();
        RenderSystem.setShaderTexture(0, TextureConst.background());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        drawBackgroundQuad(fboPose, 0, 0, BLUR_W, BLUR_H, backgroundLayer.getScale());
        RenderSystem.setProjectionMatrix(prevProjection, prevSorting);
        main.bindWrite(true);
        RenderSystem.enableScissor(scissorX, scissorY, scissorX + scissorW, scissorY + scissorH);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blurPower);
        RenderSystem.setShaderTexture(0, blurTarget.getColorTextureId());
        RenderUtils.blit(VIRTUAL_SCREEN.toPracticalX(0), VIRTUAL_SCREEN.toPracticalY(0),
                VIRTUAL_SCREEN.toPracticalWidth(2560), VIRTUAL_SCREEN.toPracticalHeight(1440),
                guiGraphics.pose());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private float computeUiAlpha() {
        long elapsed = System.currentTimeMillis() - getStartTime();
        if (elapsed < UI_DELAY) return 0f;
        return Mth.clamp((float) (elapsed - UI_DELAY) / UI_DURATION, 0f, 1f);
    }

    private long getStartTime() {
        Long st = backgroundLayer.getStartTime();
        return st != null ? st : System.currentTimeMillis();
    }

    public <T extends Renderable & Tickable> void addChild(T child) {
        this.tickables.add(child);
        this.addRenderableOnly(child);
    }

    @Override
    protected void rebuildWidgets() {}
}
