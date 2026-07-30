package me.shiiyuko.manosaba.gui;

import cpw.mods.fml.client.GuiModList;
import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.constant.TextureConst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Manosaba 标题画面（1.7.10 GTNH 移植版）。
 * 基于 forge-1 的布局（2560×1440 虚拟空间，原游戏精确坐标），
 * 适配 1.7.10 的 GuiScreen + Tessellator 渲染管线。
 */
public class ManosabaTitleScreen extends GuiScreen {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(2560, 1440);

    // 纹理尺寸
    private static final int LOGO_W = 1039;
    private static final int LOGO_H = 622;

    // 动画时序（毫秒）
    private static final long BG_DURATION = 2500L;
    private static final long UI_DELAY = BG_DURATION + 250L;
    private static final long UI_DURATION = 500L;

    // 背景纹理 4096×2048（2:1），目标 16:9 UV 裁剪比例
    private static final float BASE_CROP_U = 2048f * (16f / 9f) / 4096f;

    // Layers
    private Layer backgroundLayer;
    private Layer overlayLayer;
    private Layer logoLayer;
    private final List<Layer> layers = new ArrayList<>();
    private final List<TitleScreenButton> buttons = new ArrayList<>();

    private long initTime;
    private boolean initialized = false;

    /** 退出标记：设为 true 后 MinecraftMixin 不再拦截 GuiMainMenu。 */
    public static boolean exit = false;
    // 背景音乐
    private static final ResourceLocation MANOSABA_MUSIC = new ResourceLocation("manosaba", "music");
    private static PositionedSoundRecord currentMusic = null;
    private static long soundStartTime = 0;
    private static final long MUSIC_DELAY = 1500L;
    /** 进世界标记：从世界返回标题画面时触发动画重播。 */
    public static boolean inGamed = false;

    // 点击音效
    private static final ResourceLocation SOUND_CLICK = new ResourceLocation("manosaba", "button_click_submit");
    private static final ResourceLocation SOUND_START = new ResourceLocation("manosaba", "button_click_start_game");

    public ManosabaTitleScreen() {
        this.initTime = System.currentTimeMillis();
    }

    @Override
    public void initGui() {
        // 从世界返回时重播动画
        if (inGamed) {
            this.initialized = false;
            inGamed = false;
        }
        if (initialized) return;
        this.layers.clear();
        this.buttons.clear();
        this.initTime = System.currentTimeMillis();
        initLayers();
        initButtons();
        this.initialized = true;
    }

    private void initLayers() {
        // 背景：1.1×→1.0× 缩放
        backgroundLayer = new Layer(TextureConst.background(),
                0, 0, 2560, 1440, 1.1f, 1f, VIRTUAL_SCREEN);
        backgroundLayer.setDelay(0L);
        backgroundLayer.setDuration(BG_DURATION);
        backgroundLayer.setScaleFunction((t, now) -> 1.1f + (1.0f - 1.1f) * t);

        // TitleOverlay：全屏画框
        overlayLayer = new Layer(TextureConst.TITLE_OVERLAY,
                0, 0, 2560, 1440, 1f, 0f, VIRTUAL_SCREEN);
        overlayLayer.setDelay(UI_DELAY);
        overlayLayer.setDuration(UI_DURATION);
        overlayLayer.setAlphaFunction((t, now) -> t);

        // TitleLogo
        float logoCenterX = 1280 + 747;
        float logoCenterY = 1440 - (720 + 381);
        ResourceLocation logoTex = isChineseLocale()
                ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA;
        logoLayer = new Layer(logoTex,
                logoCenterX - LOGO_W / 2f, logoCenterY - LOGO_H / 2f,
                LOGO_W, LOGO_H, 1f, 0f, VIRTUAL_SCREEN);
        logoLayer.setDelay(UI_DELAY);
        logoLayer.setDuration(UI_DURATION);
        logoLayer.setAlphaFunction((t, now) -> t);

        layers.add(backgroundLayer);
        layers.add(overlayLayer);
        layers.add(logoLayer);
    }

    private void initButtons() {
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

            ResourceLocation normal = buttonTexture(name, false);
            ResourceLocation highlighted = buttonTexture(name, true);
            TitleScreenButton btn = new TitleScreenButton(
                    x, y, widths[i], heights[i],
                    normal, highlighted, VIRTUAL_SCREEN, 0f);
            btn.setDelay(UI_DELAY);
            btn.setDuration(UI_DURATION);
            btn.setAlphaFunction((t, now) -> t);
            btn.setCollisionSize(colW[i], colH[i]);
            btn.setLabelTexture(labelTexture(name), labelW[i], labelH[i], labelOx[i], labelOy[i]);
            btn.setOnClick(b -> onButtonClick(name));

            buttons.add(btn);
        }
    }

    private void onButtonClick(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147673_a(
                "NewGame".equals(name) ? SOUND_START : SOUND_CLICK));

        switch (name) {
            case "LoadGame":   mc.displayGuiScreen(new GuiSelectWorld(this)); break;
            case "NewGame":    mc.displayGuiScreen(new GuiCreateWorld(this)); break;
            case "Gallery":    mc.displayGuiScreen(new GuiMultiplayer(this)); break;
            case "Options":    mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
            case "Exit":
                // 参考 YuZuUI：设置 exit 标记让 Mixin 不再拦截，
                // 然后 displayGuiScreen(null) 触发原版 GuiMainMenu
                // 停止音乐后再退出
                if (currentMusic != null && mc.getSoundHandler() != null) {
                    mc.getSoundHandler().stopSound(currentMusic);
                    currentMusic = null;
                    soundStartTime = 0;
                }
                exit = true;
                if (ManosabaConfig.justExit) {
                    mc.shutdown();
                } else {
                    mc.displayGuiScreen(null);
                }
                break;
        }
    }

    @Override
    public void updateScreen() {
        backgroundLayer.tick();
        for (TitleScreenButton btn : buttons) btn.tick();
        tickMusic();
    }

    private void tickMusic() {
        if (!ManosabaConfig.bgm) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getSoundHandler() == null) return;
        if (currentMusic == null || !mc.getSoundHandler().isSoundPlaying(currentMusic)) {
            long now = System.currentTimeMillis();
            if (soundStartTime == 0) soundStartTime = now;
            if (now - soundStartTime > MUSIC_DELAY) {
                currentMusic = PositionedSoundRecord.func_147674_a(MANOSABA_MUSIC, 1.0F);
                mc.getSoundHandler().playSound(currentMusic);
                soundStartTime = 0;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        int screenWidth = this.width;
        int screenHeight = this.height;

        // 16:9 letterbox
        int currentWidth, currentHeight;
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

        // Logo 随语言切换动态更新
        logoLayer.setTexture(isChineseLocale()
                ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA);

        // 全屏黑色背景
        drawRect(0, 0, screenWidth, screenHeight, 0xFF000000);

        // 开启混合
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // 渲染背景（ContentScale.Crop）
        renderBackgroundCrop();

        // 渲染 layers（跳过 backgroundsLayer，它已在 renderBackgroundCrop 中单独渲染）
        for (int i = 1; i < layers.size(); i++) {
            layers.get(i).render(mouseX, mouseY, delta);
        }

        // 渲染按钮
        for (TitleScreenButton btn : buttons) {
            btn.render(mouseX, mouseY, delta);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // 版本号 — 右下角，完整显示
        float uiAlpha = computeUiAlpha();
        if (uiAlpha > 0f) {
            String version = "Ver. 1.7.10-GTNH";
            int alpha = Math.round(uiAlpha * 255f);
            int color = (alpha << 24) | 0xFFFFFF;
            int textW = mc.fontRenderer.getStringWidth(version);
            mc.fontRenderer.drawStringWithShadow(version, currentX + currentWidth - textW - 8, currentY + currentHeight - 16, color);
        }

        // 上下黑条（letterbox 填充）
        if (currentX == 0 && currentY != 0) {
            drawRect(0, 0, screenWidth, currentY, 0xFF000000);
            drawRect(0, currentY + currentHeight, screenWidth, screenHeight, 0xFF000000);
        }
        if (currentY == 0 && currentX != 0) {
            drawRect(0, 0, currentX, screenHeight, 0xFF000000);
            drawRect(currentX + currentWidth, 0, screenWidth, screenHeight, 0xFF000000);
        }
    }

    // onGuiClosed() 不在此停止音乐 — 1.7.10 在切子画面时也会调用，
    // 导致音乐被打断。音乐的停止由 MusicTickerMixin 负责：检测画面
    // 是否已离开标题画面体系（进世界、退出等）。

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            for (TitleScreenButton btn : buttons) {
                btn.mousePressed(mouseX, mouseY);
            }
        }
    }

    private void renderBackgroundCrop() {
        float zoomScale = backgroundLayer.getScale();
        float alpha = backgroundLayer.getAlpha();

        float uFrac = BASE_CROP_U / zoomScale;
        float vFrac = 1.0f / zoomScale;
        float texU0 = (1f - uFrac) / 2f;
        float texV0 = (1f - vFrac) / 2f;
        float texU1 = texU0 + uFrac;
        float texV1 = texV0 + vFrac;

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureConst.background());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        float px = VIRTUAL_SCREEN.toPracticalX(0);
        float py = VIRTUAL_SCREEN.toPracticalY(0);
        float pw = VIRTUAL_SCREEN.toPracticalWidth(2560);
        float ph = VIRTUAL_SCREEN.toPracticalHeight(1440);

        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(px, py + ph, 0, texU0, texV1);
        tess.addVertexWithUV(px + pw, py + ph, 0, texU1, texV1);
        tess.addVertexWithUV(px + pw, py, 0, texU1, texV0);
        tess.addVertexWithUV(px, py, 0, texU0, texV0);
        tess.draw();
    }

    private float computeUiAlpha() {
        long elapsed = System.currentTimeMillis() - initTime;
        if (elapsed < UI_DELAY) return 0f;
        return Math.min((float) (elapsed - UI_DELAY) / UI_DURATION, 1f);
    }

    private static boolean isChineseLocale() {
        String lang = Minecraft.getMinecraft().gameSettings.language;
        return lang != null && lang.startsWith("zh");
    }

    private static ResourceLocation buttonTexture(String name, boolean highlighted) {
        switch (name) {
            case "LoadGame": return highlighted ? TextureConst.BUTTON_LOAD_GAME_HIGHLIGHTED : TextureConst.BUTTON_LOAD_GAME_NORMAL;
            case "NewGame":  return highlighted ? TextureConst.BUTTON_NEW_GAME_HIGHLIGHTED : TextureConst.BUTTON_NEW_GAME_NORMAL;
            case "Gallery":  return highlighted ? TextureConst.BUTTON_GALLERY_HIGHLIGHTED : TextureConst.BUTTON_GALLERY_NORMAL;
            case "Options":  return highlighted ? TextureConst.BUTTON_OPTIONS_HIGHLIGHTED : TextureConst.BUTTON_OPTIONS_NORMAL;
            case "Exit":     return highlighted ? TextureConst.BUTTON_EXIT_HIGHLIGHTED : TextureConst.BUTTON_EXIT_NORMAL;
            default: throw new IllegalArgumentException("Unknown button: " + name);
        }
    }

    private static ResourceLocation labelTexture(String name) {
        switch (name) {
            case "LoadGame": return TextureConst.LABEL_LOAD_GAME;
            case "NewGame":  return TextureConst.LABEL_NEW_GAME;
            case "Gallery":  return TextureConst.LABEL_GALLERY;
            case "Options":  return TextureConst.LABEL_OPTIONS;
            case "Exit":     return TextureConst.LABEL_EXIT;
            default: return null;
        }
    }
}
