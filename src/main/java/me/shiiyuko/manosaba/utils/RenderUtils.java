package me.shiiyuko.manosaba.utils;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

/**
 * 底层纹理绘制工具：画一张完整纹理（UV 从 0,0 到 1,1）到指定矩形区域。
 * 适配 1.21.1 的 BufferBuilder API（setUv、buildOrThrow、Tesselator.begin）。
 */
public final class RenderUtils {

    private RenderUtils() {
    }

    public static void blit(float x, float y, float width, float height, PoseStack poseStack) {
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix4f, x, y, 0).setUv(0, 0);
        bufferBuilder.addVertex(matrix4f, x, y + height, 0).setUv(0, 1);
        bufferBuilder.addVertex(matrix4f, x + width, y + height, 0).setUv(1, 1);
        bufferBuilder.addVertex(matrix4f, x + width, y, 0).setUv(1, 0);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}