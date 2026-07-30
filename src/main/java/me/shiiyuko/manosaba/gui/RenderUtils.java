package me.shiiyuko.manosaba.gui;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * 底层纹理绘制工具：画一张完整纹理（UV 0,0 到 1,1）到指定矩形区域。
 * 1.7.10 使用 Tessellator 直接绘制。
 */
public final class RenderUtils {

    private RenderUtils() {
    }

    public static void blit(float x, float y, float width, float height) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, 0, 1);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1, 1);
        tessellator.addVertexWithUV(x + width, y, 0, 1, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();
    }
}
