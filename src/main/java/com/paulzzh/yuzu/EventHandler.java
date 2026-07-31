package com.paulzzh.yuzu;

import com.paulzzh.yuzu.gui.screen.BootLogoScreen;
import com.paulzzh.yuzu.gui.screen.ManosabaTitleScreen;
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
            event.setGui(new ManosabaTitleScreen());
            Manosaba.inGame = false;
            Manosaba.LOGGER.info("inGame = false (title screen shown)");
        }
    }

    @SubscribeEvent
    public static void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // 玩家进入世界/服务器 → 标记 inGame，允许 MusicTicker 运行
        Manosaba.inGame = true;
        Manosaba.LOGGER.info("inGame = true (joined server/world)");
    }
}
