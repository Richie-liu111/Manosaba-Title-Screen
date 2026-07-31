package me.shiiyuko.manosaba.mixins.early.minecraft;

import me.shiiyuko.manosaba.Manosaba;
import me.shiiyuko.manosaba.gui.BootLogoScreen;
import me.shiiyuko.manosaba.gui.ManosabaTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截 {@link Minecraft#displayGuiScreen}，将原版 {@link GuiMainMenu} 替换为
 * Manosaba 自定义标题画面。兼容 CustomMainMenu。
 * <p>
 * 首次进主界面时先播启动 logo（BootLogoScreen），播完再进
 * ManosabaTitleScreen 入场动画（会话内仅一次）。
 * <p>
 * 参考 YuZuUI-GTNH 的 exit / inGamed 守卫：
 * <ul>
 *   <li>exit=true 时不拦截，让 displayGuiScreen(null) 回到原版菜单；</li>
 *   <li>inGamed 标记在进世界时设 true，返回标题画面时触发动画重播。</li>
 * </ul>
 */
@Mixin(value = Minecraft.class)
public class MinecraftMixin {
    @Unique
    private static ManosabaTitleScreen manosaba$instance = null;

    @Inject(method = "displayGuiScreen", at = @At(value = "RETURN"))
    public void manosaba$replaceMainMenu(GuiScreen guiScreenIn, CallbackInfo ci) {
        if (ManosabaTitleScreen.exit) return;
        if (guiScreenIn instanceof GuiMainMenu
            || (guiScreenIn != null && "lumien.custommainmenu.gui.GuiCustom".equals(
                guiScreenIn.getClass().getCanonicalName()))) {
            // 首次进主界面：先播放启动 logo，再进 Manosaba 标题入场
            if (!Manosaba.bootSequencePlayed) {
                Minecraft.getMinecraft().displayGuiScreen(new BootLogoScreen());
                return;
            }
            if (manosaba$instance == null) {
                manosaba$instance = new ManosabaTitleScreen();
            }
            Minecraft.getMinecraft().displayGuiScreen(manosaba$instance);
        }
    }

    /** 进世界时标记 inGamed，同时重置 exit（从原版菜单进世界后再次返回时需重新拦截）。 */
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundHandler;stopSounds()V"))
    public void manosaba$onLoadWorld(WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        ManosabaTitleScreen.inGamed = true;
        ManosabaTitleScreen.exit = false;
    }
}
