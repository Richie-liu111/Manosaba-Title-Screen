package me.shiiyuko.manosaba.gui;

import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.constant.TextureConst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
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

    // 动画时序（毫秒），对齐原游戏 System_Title.nani：
    // @animate Stills Scale:1.0 easing:EaseOutQuad time:2.7（缩放+模糊 2700ms）
    // @back Overlay Transparent time:1.8（全屏黑幕淡出 1800ms）
    private static final long BG_DURATION = 2700L;
    private static final long UI_DELAY = BG_DURATION + 250L;
    private static final long UI_DURATION = 500L;
    private static final long BLACK_FADE_MS = 1800L;

    // 背景纹理 4096×2048（2:1），目标 16:9 UV 裁剪比例
    private static final float BASE_CROP_U = 2048f * (16f / 9f) / 4096f;

    // 入场模糊：低分辨率离屏渲染 + 上采样（同现代版模糊技术）
    private static final int BLUR_W = 640;
    private static final int BLUR_H = 360;

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
    /** 进世界标记：从世界返回标题画面时触发动画重播。 */
    public static boolean inGamed = false;

    // 音效
    private static final ResourceLocation SOUND_CLICK = new ResourceLocation("manosaba", "button_click_submit");
    private static final ResourceLocation SOUND_LOADDATA = new ResourceLocation("manosaba", "sfx_system_loaddata_001");
    private static final ResourceLocation SOUND_STARTGAME = new ResourceLocation("manosaba", "sfx_system_startgame_001");

    // 入场模糊 FBO 资源（-1 = 不支持或未初始化）
    private int blurFbo = -1;
    private int blurTexture = -1;
    private boolean blurAvailable = false;

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
        // 背景：1.05×→1.0× 缩放（原游戏 Scale:{default*1.05}→1.0），EaseOutQuad，2700ms。
        backgroundLayer = new Layer(TextureConst.background(),
                0, 0, 2560, 1440, 1.05f, 1f, VIRTUAL_SCREEN);
        backgroundLayer.setDelay(0L);
        backgroundLayer.setDuration(BG_DURATION);
        backgroundLayer.setScaleFunction((t, now) -> 1.0f + 0.05f * (1f - t) * (1f - t));

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
        // 检测是否有存档（1.7.10 无 findLevelCandidates，直接检查 saves 目录）
        boolean loadLocked = !hasAnySaves();
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
            boolean locked = "LoadGame".equals(name) && loadLocked;
            if (locked) {
                // Locked 态：双态同为锁定纹理 → 无悬停高亮
                normal = TextureConst.BUTTON_LOAD_GAME_LOCKED;
                highlighted = TextureConst.BUTTON_LOAD_GAME_LOCKED;
            }
            TitleScreenButton btn = new TitleScreenButton(
                    x, y, widths[i], heights[i],
                    normal, highlighted, VIRTUAL_SCREEN, 0f);
            btn.setDelay(UI_DELAY);
            btn.setDuration(UI_DURATION);
            btn.setAlphaFunction((t, now) -> t);
            btn.setCollisionSize(colW[i], colH[i]);
            if (!locked) {
                btn.setClickSound(clickSoundFor(name));
                btn.setLabelTexture(labelTexture(name), labelW[i], labelH[i], labelOx[i], labelOy[i]);
                btn.setOnClick(b -> onButtonClick(name));
            } else {
                // Locked 态：不可悬停、不可点击、无音效
                btn.setHoverable(false);
            }

            buttons.add(btn);
        }
    }

    /** 检查 saves 目录是否有存档（1.7.10 兼容方式）。 */
    private static boolean hasAnySaves() {
        File savesDir = new File(Minecraft.getMinecraft().mcDataDir, "saves");
        if (!savesDir.isDirectory()) return false;
        String[] files = savesDir.list();
        if (files == null || files.length == 0) return false;
        // 检查是否有子目录（每个存档是一个子目录）
        for (String f : files) {
            if (new File(savesDir, f).isDirectory()) return true;
        }
        return false;
    }

    private void onButtonClick(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        // 音效由 TitleScreenButton.mousePressed() 播放，此处不再统一播放
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
        // 原游戏 System_Title.nani: @bgm 在 time:0 即播放（黑幕覆盖时音乐已响起）。
        // 循环检测：若音乐中断则重播（切语言 SoundManager 重建等场景）。
        if (currentMusic == null || !mc.getSoundHandler().isSoundPlaying(currentMusic)) {
            currentMusic = PositionedSoundRecord.func_147674_a(MANOSABA_MUSIC, 1.0F);
            mc.getSoundHandler().playSound(currentMusic);
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

        // 入场模糊：低分辨率离屏渲染 + 上采样，alpha = blurPower（1→0，2700ms）
        long elapsed = System.currentTimeMillis() - initTime;
        float blurPower = 1f - Math.min((float) elapsed / BG_DURATION, 1f);
        if (blurPower > 0.001f) {
            renderBlurredBackground(blurPower, currentX, currentY, currentWidth, currentHeight);
        }

        // 渲染 layers（跳过 backgroundLayer，它已在 renderBackgroundCrop 中单独渲染）
        for (int i = 1; i < layers.size(); i++) {
            layers.get(i).render(mouseX, mouseY, delta);
        }

        // 渲染按钮
        for (TitleScreenButton btn : buttons) {
            btn.render(mouseX, mouseY, delta);
        }

        // 版本号 — 右下角，完整显示
        float uiAlpha = computeUiAlpha();
        if (uiAlpha > 0f) {
            String version = "Ver. 1.7.10-GTNH";
            int alpha = Math.round(uiAlpha * 255f);
            int color = (alpha << 24) | 0xFFFFFF;
            int textW = mc.fontRenderer.getStringWidth(version);
            mc.fontRenderer.drawStringWithShadow(version, currentX + currentWidth - textW - 8, currentY + currentHeight - 16, color);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // 全屏黑幕淡出（原游戏 @back Overlay Transparent time:1.8，覆盖整个窗口）
        long blackAlpha = Math.max(0L, Math.min(BLACK_FADE_MS - elapsed, BLACK_FADE_MS));
        if (blackAlpha > 0) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            float bAlpha = (float) blackAlpha / BLACK_FADE_MS;
            drawBlackOverlay(bAlpha);
            GL11.glDisable(GL11.GL_BLEND);
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

    /** 绘制半透明黑色覆盖层（position-tex 路径，与元素渲染路径一致）。 */
    private void drawBlackOverlay(float alpha) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureConst.BLACK);
        RenderUtils.blit(0, 0, this.width, this.height);
    }

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

        // 背景纹理 4096×2048（2:1），目标 16:9。
        // 基准 (zoom=1)：U 采样 88.89%（= 16/9 / 2），V 采样 100%（全高）。
        // 放大 (zoom>1)：U 和 V 等比例缩小，保证采样区域始终是 16:9。
        final float BASE_V = 1.0f;
        float uFrac = BASE_CROP_U / zoomScale;
        float vFrac = BASE_V / zoomScale;
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

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(px, py + ph, 0, texU0, texV1);
        tess.addVertexWithUV(px + pw, py + ph, 0, texU1, texV1);
        tess.addVertexWithUV(px + pw, py, 0, texU1, texV0);
        tess.addVertexWithUV(px, py, 0, texU0, texV0);
        tess.draw();
    }

    // ---- 入场模糊 ----

    /** 初始化 FBO 资源。若 OpenGL 不支持 EXT_framebuffer_object，则保持 blurAvailable=false。 */
    private void ensureBlurFbo() {
        if (blurFbo >= 0) return;
        if (blurFbo == -2) return; // 已知不支持
        try {
            blurFbo = EXTFramebufferObject.glGenFramebuffersEXT();
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, blurFbo);

            blurTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, blurTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, BLUR_W, BLUR_H, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                    EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, blurTexture, 0);

            int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(
                    EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
            if (status == EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
                blurAvailable = true;
            } else {
                cleanupBlurFbo();
            }
        } catch (Exception e) {
            cleanupBlurFbo();
        }
        // 恢复默认帧缓冲和纹理
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    private void cleanupBlurFbo() {
        if (blurTexture >= 0) {
            GL11.glDeleteTextures(blurTexture);
            blurTexture = -1;
        }
        if (blurFbo >= 0) {
            EXTFramebufferObject.glDeleteFramebuffersEXT(blurFbo);
            blurFbo = -2; // 标记为已尝试但失败
        }
        blurAvailable = false;
    }

    /**
     * 入场模糊：把背景画进低分辨率 FBO（640×360），再以线性过滤上采样回屏幕，
     * alpha = blurPower 覆盖在清晰版上。低分辨率 + 线性插值放大 = 平滑高斯式模糊。
     */
    private void renderBlurredBackground(float blurPower,
                                         int scissorX, int scissorY, int scissorW, int scissorH) {
        ensureBlurFbo();
        if (!blurAvailable) return;

        // 保存当前视口和投影矩阵
        // LWJGL 2 要求 buffer 至少有 16 个 int 元素（64 字节），即使 GL_VIEWPORT 只返回 4 个值
        IntBuffer vpBuf = ByteBuffer.allocateDirect(64)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        GL11.glGetInteger(GL11.GL_VIEWPORT, vpBuf);
        int oldVpX = vpBuf.get(0);
        int oldVpY = vpBuf.get(1);
        int oldVpW = vpBuf.get(2);
        int oldVpH = vpBuf.get(3);

        // 切换到 FBO 渲染
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, blurFbo);
        GL11.glViewport(0, 0, BLUR_W, BLUR_H);

        // 设置 640×360 正交投影
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, BLUR_W, BLUR_H, 0, 1000.0, 3000.0);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        // 用当前背景 crop 绘制到 FBO（缩放由 backgroundLayer.getScale() 提供）
        float zoomScale = backgroundLayer.getScale();
        final float BASE_V = 1.0f;
        float uFrac = BASE_CROP_U / zoomScale;
        float vFrac = BASE_V / zoomScale;
        float texU0 = (1f - uFrac) / 2f;
        float texV0 = (1f - vFrac) / 2f;
        float texU1 = texU0 + uFrac;
        float texV1 = texV0 + vFrac;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureConst.background());
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(0, BLUR_H, 0, texU0, texV1);
        tess.addVertexWithUV(BLUR_W, BLUR_H, 0, texU1, texV1);
        tess.addVertexWithUV(BLUR_W, 0, 0, texU1, texV0);
        tess.addVertexWithUV(0, 0, 0, texU0, texV0);
        tess.draw();

        // 恢复投影和视口
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        GL11.glViewport(oldVpX, oldVpY, oldVpW, oldVpH);

        // 模糊版覆盖到屏幕（alpha = blurPower，线性过滤已在上方 tex 参数设置）
        GL11.glColor4f(1.0F, 1.0F, 1.0F, blurPower);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, blurTexture);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        float fboPx = VIRTUAL_SCREEN.toPracticalX(0);
        float fboPy = VIRTUAL_SCREEN.toPracticalY(0);
        float fboPw = VIRTUAL_SCREEN.toPracticalWidth(2560);
        float fboPh = VIRTUAL_SCREEN.toPracticalHeight(1440);

        tess.startDrawingQuads();
        tess.addVertexWithUV(fboPx, fboPy + fboPh, 0, 0, 1);
        tess.addVertexWithUV(fboPx + fboPw, fboPy + fboPh, 0, 1, 1);
        tess.addVertexWithUV(fboPx + fboPw, fboPy, 0, 1, 0);
        tess.addVertexWithUV(fboPx, fboPy, 0, 0, 0);
        tess.draw();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
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

    /** 每个按钮使用对应的原游戏系统音效。 */
    private static ResourceLocation clickSoundFor(String name) {
        switch (name) {
            case "LoadGame": return SOUND_LOADDATA;
            case "NewGame":  return SOUND_STARTGAME;
            default:         return SOUND_CLICK;
        }
    }
}
