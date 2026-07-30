package com.paulzzh.yuzu;

import com.paulzzh.yuzu.gui.screen.ManosabaTitleScreen;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Manosaba.MODID, value = Side.CLIENT)
public class EventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiMainMenu && !(gui instanceof ManosabaTitleScreen)) {
            event.setGui(new ManosabaTitleScreen());
        }
    }
}
