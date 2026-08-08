package me.shiiyuko.manosaba;

import me.shiiyuko.manosaba.init.ManosabaSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

/**
 * Fabric 客户端入口：注册音效，并监听进入世界事件停止标题 BGM
 * （替代 Forge 版的 {@code ClientPlayerNetworkEvent.LoggingIn}）。
 */
public class ManosabaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ManosabaSounds.register();

        // 进入世界：停止标题 BGM，MusicManager 恢复游戏音乐。
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Manosaba.titleMusicPlaying = false;
            Minecraft.getInstance().getSoundManager().stop();
        });

        Manosaba.LOGGER.info("Manosaba title screen loaded (Fabric 1.21.10).");
    }
}
