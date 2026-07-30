package me.shiiyuko.manosaba;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import me.shiiyuko.manosaba.mixins.Mixins;

import java.util.List;
import java.util.Set;

/**
 * LATE phase Mixin 加载器。GTNHMixins 在 mod 加载后通过 {@link LateMixin} 注解
 * 发现此实现，调用 {@link #getMixins(Set)} 获取需条件加载的 Mixin 类列表。
 */
@LateMixin
public class ManosabaMixins implements ILateMixinLoader {
    @Override
    public String getMixinConfig() {
        return "mixins.manosaba.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Mixins.getLateMixins(loadedMods);
    }
}
