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
import me.shiiyuko.manosaba.utils.RenderUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

/**
 * 退出确认对话框（2 级菜单）。
 *
 * <p><b>背景结构</b>（2560×1440 设计坐标，Y-down）：
 * <ol>
 *   <li>全屏半透明黑幕压暗主界面背景（原游戏 Underlay，α≈0.65）；</li>
 *   <li>亮色条带 = Frame 区域（y 370..1070，dialogbase 纹理行 370..1070），完全不透明；</li>
 *   <li>上下装饰暗条（完整纹理不规则剪影）。</li>
 * </ol>
 *
 * <p><b>退出时序</b>（原游戏 # QuitGame）：停 BGM → 提交音效 → 关闭对话框 150ms
 * → 黑幕 1.2s 淡入 → 退出游戏。
 */
public class ExitDialog {

    private enum State { CLOSED, FADE_IN, OPEN, FADE_OUT, QUIT_FADE }

    private static final Logger LOG = LogManager.getLogger("manosaba:ExitDialog");

    private static final long FADE_MS = 150L;
    private static final long CLOSE_MS = 150L;
    private static final long QUIT_FADE_MS = 1200L;

    // 元素布局（2560×1440，Y-down）
    private static final float MSG_W_ZH = 325f;
    private static final float MSG_H_ZH = 49f;
    private static final float MSG_W_JA = 356f;
    private static final float MSG_H_JA = 38f;
    private static final float MSG_Y = 640f;
    private static final float BUTTON_W = 505f;
    private static final float BUTTON_H = 120f;
    private static final float BUTTON_Y = 740f;
    private static final float CANCEL_X = 728.5f;
    private static final float END_X = 1326.5f;

    private static final float BAND_Y = 370f;
    private static final float BAND_H = 700f;
    private static final float BAND_UV_V0 = BAND_Y / 1440f;
    private static final float BAND_UV_V1 = (BAND_Y + BAND_H) / 1440f;
    private static final int CURTAIN_ALPHA = Math.round(0.65f * 255f);

    private static final float TOP_FRAME_Y = 370f;
    private static final float BOTTOM_FRAME_Y = 641f;

    private final VirtualScreen vs;
    private final Layer topLayer;
    private final Layer bottomLayer;
    private final Layer messageLayer;
    private final TitleScreenButton cancelButton;
    private final TitleScreenButton endButton;

    private State state = State.CLOSED;
    private long stateStart = 0L;
    private float alpha = 0f;
    private float quitAlpha = 0f;

    public ExitDialog(VirtualScreen vs) {
        this.vs = vs;
        boolean zh = isChineseLocale();
        topLayer = new Layer(TextureConst.TOP_FRAME, 0, TOP_FRAME_Y, 2560, 429, 1f, 1f, vs);
        bottomLayer = new Layer(TextureConst.BOTTOM_FRAME, 0, BOTTOM_FRAME_Y, 2560, 429, 1f, 1f, vs);
        float msgW = zh ? MSG_W_ZH : MSG_W_JA;
        float msgH = zh ? MSG_H_ZH : MSG_H_JA;
        messageLayer = new Layer(zh ? TextureConst.DIALOG_MESSAGE_ZH : TextureConst.DIALOG_MESSAGE_JA,
                1280 - msgW / 2f, MSG_Y, msgW, msgH, 1f, 1f, vs);

        cancelButton = new TitleScreenButton(CANCEL_X, BUTTON_Y, BUTTON_W, BUTTON_H,
                TextureConst.dialogButton("cancel", false, zh),
                TextureConst.dialogButton("cancel", true, zh), vs, 0f);
        endButton = new TitleScreenButton(END_X, BUTTON_Y, BUTTON_W, BUTTON_H,
                TextureConst.dialogButton("end", false, zh),
                TextureConst.dialogButton("end", true, zh), vs, 0f);
        cancelButton.setClickSound(ManosabaSounds.SFX_SYSTEM_CANCEL.get());
        endButton.setClickSound(null);
        cancelButton.setOnClick(b -> close());
        endButton.setOnClick(b -> quit());
        applyAlpha();
    }

    private static boolean isChineseLocale() {
        String code = Minecraft.getInstance().options.languageCode;
        return code != null && code.startsWith("zh");
    }

    public boolean isOpen() { return state != State.CLOSED; }
    public float getQuitFade() { return state == State.QUIT_FADE ? quitAlpha : 0f; }
    public boolean isQuitting() { return state == State.QUIT_FADE; }

    public void open() {
        state = State.FADE_IN;
        stateStart = Util.getEpochMillis();
        alpha = 0f;
        quitAlpha = 0f;
        applyAlpha();
    }

    public void close() {
        if (state == State.OPEN || state == State.FADE_IN) {
            state = State.FADE_OUT;
            stateStart = Util.getEpochMillis();
        }
    }

    private void quit() {
        LOG.info("ExitDialog: quit confirmed");
        state = State.QUIT_FADE;
        stateStart = Util.getEpochMillis();
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().stop();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(ManosabaSounds.SFX_SYSTEM_SUBMIT.get(), 1.0f, 1.0f));
    }

    public void tick() {
        if (state == State.CLOSED) return;
        long t = Util.getEpochMillis() - stateStart;
        switch (state) {
            case FADE_IN -> {
                alpha = Math.min(1f, t / (float) FADE_MS);
                if (alpha >= 1f) state = State.OPEN;
            }
            case OPEN -> {}
            case FADE_OUT -> {
                alpha = Math.max(0f, 1f - t / (float) FADE_MS);
                if (alpha <= 0f) state = State.CLOSED;
            }
            case QUIT_FADE -> {
                if (t < CLOSE_MS) {
                    alpha = Math.max(0f, 1f - t / (float) CLOSE_MS);
                } else {
                    alpha = 0f;
                    quitAlpha = Math.min(1f, (float) (t - CLOSE_MS) / QUIT_FADE_MS);
                    if (quitAlpha >= 1f) {
                        LOG.info("ExitDialog: quit fade complete, stopping game");
                        Minecraft.getInstance().stop();
                    }
                }
            }
        }
        applyAlpha();
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        if (state == State.CLOSED) return;
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // 1) 黑幕
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, CURTAIN_ALPHA / 255f);
        RenderSystem.setShaderTexture(0, TextureConst.BLACK);
        RenderUtils.blit(0, 0, g.guiWidth(), g.guiHeight(), g.pose());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 2) 亮色条带
        RenderSystem.setShaderTexture(0, TextureConst.DIALOG_BASE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        drawBandQuad(g, 0, BAND_Y, 2560, BAND_H, 0f, BAND_UV_V0, 1f, BAND_UV_V1);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 3) 装饰条 + 元素
        topLayer.render(g, mouseX, mouseY, delta);
        bottomLayer.render(g, mouseX, mouseY, delta);
        messageLayer.render(g, mouseX, mouseY, delta);
        cancelButton.render(g, mouseX, mouseY, delta);
        endButton.render(g, mouseX, mouseY, delta);
    }

    private void drawBandQuad(GuiGraphics g, float x, float y, float w, float h,
                              float u0, float v0, float u1, float v1) {
        float px = vs.toPracticalX(x), py = vs.toPracticalY(y);
        float pw = vs.toPracticalWidth(w), ph = vs.toPracticalHeight(h);
        PoseStack pose = g.pose();
        Matrix4f matrix = pose.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, px, py, 0).setUv(u0, v0);
        buffer.addVertex(matrix, px, py + ph, 0).setUv(u0, v1);
        buffer.addVertex(matrix, px + pw, py + ph, 0).setUv(u1, v1);
        buffer.addVertex(matrix, px + pw, py, 0).setUv(u1, v0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (state == State.OPEN || state == State.FADE_IN) {
            return cancelButton.mouseClicked(mouseX, mouseY, button)
                    | endButton.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((state == State.OPEN || state == State.FADE_IN) && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    private void applyAlpha() {
        topLayer.setAlpha(alpha);
        bottomLayer.setAlpha(alpha);
        messageLayer.setAlpha(alpha);
        cancelButton.setAlpha(alpha);
        endButton.setAlpha(alpha);
    }
}
