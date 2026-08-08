package me.shiiyuko.manosaba.gui.screen;

import me.shiiyuko.manosaba.Manosaba;
import me.shiiyuko.manosaba.constant.TextureConst;
import me.shiiyuko.manosaba.gui.Layer;
import me.shiiyuko.manosaba.gui.TitleScreenButton;
import me.shiiyuko.manosaba.gui.VirtualScreen;
import me.shiiyuko.manosaba.init.ManosabaSounds;
import me.shiiyuko.manosaba.mixin.ScreenMixin;
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
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelStorageException;

import java.util.ArrayList;
import java.util.List;

/**
 * Manosaba 标题画面。布局还原自原游戏（魔法少女ノ魔女裁判）TitleUI.prefab：
 * <ul>
 *   <li>设计空间 2560×1440（原游戏 CanvasScaler ReferenceResolution）；</li>
 *   <li>背景图 ContentScale.Crop（2:1→16:9），1.05→1.0 呼吸动画；</li>
 *   <li>TitleOverlay 2560×1440 全屏画框 1:1 填充，延迟淡入；</li>
 *   <li>TitleLogo 位于原 anchoredPosition=(747,381)，原生尺寸；</li>
 *   <li>5 个按钮使用原游戏 anchoredPosition + 160px 左移（避免 Exit 被 scissor 裁剪）；</li>
 *   <li>版本号右下角；点击 Exit 弹出二级确认菜单（ExitDialog）。</li>
 * </ul>
 * 动画由 {@link Layer#tick()} 和 {@link TitleScreenButton#tick()} 驱动，
 * 在 {@link #tick()} 中统一推进。
 * <p>
 * 1.21.10 版：渲染全部走 {@link RenderUtils#blit}/{@link RenderUtils#blitCrop}
 * （GUI_TEXTURED pipeline + ARGB alpha），入场模糊已按用户决定跳过。
 */
public class ManosabaTitleScreen extends TitleScreen {

    private static final VirtualScreen VIRTUAL_SCREEN = new VirtualScreen(2560, 1440);

    // 纹理尺寸（原游戏设计空间 2560×1440，素材以原生尺寸直接放置）
    private static final int LOGO_W = 1039;
    private static final int LOGO_H = 622;
    private static final int OVERLAY_W = 2560;
    private static final int OVERLAY_H = 1440;
    private static final int BG_W = 4096;
    private static final int BG_H = 2048;

    // 动画时序（毫秒），对齐原游戏 System_Title.nani：
    // @animate Stills Scale:1.0 easing:EaseOutQuad time:2.7（缩放 2700ms）
    // @back Overlay Transparent time:1.8（全屏黑幕淡出 1800ms）
    private static final long BG_DURATION = 2700L;
    private static final long UI_DELAY = BG_DURATION + 250L;
    private static final long UI_DURATION = 500L;
    private static final long BLACK_FADE_MS = 1800L;

    private final List<Tickable> tickables = new ArrayList<>();
    private final ExitDialog exitDialog = new ExitDialog(VIRTUAL_SCREEN);

    private Layer backgroundLayer;
    private Layer overlayLayer;
    private Layer logoLayer;

    // 标题 BGM 由本屏自行管理（MusicManager 在标题屏被抑制）：
    // 对齐原游戏 System_Title.nani —— @bgm 在黑幕淡出开始时就播放
    private boolean musicStarted = false;
    private SoundInstance titleMusic;

    // 主界面按钮（用于对话框打开时禁用悬停高亮）
    private final List<TitleScreenButton> titleButtons = new ArrayList<>();

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
        // 背景：1.05×→1.0× 缩放（原游戏 Scale:{default*1.05}→1.0），EaseOutQuad，2700ms。
        // 注意：任何时候可见区域都是 2:1 图的裁切（1.0× = 88.89% 宽），非完整图。
        backgroundLayer = new Layer(TextureConst.background(),
                0, 0, 2560, 1440, 1.05f, 1f, VIRTUAL_SCREEN);
        backgroundLayer.setDelay(0L);
        backgroundLayer.setDuration(BG_DURATION);
        backgroundLayer.setScaleFunction((t, now) ->
                1.0f + 0.05f * (1f - t) * (1f - t));

        // TitleOverlay：全屏画框，纹理 2560×1440，1:1 填充虚拟画布。
        overlayLayer = new Layer(TextureConst.TITLE_OVERLAY,
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
        // 转换为 (Y-down, 原点左上): X 不变，Y = 1440 − unityY。
        // 无存档时 LoadGame 进入锁定态（原游戏 Button_LoadGame 的 Locked 状态）。
        boolean loadLocked;
        try {
            loadLocked = Minecraft.getInstance().getLevelSource()
                    .findLevelCandidates().isEmpty();
        } catch (LevelStorageException e) {
            // 存档目录异常时保守锁定
            loadLocked = true;
        }
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
            boolean locked = "LoadGame".equals(name) && loadLocked;
            if (locked) {
                // Locked 态：双态同为锁定纹理 → 无悬停高亮
                normal = TextureConst.BUTTON_LOAD_GAME_LOCKED;
                highlighted = TextureConst.BUTTON_LOAD_GAME_LOCKED;
            }
            TitleScreenButton button = new TitleScreenButton(
                    x, y, widths[i], heights[i],
                    normal, highlighted, VIRTUAL_SCREEN, 0f);
            button.setDelay(UI_DELAY);
            button.setDuration(UI_DURATION);
            button.setAlphaFunction((t, now) -> t);
            button.setCollisionSize(colW[i], colH[i]);
            if (!locked) {
                button.setClickSound(clickSoundFor(name));
                button.setLabelTexture(labelTexture(name), labelW[i], labelH[i], labelOx[i], labelOy[i]);
                button.setOnClick(b -> onButtonClick(name));
            }

            addChild(button);
            addWidget(button);
            titleButtons.add(button);
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
            case "LoadGame" -> ManosabaSounds.SFX_SYSTEM_LOADDATA.value();
            case "NewGame" -> ManosabaSounds.SFX_SYSTEM_STARTGAME.value();
            default -> ManosabaSounds.BUTTON_CLICK_SUBMIT.value();
        };
    }

    private void onButtonClick(String name) {
        Minecraft mc = this.minecraft;
        switch (name) {
            case "LoadGame" -> mc.setScreen(new SelectWorldScreen(this));
            // onClose 回调在 ESC/取消时经 popScreen() 执行：必须返回主菜单。
            // 原版 TitleScreen 传的是 setScreen(this)（method_73413），此处照搬。
            case "NewGame" -> CreateWorldScreen.openFresh(mc, () -> mc.setScreen(this));
            case "Gallery" -> {
                Screen next = mc.options.skipMultiplayerWarning
                        ? new JoinMultiplayerScreen(this)
                        : new SafetyScreen(this);
                mc.setScreen(next);
            }
            case "Options" -> mc.setScreen(new OptionsScreen(this, mc.options));
            case "Exit" -> exitDialog.open();
        }
    }

    @Override
    public void tick() {
        super.tick();
        // 加载完成时 setScreen(标题屏) 在 LoadingOverlay 淡出前即被调用
        // （原版 onFinish 先 setScreen、后 setOverlay(null)）。此窗口内暂不启动
        // 标题动画/BGM：等 overlay 移除、标题真正可见时再开始 —— 对应原游戏
        // Boot.unity logo 静音 → System_Title BGM 起播，也避免入场动画/黑幕
        // 淡出在加载 logo 背后空转（logo 已由 LoadingOverlayMixin 播放一次）。
        if (Minecraft.getInstance().getOverlay() != null) {
            return;
        }
        // 标题 BGM：首个 tick 即播放（黑幕开始淡出时音乐响起，原游戏 @bgm 时机）。
        // 子界面（Options/世界选择等）切换不停止音乐（原游戏 BGM 持续）；
        // 进入世界由 ManosabaClient 的 JOIN 事件停止。
        if (!musicStarted) {
            musicStarted = true;
            this.titleMusic = SimpleSoundInstance.forMusic(ManosabaSounds.TITLE_MUSIC.value(), 1.0f);
            Minecraft.getInstance().getSoundManager().play(this.titleMusic);
            Manosaba.titleMusicPlaying = true;
        } else if (this.titleMusic != null
                && !this.exitDialog.isQuitting() // 退出淡出期间 BGM 保持停止
                && Minecraft.getInstance().getOverlay() == null
                && !Minecraft.getInstance().getSoundManager().isActive(this.titleMusic)) {
            // 资源重载（切换语言等）会重建 SoundManager 中断 BGM：
            // 重载完成后（overlay 关闭）检测到音乐未在播放则从头重播
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

    /** 对话框打开时优先消费点击，屏蔽底层按钮。 */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean b) {
        if (exitDialog.isOpen()) {
            return exitDialog.mouseClicked(event, b);
        }
        return super.mouseClicked(event, b);
    }

    /** 对话框打开时拦截 ESC（关闭对话框而非其他行为）。 */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (exitDialog.isOpen()) {
            return exitDialog.keyPressed(event);
        }
        return super.keyPressed(event);
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

        // Logo 随语言切换动态更新（屏幕实例可能被复用，不在构造时锁定）
        logoLayer.setTexture(isChineseLocale()
                ? TextureConst.TITLE_LOGO_ZH : TextureConst.TITLE_LOGO_JA);

        // 背景：先填黑，再在 16:9 区域内做 ContentScale.Crop 渲染。
        guiGraphics.fill(0, 0, screenWidth, screenHeight, 0xFF000000);

        guiGraphics.enableScissor(currentX, currentY, currentWidth + currentX, currentHeight + currentY);

        // 背景层单独渲染（用纹理原始宽高做 Crop，而非 VirtualScreen 的 FillBounds）。
        renderBackgroundCrop(guiGraphics);

        // 其余 Layer（overlay、logo）和按钮走标准 Renderable 流程。
        // 退出确认后主界面 UI 随黑幕同步淡出（原游戏 @HideUI TitleUI time:1.2）。
        float uiFade = 1f - exitDialog.getQuitFade();
        List<Renderable> renderables = ((ScreenMixin) (Object) this).getRenderables();
        for (int i = 1; i < renderables.size(); i++) {
            Renderable renderable = renderables.get(i);
            if (uiFade < 1f) {
                if (renderable instanceof Layer layer) {
                    layer.setAlpha(layer.getAlpha() * uiFade);
                } else if (renderable instanceof TitleScreenButton button) {
                    button.setAlpha(button.getAlpha() * uiFade);
                }
            }
            renderable.render(guiGraphics, mouseX, mouseY, delta);
        }
        guiGraphics.disableScissor();

        // 版本号：右下角，alpha 跟随 UI 淡入。
        float uiAlpha = computeUiAlpha() * uiFade;
        if (uiAlpha > 0f) {
            String version = "Ver. " + SharedConstants.getCurrentVersion().name();
            int alpha = Math.round(uiAlpha * 255f);
            int color = (alpha << 24) | 0xFFFFFF;
            int versionX = currentX + currentWidth - 60;
            int versionY = currentY + currentHeight - 24;
            guiGraphics.drawCenteredString(this.font, Component.literal(version),
                    versionX, versionY, color);
        }

        // 全屏黑幕淡出（原游戏 @back Overlay Transparent time:1.8，覆盖整个窗口）。
        long now = System.currentTimeMillis();
        long elapsed = now - getStartTime();
        float blackAlpha = 1f - Mth.clamp((float) elapsed / BLACK_FADE_MS, 0f, 1f);
        if (blackAlpha > 0f) {
            int a = Math.round(blackAlpha * 255f);
            guiGraphics.fill(0, 0, screenWidth, screenHeight, (a << 24) | 0x000000);
        }

        // 退出确认对话框（置于最上层）。
        this.exitDialog.render(guiGraphics, mouseX, mouseY, delta);

        // 退出黑幕淡入：黑色纹理 + alpha，置于帧末，最终全黑覆盖一切后退出
        // （原游戏 # QuitGame @back SolidColor time:1.2）。
        float quitFade = this.exitDialog.getQuitFade();
        if (quitFade > 0f) {
            RenderUtils.blit(guiGraphics, TextureConst.BLACK,
                    0, 0, screenWidth, screenHeight, quitFade);
        }
    }

    /**
     * 背景图 ContentScale.Crop：从 2:1 纹理（4096×2048）中裁剪 16:9 区域，
     * 填满 2560×1440 虚拟画布。缩放从 1.05×→1.0×（EaseOutQuad），
     * 通过缩小 UV 采样范围实现放大呼吸动画。
     */
    private void renderBackgroundCrop(GuiGraphics guiGraphics) {
        float px = VIRTUAL_SCREEN.toPracticalX(0);
        float py = VIRTUAL_SCREEN.toPracticalY(0);
        float pw = VIRTUAL_SCREEN.toPracticalWidth(2560);
        float ph = VIRTUAL_SCREEN.toPracticalHeight(1440);
        drawBackgroundQuad(guiGraphics, px, py, pw, ph,
                backgroundLayer.getScale(), backgroundLayer.getAlpha());
    }

    /** 以指定矩形绘制背景 quad（UV 裁切数学，走 13 参 blitCrop）。 */
    private void drawBackgroundQuad(GuiGraphics guiGraphics, float px, float py,
                                    float pw, float ph, float zoomScale, float alpha) {
        // 背景纹理 4096×2048（2:1），目标 16:9。
        // 基准 (zoom=1)：U 采样 88.89%（= 16/9 / 2），V 采样 100%（全高）。
        // 放大 (zoom>1)：U 和 V 等比例缩小，保证采样区域始终是 16:9。
        final float BASE_U = 2048f * (16f / 9f) / 4096f; // ≈0.8889
        final float BASE_V = 1.0f;
        float uFrac = BASE_U / zoomScale;
        float vFrac = BASE_V / zoomScale;
        float texU0 = (1f - uFrac) / 2f;
        float texV0 = (1f - vFrac) / 2f;
        // 归一化 UV → 纹理像素坐标
        RenderUtils.blitCrop(guiGraphics, TextureConst.background(), px, py, pw, ph,
                texU0 * BG_W, texV0 * BG_H,
                Math.round(uFrac * BG_W), Math.round(vFrac * BG_H),
                BG_W, BG_H, alpha);
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
