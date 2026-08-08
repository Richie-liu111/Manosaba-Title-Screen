package me.shiiyuko.manosaba.utils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

/**
 * 底层纹理绘制工具（1.21.10 blaze3d 重写版）。
 * <p>
 * 1.21.10 移除了 {@code RenderSystem.setShader/setShaderColor/setShaderTexture} 与
 * {@code BufferUploader} 等立即模式 API，全部改走 {@link GuiGraphics#blit} 流水线：
 * alpha 通过 blit 的 ARGB color 参数实现，UV 裁切通过 13 参数重载实现。
 * 本类封装两个基元：
 * <ul>
 *   <li>{@link #blit} —— 整张纹理 1:1 绘制 + alpha；</li>
 *   <li>{@link #blitCrop} —— 从纹理中裁切 srcW×srcH 区域缩放到 w×h + alpha。</li>
 * </ul>
 * 混色由 GUI_TEXTURED pipeline 自带，无需显式 enableBlend。
 */
public final class RenderUtils {

    private RenderUtils() {
    }

    /** 把 alpha (0..1) 编码进 ARGB int（不透明色，仅调整透明度通道）。 */
    public static int color(float alpha) {
        return ((int) (alpha * 255f) << 24) | 0xFFFFFF;
    }

    /**
     * 整张纹理 1:1 绘制到 (x,y)-(x+w,y+h)，带 alpha。
     */
    public static void blit(GuiGraphics g, ResourceLocation tex, float x, float y, float w, float h, float alpha) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex, (int) x, (int) y, 0f, 0f, (int) w, (int) h, (int) w, (int) h, color(alpha));
    }

    /**
     * 从纹理 (srcU,srcV) 起裁切 srcW×srcH 区域，缩放绘制到 (x,y)-(x+w,y+h)，带 alpha。
     * texW/texH 为纹理完整尺寸（UV 归一化用）。
     */
    public static void blitCrop(GuiGraphics g, ResourceLocation tex, float x, float y, float w, float h,
                                float srcU, float srcV, int srcW, int srcH, int texW, int texH, float alpha) {
        g.blit(RenderPipelines.GUI_TEXTURED, tex, (int) x, (int) y, srcU, srcV, (int) w, (int) h, srcW, srcH, texW, texH, color(alpha));
    }
}
