package me.shiiyuko.manosaba.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shiiyuko.manosaba.constant.TextureConst;
import me.shiiyuko.manosaba.gui.Layer;
import me.shiiyuko.manosaba.gui.TitleScreenButton;
import me.shiiyuko.manosaba.gui.VirtualScreen;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.OptionsScreen;
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

import java.util.List;

/**
 * Manosaba 标题画面。基于 YuZuUI 的 SenrenBankaTitleScreen 模式：
 * <ul>
 *   <li>设计空间 1920×1080，通过 {@link VirtualScreen} letterbox 到实际屏幕；</li>
 *   <li>背景图 ContentScale.Crop（填满屏幕，裁切溢出），1.1→1.0 缩放动画；</li>
 *   <li>TitleOverlay 全屏画框，延迟淡入；</li>
 *   <li>TitleLogo 右上角，0.65× 缩放，延迟淡入；</li>
 *   <li>5 个按钮横排于左下角，交替 Y 偏移（±20）形成 zigzag；</li>
 *   <li>版本号右下角；点击 Exit 直接退出游戏。</li>
 * </ul>
 * 动画由 {@link Layer#tick()} 和 {@link TitleScreenButton#tick()} 驱动，
 * 在 {@link #tick()} 中统一推进。
 */
public class ManosabaTitleScreen extends TitleScreen {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(1920, 1080);

    // 设计空间与纹理尺寸
    private static final float BUTTON_SCALE = 0.6f;
    private static final float LOGO_SCALE = 0.65f;
    private static final int LOGO_W = 1039;
    private static final int LOGO_H = 622;
    private static final int OVERLAY_W = 2560;
    private static final int OVERLAY_H = 1440;
    private static final int BG_W = 4096;
    private static final int BG_H = 2048;

    // 动画时序（毫秒），匹配原 Fabric 项目
    private static final long BG_DURATION = 2500L;
    private static final long UI_DELAY = BG_DURATION + 250L;
    private static final long UI_DURATION = 500L;

    private final List<Tickable> tickables = Lists.newArrayList();

    private Layer backgroundLayer;
    private Layer overlayLayer;
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
        backgroundLayer = new Layer(TextureConst.BACKGROUND,
                0, 0, 1920, 1080, 1.1f, 1f, VIRTUAL_SCREEN);
        backgroundLayer.setDelay(0L);
        backgroundLayer.setDuration(BG_DURATION);
        backgroundLayer.setScaleFunction((t, now) ->
                1.1f + (1.0f - 1.1f) * t);

        // TitleOverlay：全屏画框，延迟 2750ms 淡入 500ms。
        // 纹理 2560×1440，拉伸到 1920×1080（FillBounds）。
        overlayLayer = new Layer(TextureConst.TITLE_OVERLAY,
                0, 0, 1920, 1080, 1f, 0f, VIRTUAL_SCREEN);
        overlayLayer.setDelay(UI_DELAY);
        overlayLayer.setDuration(UI_DURATION);
        overlayLayer.setAlphaFunction((t, now) -> t);

        // TitleLogo：右上角，0.65× 缩放。
        float logoDisplayW = LOGO_W * LOGO_SCALE;
        float logoDisplayH = LOGO_H * LOGO_SCALE;
        logoLayer = new Layer(TextureConst.TITLE_LOGO_JA,
                1920 - logoDisplayW - 24, 24,
                LOGO_W, LOGO_H, LOGO_SCALE, 0f, VIRTUAL_SCREEN);
        logoLayer.setDelay(UI_DELAY);
        logoLayer.setDuration(UI_DURATION);
        logoLayer.setAlphaFunction((t, now) -> t);

        addChild(backgroundLayer);
        addChild(overlayLayer);
        addChild(logoLayer);
    }

    private void initButtons() {
        // 按钮横排于左下角，交替 Y 偏移（+20/-20）形成 zigzag。
        // 原始尺寸 × BUTTON_SCALE 得到显示尺寸。
        String[] names = {"LoadGame", "NewGame", "Gallery", "Options", "Exit"};
        int[] yOffsets = {20, -20, 20, -20, 20};
        int[] widths = {498, 437, 362, 338, 277};
        int[] heights = {323, 301, 251, 230, 190};

        float baseY = 1080 - 24;
        float cursorX = 24;

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            float displayW = widths[i] * BUTTON_SCALE;
            float displayH = heights[i] * BUTTON_SCALE;
            float y = baseY - displayH + yOffsets[i];

            ResourceLocation normal = buttonTexture(name, false);
            ResourceLocation highlighted = buttonTexture(name, true);
            TitleScreenButton button = new TitleScreenButton(
                    cursorX, y, displayW, displayH,
                    normal, highlighted, VIRTUAL_SCREEN, 0f);
            button.setDelay(UI_DELAY);
            button.setDuration(UI_DURATION);
            button.setAlphaFunction((t, now) -> t);
            button.setClickSound(clickSoundFor(name));
            button.setOnClick(b -> onButtonClick(name));

            addChild(button);
            addWidget(button);
            cursorX += displayW;
        }
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

    private static SoundEvent clickSoundFor(String name) {
        return "NewGame".equals(name)
                ? ManosabaSounds.BUTTON_CLICK_START_GAME.get()
                : ManosabaSounds.BUTTON_CLICK_SUBMIT.get();
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
        // 计算 16:9 letterbox，设置 VirtualScreen 的实际映射区域。
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

        // 背景：先填黑，再在 16:9 区域内做 ContentScale.Crop 渲染。
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        guiGraphics.enableScissor(currentX, currentY, currentWidth + currentX, currentHeight + currentY);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // 背景层单独渲染（用纹理原始宽高做 Crop，而非 VirtualScreen 的 FillBounds）。
        renderBackgroundCrop(guiGraphics);

        // 其余 Layer（overlay、logo）和按钮走标准 Renderable 流程。
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (int i = 1; i < this.renderables.size(); i++) {
            Renderable renderable = this.renderables.get(i);
            renderable.render(guiGraphics, mouseX, mouseY, delta);
        }
        RenderSystem.disableBlend();
        guiGraphics.disableScissor();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 版本号：右下角，alpha 跟随 UI 淡入。
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
     * 背景图 ContentScale.Crop：填满 16:9 虚拟区域，按纹理原始宽高比裁切。
     * 缩放从 1.1× 动画到 1.0×，通过采样更小的中央区域再放大实现。
     */
    private void renderBackgroundCrop(GuiGraphics guiGraphics) {
        float scale = backgroundLayer.getScale();
        float alpha = backgroundLayer.getAlpha();

        // 虚拟区域 1920×1080，纹理 4096×2048（2:1），虚拟区域也是 16:9。
        // 直接把虚拟区域映射到纹理中央的 1920×1080 对应区域。
        // zoom > 1 时采样更小的中央区域（scale=1.1 采 1745×982）。
        float cropVirtualW = 1920 / scale;
        float cropVirtualH = 1080 / scale;
        // 虚拟空间裁切区域 → 纹理空间坐标
        float texU0 = (1920 - cropVirtualW) / 2f / 1920f;
        float texV0 = (1080 - cropVirtualH) / 2f / 1080f;
        float texU1 = texU0 + cropVirtualW / 1920f;
        float texV1 = texV0 + cropVirtualH / 1080f;

        RenderSystem.setShaderTexture(0, TextureConst.BACKGROUND);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        float px = VIRTUAL_SCREEN.toPracticalX(0);
        float py = VIRTUAL_SCREEN.toPracticalY(0);
        float pw = VIRTUAL_SCREEN.toPracticalWidth(1920);
        float ph = VIRTUAL_SCREEN.toPracticalHeight(1080);

        com.mojang.blaze3d.vertex.PoseStack pose = guiGraphics.pose();
        org.joml.Matrix4f matrix = pose.last().pose();
        com.mojang.blaze3d.vertex.BufferBuilder buffer =
                com.mojang.blaze3d.vertex.Tesselator.getInstance().getBuilder();
        buffer.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, px, py, 0).uv(texU0, texV0).endVertex();
        buffer.vertex(matrix, px, py + ph, 0).uv(texU0, texV1).endVertex();
        buffer.vertex(matrix, px + pw, py + ph, 0).uv(texU1, texV1).endVertex();
        buffer.vertex(matrix, px + pw, py, 0).uv(texU1, texV0).endVertex();
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buffer.end());
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
        // Layer/按钮的 startTime 由 tick() 首次调用时设置为当前时间，取背景层即可。
        Long st = backgroundLayer.getStartTime();
        return st != null ? st : System.currentTimeMillis();
    }

    public <T extends Renderable & Tickable> void addChild(T child) {
        this.tickables.add(child);
        this.addRenderableOnly(child);
    }

    @Override
    protected void rebuildWidgets() {
        // 重写以防止 clearWidgets 清除组件（参考 YuZuUI）。
    }
}