package me.shiiyuko.manosaba;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import me.shiiyuko.manosaba.config.ManosabaConfig;
import me.shiiyuko.manosaba.mixins.Mixins;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CoreMod 入口：在类加载阶段注册配置并加载 EARLY mixin。
 * 通过 gradle.properties 的 coreModClass 自动注册为 CoreMod。
 */
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class ManosabaCore implements IFMLLoadingPlugin, IEarlyMixinLoader {
    static {
        try {
            ConfigurationManager.registerConfig(ManosabaConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getMixinConfig() {
        return "mixins.manosaba.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Mixins.getEarlyMixins(loadedCoreMods);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
