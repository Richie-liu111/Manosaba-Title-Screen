package me.shiiyuko.manosaba.mixin;

import me.shiiyuko.manosaba.gui.screen.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces any vanilla {@link TitleScreen} the game tries to display with the
 * custom Manosaba title screen.
 * <p>
 * Two-pronged approach:
 * <ol>
 *   <li>{@code @ModifyVariable} — 拦截传给 setScreen 的 TitleScreen 参数，
 *       一律替换为 ManosabaTitleScreen（开发商/发行商 logo 由
 *       {@code LoadingOverlayMixin} 在加载界面播放，此处不再有独立 logo 屏）；</li>
 *   <li>{@code @Redirect} — 兜底拦截 setScreen 调用栈内所有
 *       {@code new TitleScreen()}，防止某些代码路径（如读取存档→无存档→
 *       创建世界→返回）的 TitleScreen 实例不经过参数传递而漏网。</li>
 * </ol>
 * <p>
 * 1.21.11 校验：{@code Minecraft.setScreen} 字节码中偏移 80 处仍为
 * {@code new TitleScreen()} / {@code invokespecial TitleScreen.<init>()V}，
 * 两个注入点均有效。1.21.11 新增的 {@code setScreenAndShow(Screen)} 内部只是
 * 调用 {@code setScreen} 后再 {@code runTick(false)}，因此不构成绕过路径。
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Screen manosaba$replaceTitleScreen(Screen screen) {
        if (screen instanceof TitleScreen && !(screen instanceof ManosabaTitleScreen)) {
            return new ManosabaTitleScreen();
        }
        return screen;
    }

    @Redirect(method = "setScreen",
            at = @At(value = "NEW", target = "net/minecraft/client/gui/screens/TitleScreen"))
    private TitleScreen manosaba$redirectTitleScreen() {
        return new ManosabaTitleScreen();
    }
}
