package me.shiiyuko.manosaba.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shiiyuko.manosaba.config.ManosabaConfigScreen;

/**
 * Mod Menu（https://github.com/TerraformersMC/ModMenu）集成：
 * 在模组列表里给本模组加一个「配置」按钮，打开 {@link ManosabaConfigScreen}。
 *
 * <p>声明在 {@code fabric.mod.json} 的 {@code "modmenu"} entrypoint 下。
 * Mod Menu 只是 <b>compileOnly</b> 依赖——未安装 Mod Menu 时，Fabric 永远不会
 * 请求 {@code "modmenu"} 这一组 entrypoint，本类也就不会被加载，因此本模组
 * 对 Mod Menu 没有硬依赖。
 */
public class ManosabaModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ManosabaConfigScreen::new;
    }
}
