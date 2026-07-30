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
 *   <li>{@code @ModifyVariable} — 拦截传给 setScreen 的 TitleScreen 参数；</li>
 *   <li>{@code @Redirect} — 兜底拦截 setScreen 调用栈内所有
 *       {@code new TitleScreen()}，防止某些代码路径（如读取存档→无存档→
 *       创建世界→返回）的 TitleScreen 实例不经过参数传递而漏网。</li>
 * </ol>
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