package com.paulzzh.yuzu.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * 极简配置界面：切换背景角色（希罗/艾玛）。
 * 参考 forge-1 的 ManosabaConfigScreen + 1.12.2 GuiScreen API。
 */
public class ManosabaConfigScreen extends GuiScreen {

    private final GuiScreen parent;

    public ManosabaConfigScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int cx = this.width / 2;

        ManosabaConfig.BackgroundCharacter current = ManosabaConfig.getBackground();
        String label = "背景角色: " + (current == ManosabaConfig.BackgroundCharacter.HIRO
                ? "希罗 (HIRO)" : "艾玛 (EMA)");

        this.buttonList.add(new GuiButton(0, cx - 100, this.height / 2 - 10, 200, 20, label));
        this.buttonList.add(new GuiButton(1, cx - 30, this.height / 2 + 30, 60, 20, "完成"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            ManosabaConfig.toggleBackground();
            this.mc.displayGuiScreen(new ManosabaConfigScreen(parent));
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Manosaba 配置", this.width / 2, 20, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, "重启游戏或重新进入标题画面后生效",
                this.width / 2, this.height / 2 + 55, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
