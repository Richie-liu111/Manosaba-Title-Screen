package me.shiiyuko.manosaba.mixins;

import cpw.mods.fml.common.Mod;

/**
 * 条件 Mixin 加载的目标 Mod 定义。
 */
public enum TargetedMod {
    VANILLA("Minecraft", null);

    /** "name" in the {@link Mod @Mod} annotation */
    public final String modName;
    /** Class that implements IFMLLoadingPlugin */
    public final String coreModClass;
    /** The "modid" in the {@link Mod @Mod} annotation */
    public final String modId;

    TargetedMod(String modName, String coreModClass) {
        this(modName, coreModClass, null);
    }

    TargetedMod(String modName, String coreModClass, String modId) {
        this.modName = modName;
        this.coreModClass = coreModClass;
        this.modId = modId;
    }

    @Override
    public String toString() {
        return "TargetedMod{modName='" + modName + "', coreModClass='"
                + coreModClass + "', modId='" + modId + "'}";
    }
}
