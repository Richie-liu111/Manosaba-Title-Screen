package com.paulzzh.yuzu.gui;

import com.img.gui.VirtualScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * 底层纹理绘制工具：画一张完整纹理（UV 从 0,0 到 1,1）到指定矩形区域。
 * 来自 YuZuUI 的 RenderUtils，配合独立 PNG 使用，1.12.2 版本。
 */
public final class RenderUtils {

    private RenderUtils() {
    }

    public static void blit(float x, float y, float width, float height) {
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(x, y + height, 0).tex(0, 1).endVertex();
        bufferBuilder.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        bufferBuilder.pos(x + width, y, 0).tex(1, 0).endVertex();
        bufferBuilder.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }

    public static void blit(VirtualScreen virtualScreen, float vX, float vY, float vWidth, float vHeight) {
        blit(
                virtualScreen.toPracticalX(vX),
                virtualScreen.toPracticalY(vY),
                virtualScreen.toPracticalWidth(vWidth),
                virtualScreen.toPracticalHeight(vHeight)
        );
    }

    /**
     * Creates a scissor test using minecraft screen coordinates instead of pixel coordinates.
     */
    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        int scale = new ScaledResolution(mc).getScaleFactor();

        boxWidth = Math.max(0, boxWidth);
        boxHeight = Math.max(0, boxHeight);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                screenX * scale,
                mc.displayHeight - (screenY + boxHeight) * scale,
                boxWidth * scale,
                boxHeight * scale
        );
    }

    public static void endScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
