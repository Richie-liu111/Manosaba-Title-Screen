package me.shiiyuko.manosaba.init;

import me.shiiyuko.manosaba.Manosaba;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 声音注册。NeoForge 1.21.1 使用 DeferredRegister + DeferredHolder，
 * 注册表用原版 Registries.SOUND_EVENT（而非 Forge 的 ForgeRegistries）。
 */
public final class ManosabaSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Manosaba.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TITLE_MUSIC =
            SOUND_EVENTS.register("music", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "music")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTON_CLICK_SUBMIT =
            SOUND_EVENTS.register("button_click_submit", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "button_click_submit")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTON_CLICK_START_GAME =
            SOUND_EVENTS.register("button_click_start_game", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "button_click_start_game")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SFX_SYSTEM_SUBMIT =
            SOUND_EVENTS.register("sfx_system_submit_001", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "sfx_system_submit_001")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SFX_SYSTEM_CANCEL =
            SOUND_EVENTS.register("sfx_system_cancel_001", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "sfx_system_cancel_001")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SFX_SYSTEM_LOADDATA =
            SOUND_EVENTS.register("sfx_system_loaddata_001", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "sfx_system_loaddata_001")));

    public static final DeferredHolder<SoundEvent, SoundEvent> SFX_SYSTEM_STARTGAME =
            SOUND_EVENTS.register("sfx_system_startgame_001", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(Manosaba.MODID, "sfx_system_startgame_001")));

    private ManosabaSounds() {
    }
}