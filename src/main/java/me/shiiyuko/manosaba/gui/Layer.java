package me.shiiyuko.manosaba.gui;

import me.shiiyuko.manosaba.function.AnimationFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * 单个图层：一张完整 PNG + 位置 + 缩放 + alpha + 可选的缓动动画。
 * 1.7.10 版本使用 Tessellator 直接绘制，支持 x/y/alpha/scale 四维缓动。
 */
public class Layer {
    private ResourceLocation texture;
    private float x;
    private float y;
    private float width;
    private float height;
    private float scale;
    private float alpha;
    private VirtualScreen virtualScreen;
    private final Minecraft mc;

    // 动画相关
    private Long duration;
    private Long startTime = null;
    private Long delay;
    private AnimationFunction<Float> xFunction;
    private AnimationFunction<Float> yFunction;
    private AnimationFunction<Float> alphaFunction;
    private AnimationFunction<Float> scaleFunction;

    public Layer() {
        this.mc = Minecraft.getMinecraft();
    }

    public Layer(ResourceLocation texture, float x, float y, float width, float height,
                 float scale, float alpha, VirtualScreen virtualScreen) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.alpha = alpha;
        this.virtualScreen = virtualScreen;
        this.mc = Minecraft.getMinecraft();
    }

    public void render(int mouseX, int mouseY, float delta) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        mc.getTextureManager().bindTexture(texture);
        RenderUtils.blit(
                virtualScreen.toPracticalX(x),
                virtualScreen.toPracticalY(y),
                virtualScreen.toPracticalWidth(width * scale),
                virtualScreen.toPracticalHeight(height * scale)
        );
        tick();
    }

    public void tick() {
        if (delay == null || duration == 0) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (startTime == null) {
            startTime = currentTime;
        } else {
            long t = currentTime - startTime;
            if (t > delay) {
                float time = Math.min((float) (t - delay) / duration, 1);
                if (xFunction != null) {
                    x = xFunction.apply(time, x);
                }
                if (yFunction != null) {
                    y = yFunction.apply(time, y);
                }
                if (alphaFunction != null) {
                    alpha = alphaFunction.apply(time, alpha);
                }
                if (scaleFunction != null) {
                    scale = scaleFunction.apply(time, scale);
                }
            }
        }
    }

    // Getters and setters

    public ResourceLocation getTexture() { return texture; }
    public void setTexture(ResourceLocation texture) { this.texture = texture; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public void setWidth(float w) { this.width = w; }
    public float getHeight() { return height; }
    public void setHeight(float h) { this.height = h; }
    public float getScale() { return scale; }
    public void setScale(float s) { this.scale = s; }
    public float getAlpha() { return alpha; }
    public void setAlpha(float a) { this.alpha = a; }
    public VirtualScreen getVirtualScreen() { return virtualScreen; }
    public void setVirtualScreen(VirtualScreen vs) { this.virtualScreen = vs; }
    public Long getDuration() { return duration; }
    public void setDuration(Long d) { this.duration = d; }
    public Long getStartTime() { return startTime; }
    public void setStartTime(Long t) { this.startTime = t; }
    public Long getDelay() { return delay; }
    public void setDelay(Long d) { this.delay = d; }
    public AnimationFunction<Float> getXFunction() { return xFunction; }
    public void setXFunction(AnimationFunction<Float> f) { this.xFunction = f; }
    public AnimationFunction<Float> getYFunction() { return yFunction; }
    public void setYFunction(AnimationFunction<Float> f) { this.yFunction = f; }
    public AnimationFunction<Float> getAlphaFunction() { return alphaFunction; }
    public void setAlphaFunction(AnimationFunction<Float> f) { this.alphaFunction = f; }
    public AnimationFunction<Float> getScaleFunction() { return scaleFunction; }
    public void setScaleFunction(AnimationFunction<Float> f) { this.scaleFunction = f; }
}
