package com.img.gui;

import com.img.function.AnimationFunction;
import com.paulzzh.yuzu.gui.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * 单个图层：一张完整 PNG + 位置 + 缩放 + alpha + 可选的缓动动画。
 * 坐标通过 VirtualScreen 从虚拟空间转换到实际屏幕空间。
 * 来自 YuZuUI 的 Layer。
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

    public Layer() {
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
    }

    public void render() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        RenderUtils.blit(
                virtualScreen,
                x, y,
                width * scale, height * scale
        );
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public VirtualScreen getVirtualScreen() {
        return virtualScreen;
    }

    public void setVirtualScreen(VirtualScreen virtualScreen) {
        this.virtualScreen = virtualScreen;
    }

    /**
     * 动画相关
     */
    private Long duration;
    private Long startTime = null;
    private Long delay;
    private AnimationFunction<Float> xFunction;
    private AnimationFunction<Float> yFunction;
    private AnimationFunction<Float> alphaFunction;
    private AnimationFunction<Float> scaleFunction;

    public void tick() {
        if (delay == null || duration == 0) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (startTime == null) {
            startTime = currentTime;
        } else {
            float t = currentTime - startTime;
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

    public void animateTo(float targetX, float targetY, float targetAlpha, float targetScale, long durationMs) {
        // Simple convenience: animate all properties to target values over duration
        // Uses linear interpolation (no easing)
        long start = System.currentTimeMillis();
        float startX = this.x, startY = this.y;
        float startAlpha = this.alpha, startScale = this.scale;

        // Override tick behavior for this animation
        this.duration = durationMs;
        this.delay = 0L;
        this.startTime = start;
        this.xFunction = (t, now) -> startX + (targetX - startX) * t;
        this.yFunction = (t, now) -> startY + (targetY - startY) * t;
        this.alphaFunction = (t, now) -> startAlpha + (targetAlpha - startAlpha) * t;
        this.scaleFunction = (t, now) -> startScale + (targetScale - startScale) * t;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public AnimationFunction<Float> getXFunction() {
        return xFunction;
    }

    public void setXFunction(AnimationFunction<Float> xFunction) {
        this.xFunction = xFunction;
    }

    public AnimationFunction<Float> getYFunction() {
        return yFunction;
    }

    public void setYFunction(AnimationFunction<Float> yFunction) {
        this.yFunction = yFunction;
    }

    public AnimationFunction<Float> getAlphaFunction() {
        return alphaFunction;
    }

    public void setAlphaFunction(AnimationFunction<Float> alphaFunction) {
        this.alphaFunction = alphaFunction;
    }

    public AnimationFunction<Float> getScaleFunction() {
        return scaleFunction;
    }

    public void setScaleFunction(AnimationFunction<Float> scaleFunction) {
        this.scaleFunction = scaleFunction;
    }
}
