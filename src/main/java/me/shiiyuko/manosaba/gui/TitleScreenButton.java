package me.shiiyuko.manosaba.gui;

import me.shiiyuko.manosaba.function.AnimationFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

/**
 * 标题画面按钮：两张独立 PNG（Normal/Hover）+ hover 检测 + 点击回调。
 * 坐标在虚拟空间内定义，通过 VirtualScreen 转换。支持中文标签叠加。
 * 1.7.10 版本使用 Tessellator 直接绘制。
 */
public class TitleScreenButton {
    private float x;
    private float y;
    private float width;
    private float height;
    private float alpha;
    private boolean visible = true;
    private boolean isHovered = false;
    private boolean hoverable = true;

    /** 碰撞检测尺寸（默认 = 纹理尺寸），居中于渲染矩形内 */
    private float collisionW;
    private float collisionH;

    private ResourceLocation texture;
    private ResourceLocation textureHover;
    private VirtualScreen virtualScreen;
    private final Minecraft mc;

    // 中文标签
    private ResourceLocation labelTexture;
    private float labelWidth;
    private float labelHeight;
    private float labelOffsetX;
    private float labelOffsetY;

    private Consumer<TitleScreenButton> onClick;

    /** 点击音效（null = 无音效，用于锁定态按钮） */
    private ResourceLocation clickSound;

    // 动画相关
    private Long duration;
    private Long startTime = null;
    private Long delay;
    private AnimationFunction<Float> alphaFunction;

    public TitleScreenButton(float x, float y, float width, float height,
                             ResourceLocation texture, ResourceLocation textureHover,
                             VirtualScreen virtualScreen, float alpha) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
        this.textureHover = textureHover;
        this.virtualScreen = virtualScreen;
        this.alpha = alpha;
        this.collisionW = width;
        this.collisionH = height;
        this.mc = Minecraft.getMinecraft();
    }

    public void render(int mouseX, int mouseY, float delta) {
        if (!this.visible) return;

        this.isHovered = this.hoverable && this.isMouseOver(mouseX, mouseY);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        // 主按钮纹理
        float drawX = virtualScreen.toPracticalX(x);
        float drawY = virtualScreen.toPracticalY(y);
        float drawW = virtualScreen.toPracticalWidth(width);
        float drawH = virtualScreen.toPracticalHeight(height);

        mc.getTextureManager().bindTexture(isHovered ? textureHover : texture);
        RenderUtils.blit(drawX, drawY, drawW, drawH);

        // 悬停时叠加中文标签（仅简中语言时显示）
        if (isHovered && labelTexture != null
                && mc.gameSettings.language != null
                && mc.gameSettings.language.startsWith("zh")) {
            mc.getTextureManager().bindTexture(labelTexture);
            RenderUtils.blit(
                    virtualScreen.toPracticalX(x + labelOffsetX),
                    virtualScreen.toPracticalY(y + labelOffsetY),
                    virtualScreen.toPracticalWidth(labelWidth),
                    virtualScreen.toPracticalHeight(labelHeight)
            );
        }

        tick();
    }

    public void mousePressed(int mouseX, int mouseY) {
        if (this.visible && this.hoverable && isMouseOver(mouseX, mouseY)) {
            if (this.clickSound != null) {
                mc.getSoundHandler().playSound(
                        net.minecraft.client.audio.PositionedSoundRecord.func_147673_a(this.clickSound));
            }
            if (onClick != null) {
                onClick.accept(this);
            }
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        float insetX = (width - collisionW) / 2f;
        float insetY = (height - collisionH) / 2f;
        float vx = virtualScreen.toVirtualX((float) mouseX) - x - insetX;
        float vy = virtualScreen.toVirtualY((float) mouseY) - y - insetY;
        return vx >= 0 && vx < collisionW && vy >= 0 && vy < collisionH;
    }

    public void tick() {
        if (delay == null || duration == 0) return;
        long currentTime = System.currentTimeMillis();
        if (startTime == null) {
            startTime = currentTime;
        } else {
            long t = currentTime - startTime;
            if (t > delay) {
                float time = Math.min((float) (t - delay) / duration, 1);
                if (alphaFunction != null) {
                    alpha = alphaFunction.apply(time, alpha);
                }
            }
        }
    }

    // Getters and setters

    public boolean isHovered() { return isHovered; }
    public void setOnClick(Consumer<TitleScreenButton> onClick) { this.onClick = onClick; }
    /** 设置碰撞检测尺寸（居中于渲染矩形） */
    public void setCollisionSize(float w, float h) { this.collisionW = w; this.collisionH = h; }

    /** 设置是否响应悬停高亮和点击。locked=true 时可设为 false。 */
    public void setHoverable(boolean h) { this.hoverable = h; }

    /** 设置点击音效的 ResourceLocation。null = 无音效。 */
    public void setClickSound(ResourceLocation sound) { this.clickSound = sound; }

    public void setLabelTexture(ResourceLocation texture, float w, float h, float offsetX, float offsetY) {
        this.labelTexture = texture;
        this.labelWidth = w;
        this.labelHeight = h;
        this.labelOffsetX = offsetX;
        this.labelOffsetY = offsetY;
    }

    public void setAlpha(float a) { this.alpha = a; }
    public float getAlpha() { return alpha; }
    public void setAlphaFunction(AnimationFunction<Float> f) { this.alphaFunction = f; }
    public void setDelay(Long d) { this.delay = d; }
    public void setDuration(Long d) { this.duration = d; }
}
