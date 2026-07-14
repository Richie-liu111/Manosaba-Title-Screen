package me.shiiyuko.manosaba.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 极简配置界面：切换背景角色（希罗/艾玛）。
 */
public class ManosabaConfigScreen extends Screen {

    private final Screen parent;

    public ManosabaConfigScreen(Screen parent) {
        super(Component.literal("Manosaba 配置"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        ManosabaConfig.BackgroundCharacter current = ManosabaConfig.backgroundCharacter();
        String label = "背景角色: " + (current == ManosabaConfig.BackgroundCharacter.HIRO
                ? "希罗 (HIRO)" : "艾玛 (EMA)");

        addRenderableWidget(Button.builder(Component.literal(label), btn -> {
                    ManosabaConfig.toggleBackground();
                    this.minecraft.setScreen(new ManosabaConfigScreen(parent));
                })
                .pos(cx - 100, this.height / 2 - 10)
                .size(200, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("完成"),
                        btn -> this.minecraft.setScreen(parent))
                .pos(cx - 30, this.height / 2 + 30)
                .size(60, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.literal("重启游戏或重新进入标题画面后生效"),
                this.width / 2, this.height / 2 + 55, 0xAAAAAA);
        super.render(guiGraphics, mouseX, mouseY, delta);
    }
}