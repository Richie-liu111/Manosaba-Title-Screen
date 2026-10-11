package me.shiiyuko.manosaba;

import me.shiiyuko.manosaba.gui.screen.BootLogoScreen;
import me.shiiyuko.manosaba.gui.screen.ManosabaTitleScreen;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Manosaba 事件处理器。
 * - GuiOpenEvent：替换 GuiMainMenu → ManosabaTitleScreen（首次先进 BootLogoScreen）
 * - ClientConnectedToServer：标记 inGame = true → Mixin 放行 MusicTicker
 */
@Mod.EventBusSubscriber(modid = Manosaba.MODID, value = Side.CLIENT)
public class EventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiMainMenu && !(gui instanceof ManosabaTitleScreen)) {
            // 首次进主界面：先播放启动 logo，再进 Manosaba 标题入场
            if (!Manosaba.bootSequencePlayed) {
                event.setGui(new BootLogoScreen());
                Manosaba.inGame = false;
                return;
            }
            // 复用缓存实例，不要每次都 new。
            // 子界面（如 GuiOptions）按 ESC 时 GuiScreen.keyTyped 会走
            // mc.displayGuiScreen(null)，而原版在无世界时会据此新建一个 GuiMainMenu
            // （见 Minecraft.displayGuiScreen）。这里若也跟着 new，构造函数会把
            // animStart 归零、currentBGM 置 null（旧 BGM 仍在播），表现为入场动画
            // 重播 + 背景音乐叠加。复用同一实例即可；从世界返回时才丢弃（见下方）。
            event.setGui(ManosabaTitleScreen.getInstance());
            Manosaba.inGame = false;
            Manosaba.LOGGER.info("inGame = false (title screen shown)");
        }
    }

    @SubscribeEvent
    public static void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // 玩家进入世界/服务器 → 标记 inGame，允许 MusicTicker 运行
        Manosaba.inGame = true;
        // 丢弃缓存的标题屏实例：下次回到标题界面时重播入场动画（原行为）
        ManosabaTitleScreen.resetInstance();
        Manosaba.LOGGER.info("inGame = true (joined server/world)");
    }
}
