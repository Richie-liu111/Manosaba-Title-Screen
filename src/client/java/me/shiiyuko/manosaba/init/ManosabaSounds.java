package me.shiiyuko.manosaba.init;

import me.shiiyuko.manosaba.Manosaba;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Manosaba 全部音效的注册表（Fabric 直接注册到 {@link BuiltInRegistries#SOUND_EVENT}）。
 * 静态字段初始化即完成注册；{@link #register()} 仅用于显式触发类加载。
 * 字段类型为 {@link Holder}——需要 {@link SoundEvent} 时用 {@code .value()}，
 * {@link net.minecraft.sounds.Music} 等接受 Holder 的场合可直接传。
 */
public final class ManosabaSounds {

    public static final Holder<SoundEvent> TITLE_MUSIC = register("music");
    public static final Holder<SoundEvent> BUTTON_CLICK_SUBMIT = register("button_click_submit");
    public static final Holder<SoundEvent> BUTTON_CLICK_START_GAME = register("button_click_start_game");

    // 原游戏系统音效（Sfx_System_*_001，从原游戏提取）
    public static final Holder<SoundEvent> SFX_SYSTEM_SUBMIT = register("sfx_system_submit_001");
    public static final Holder<SoundEvent> SFX_SYSTEM_CANCEL = register("sfx_system_cancel_001");
    public static final Holder<SoundEvent> SFX_SYSTEM_LOADDATA = register("sfx_system_loaddata_001");
    public static final Holder<SoundEvent> SFX_SYSTEM_STARTGAME = register("sfx_system_startgame_001");

    /** 显式触发类加载（静态字段初始化即完成注册）。由 {@link me.shiiyuko.manosaba.ManosabaClient} 调用。 */
    public static void register() {
        // 静态初始化块在类加载时执行，这里无需额外操作。
    }

    private static Holder<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, name);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id,
                SoundEvent.createVariableRangeEvent(id));
    }

    private ManosabaSounds() {
    }
}
