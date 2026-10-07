package me.shiiyuko.manosaba.gui;

/**
 * 逐帧回调接口：{@link Layer} 与 {@link TitleScreenButton} 实现它，
 * 由 {@link me.shiiyuko.manosaba.gui.screen.ManosabaTitleScreen} 统一驱动 {@link #tick()}。
 * <p>
 * 1.21.10 曾直接复用原版 {@code net.minecraft.client.renderer.texture.Tickable}
 * （一个只有 {@code void tick()} 的通用标记接口）。1.21.11 移除了该接口，
 * 只保留语义已收窄为纹理动画的 {@code TickableTexture}，
 * 因此这里改为自定义接口——本 mod 的 tick 语义与纹理无关。
 */
@FunctionalInterface
public interface Tickable {

    void tick();
}
