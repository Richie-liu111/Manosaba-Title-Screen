package me.shiiyuko.manosaba.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.shiiyuko.manosaba.constant.TextureConst;
import me.shiiyuko.manosaba.gui.Layer;
import me.shiiyuko.manosaba.gui.TitleScreenButton;
import me.shiiyuko.manosaba.gui.VirtualScreen;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Manosaba 标题画面。布局还原自原游戏（魔法少女ノ魔女裁判）TitleUI.prefab：
 * <ul>
 *   <li>设计空间 2560×1440（原游戏 CanvasScaler ReferenceResolution）；</li>
 *   <li>背景图 ContentScale.Crop（2:1→16:9），1.1→1.0 呼吸动画；</li>
 *   <li>TitleOverlay 2560×1440 全屏画框 1:1 填充，延迟淡入；</li>
 *   <li>TitleLogo 位于原 anchoredPosition=(747,381)，原生尺寸；</li>
 *   <li>5 个按钮使用原游戏 anchoredPosition；</li>
 *   <li>版本号右下角；点击 Exit 直接退出游戏。</li>
 * </ul>
 * 适配 1.21.1 的 BufferBuilder API（addVertex + setUv + buildOrThrow）。
 */
public class ManosabaTitleScreen extends TitleScreen {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(2560, 1440);

    // 纹理尺寸（原游戏设计空间 2560×1440，素材以原生尺寸直接放置）
    private static final int LOGO_W = 1039;
    private static final int LOGO_H = 622;

    // 动画时序（毫秒）
    private static final long BG_DURATION = 2500L;
    private static final long UI_DELAY = BG_DURATION + 250L;
    private static final long UI_DURATION = 500L;

    private final List<Tickable> tickables = Lists.newArrayList();

    private Layer backgroundLayer;
    private Layer logoLayer;

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
        // 背景：1.1×→1.0× 缩放，持续 2500ms。虚拟空间填满，渲染时单独做 Crop。
        backgroundLayer = new Layer(TextureConst.background(),
                0, 0, 2560, 1440, 1.1f, 1f, VIRTUAL_SCREEN);
        backgroundLayer.setDelay(0L);
        backgroundLayer.setDuration(BG_DURATION);
        backgroundLayer.setScaleFunction((t, now) ->
                1.1f + (1.0f - 1.1f) * t);

        // TitleOverlay：全屏画框，纹理 2560×1440，1:1 填充虚拟画布。
        Layer overlayLayer = new Layer(TextureConst.TITLE_OVERLAY,
                0, 0, 2560, 1440, 1f, 0f, VIRTUAL_SCREEN);
        overlayLayer.setDelay(UI_DELAY);
        overlayLayer.setDuration(UI_DURATION);
        overlayLayer.setAlphaFunction((t, now) -> t);

        // TitleLogo：原游戏 anchoredPosition=(747, 381)，中心锚点。
        // 父节点 Wrapper 填满画布，其中心 = (1280, 720)。
        // Logo 绝对中心 = (1280+747, 720+381) = (2027, 1101 Y-up)。
        // 翻转为 Y-down：(2027, 339)。纹理 1039×622。
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

        addChild(backgroundLayer);
        addChild(overlayLayer);
        addChild(logoLayer);
    }

    private void initButtons() {
        // 原游戏按钮层级：Wrapper → Buttons(锚在0,0，零尺寸) → 各按钮(中心锚点)。
        // 因为 Buttons 是零尺寸点，按钮 anchoredPosition 直接就是距画布左下角的绝对坐标。
        // 转换为 forge-1 (Y-down, 原点左上): X 不变，Y = 1440 − unityY。
        String[] names = {"LoadGame", "NewGame", "Gallery", "Options", "Exit"};

        // 原游戏 anchoredPosition（直接 = 按钮中心距画布左下角的偏移）
        float[] cx = {249, 562, 836, 1086, 1299};
        float[] cy = {174, 221, 145, 187, 129};
        // 原游戏 Sprite 尺寸（Normal 状态）— 渲染用
        int[] widths = {498, 437, 362, 338, 277};
        int[] heights = {323, 301, 251, 230, 190};
        // Normal 容器相对于按钮根节点的偏移（Unity → MC Y-down 翻转）
        // 按钮精灵并非从根节点中心画起，而是从 Normal 子容器的偏移处开始
        float[] normalOx = {0, -10, -8, -10, -11};
        float[] normalOy = {4, 8, 8, 2, -7};
        // 原游戏按钮容器 SizeDelta — 碰撞检测用（比精灵图小，避免透明区域误触）
        int[] colW = {290, 260, 246, 248, 164};
        int[] colH = {230, 206, 122, 118, 106};
        // 中文标签精灵尺寸 + 相对于按钮精灵左上角的精确偏移（TitleUI.prefab RectTransform 换算）
        int[] labelW = {149, 111, 60, 125, 59};
        int[] labelH = {35, 34, 30, 29, 28};
        float[] labelOx = {216.5f, 191f, 168f, 120.5f, 120f};
        float[] labelOy = {243f, 217.5f, 150.5f, 152.5f, 135f};

        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            // Unity Y-up (左下原点) → MC Y-down (左上原点)
            // 加上 Normal 容器偏移：按钮精灵从 Normal 子容器渲染，其 anchoredPosition
            // 决定了精灵相对于按钮根节点中心的实际起始位置
            float x = cx[i] - widths[i] / 2f + normalOx[i];
            float y = (1440 - cy[i]) - heights[i] / 2f + normalOy[i];

            ResourceLocation normal = buttonTexture(name, false);
            ResourceLocation highlighted = buttonTexture(name, true);
            TitleScreenButton button = new TitleScreenButton(
                    x, y, widths[i], heights[i],
                    normal, highlighted, VIRTUAL_SCREEN, 0f);
            button.setDelay(UI_DELAY);
            button.setDuration(UI_DURATION);
            button.setAlphaFunction((t, now) -> t);
            button.setClickSound(clickSoundFor(name));
            button.setCollisionSize(colW[i], colH[i]);
            button.setLabelTexture(labelTexture(name), labelW[i], labelH[i], labelOx[i], labelOy[i]);
            button.setOnClick(b -> onButtonClick(name));

            addChild(button);
            addWidget(button);
        }
    }

    private static SoundEvent clickSoundFor(String name) {
        SoundEvent ev = "NewGame".equals(name)
                ? ManosabaSounds.BUTTON_CLICK_START_GAME.get()
                : ManosabaSounds.BUTTON_CLICK_SUBMIT.get();
        return ev;
    }

    private static ResourceLocation buttonTexture(String name, boolean highlighted) {
        return switch (name) {
            case "LoadGame" -> highlighted ? TextureConst.BUTTON_LOAD_GAME_HIGHLIGHTED
                    : TextureConst.BUTTON_LOAD_GAME_NORMAL;
            case "NewGame" -> highlighted ? TextureConst.BUTTON_NEW_GAME_HIGHLIGHTED
                    : TextureConst.BUTTON_NEW_GAME_NORMAL;
            case "Gallery" -> highlighted ? TextureConst.BUTTON_GALLERY_HIGHLIGHTED
                    : TextureConst.BUTTON_GALLERY_NORMAL;
            case "Options" -> highlighted ? TextureConst.BUTTON_OPTIONS_HIGHLIGHTED
                    : TextureConst.BUTTON_OPTIONS_NORMAL;
            case "Exit" -> highlighted ? TextureConst.BUTTON_EXIT_HIGHLIGHTED
                    : TextureConst.BUTTON_EXIT_NORMAL;
            default -> throw new IllegalArgumentException("Unknown button: " + name);
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

    private void onButtonClick(String name) {
        Minecraft mc = this.minecraft;
        switch (name) {
            case "LoadGame" -> mc.setScreen(new SelectWorldScreen(this));
            case "NewGame" -> CreateWorldScreen.openFresh(mc, this);
            case "Gallery" -> {
                Screen next = mc.options.skipMultiplayerWarning
                        ? new JoinMultiplayerScreen(this)
                        : new SafetyScreen(this);
                mc.setScreen(next);
            }
            case "Options" -> mc.setScreen(new OptionsScreen(this, mc.options));
            case "Exit" -> mc.stop();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.tickables.forEach(Tickable::tick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int screenWidth = this.width;
        int screenHeight = this.height;
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

        // Logo 随语言切换动态更新（屏幕实例可能被复用，不在构造时锁定）
        logoLayer.setTexture(isChineseLocale()
                ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA);

        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        guiGraphics.enableScissor(currentX, currentY, currentWidth + currentX, currentHeight + currentY);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        renderBackgroundCrop(guiGraphics);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (int i = 1; i < this.renderables.size(); i++) {
            Renderable renderable = this.renderables.get(i);
            renderable.render(guiGraphics, mouseX, mouseY, delta);
        }
        RenderSystem.disableBlend();
        guiGraphics.disableScissor();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float uiAlpha = computeUiAlpha();
        if (uiAlpha > 0f) {
            String version = "Ver. " + SharedConstants.getCurrentVersion().getName();
            int alpha = Math.round(uiAlpha * 255f);
            int color = (alpha << 24) | 0xFFFFFF;
            int versionX = currentX + currentWidth - 60;
            int versionY = currentY + currentHeight - 24;
            guiGraphics.drawCenteredString(this.font, Component.literal(version),
                    versionX, versionY, color);
        }
    }

    /**
     * 背景图 ContentScale.Crop：从 2:1 纹理（4096×2048）中裁剪 16:9 区域，
     * 填满 2560×1440 虚拟画布。U 和 V 等比例缩放，保证采样区域始终 16:9。
     */
    private void renderBackgroundCrop(GuiGraphics guiGraphics) {
        float zoomScale = backgroundLayer.getScale();
        float alpha = backgroundLayer.getAlpha();

        // 背景纹理 4096×2048（2:1），目标 16:9。
        // 基准 (zoom=1)：U 采样 88.89%（= 16/9 / 2），V 采样 100%（全高）。
        // 放大 (zoom>1)：U 和 V 等比例缩小，保证采样区域始终是 16:9。
        final float BASE_U = 2048f * (16f / 9f) / 4096f; // ≈0.8889
        final float BASE_V = 1.0f;
        float uFrac = BASE_U / zoomScale;
        float vFrac = BASE_V / zoomScale;
        float texU0 = (1f - uFrac) / 2f;
        float texV0 = (1f - vFrac) / 2f;
        float texU1 = texU0 + uFrac;
        float texV1 = texV0 + vFrac;

        RenderSystem.setShaderTexture(0, TextureConst.background());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        float px = VIRTUAL_SCREEN.toPracticalX(0);
        float py = VIRTUAL_SCREEN.toPracticalY(0);
        float pw = VIRTUAL_SCREEN.toPracticalWidth(2560);
        float ph = VIRTUAL_SCREEN.toPracticalHeight(1440);

        PoseStack pose = guiGraphics.pose();
        Matrix4f matrix = pose.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, px, py, 0).setUv(texU0, texV0);
        buffer.addVertex(matrix, px, py + ph, 0).setUv(texU0, texV1);
        buffer.addVertex(matrix, px + pw, py + ph, 0).setUv(texU1, texV1);
        buffer.addVertex(matrix, px + pw, py, 0).setUv(texU1, texV0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private float computeUiAlpha() {
        long now = System.currentTimeMillis();
        long elapsed = now - getStartTime();
        if (elapsed < UI_DELAY) {
            return 0f;
        }
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
    protected void rebuildWidgets() {
    }
}