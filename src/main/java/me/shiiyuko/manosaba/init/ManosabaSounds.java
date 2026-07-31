package me.shiiyuko.manosaba.init;

import me.shiiyuko.manosaba.Manosaba;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ManosabaSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Manosaba.MODID);

    public static final RegistryObject<SoundEvent> TITLE_MUSIC =
            SOUND_EVENTS.register("music", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Manosaba.MODID, "music")));

    public static final RegistryObject<SoundEvent> BUTTON_CLICK_SUBMIT =
            SOUND_EVENTS.register("button_click_submit", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Manosaba.MODID, "button_click_submit")));

    public static final RegistryObject<SoundEvent> BUTTON_CLICK_START_GAME =
            SOUND_EVENTS.register("button_click_start_game", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Manosaba.MODID, "button_click_start_game")));

    // 原游戏系统音效（Sfx_System_*_001，从原游戏提取）
    public static final RegistryObject<SoundEvent> SFX_SYSTEM_SUBMIT =
            SOUND_EVENTS.register("sfx_system_submit_001", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Manosaba.MODID, "sfx_system_submit_001")));

    public static final RegistryObject<SoundEvent> SFX_SYSTEM_CANCEL =
            SOUND_EVENTS.register("sfx_system_cancel_001", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Manosaba.MODID, "sfx_system_cancel_001")));

    public static final RegistryObject<SoundEvent> SFX_SYSTEM_LOADDATA =
            SOUND_EVENTS.register("sfx_system_loaddata_001", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Manosaba.MODID, "sfx_system_loaddata_001")));

    public static final RegistryObject<SoundEvent> SFX_SYSTEM_STARTGAME =
            SOUND_EVENTS.register("sfx_system_startgame_001", () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Manosaba.MODID, "sfx_system_startgame_001")));

    private ManosabaSounds() {
    }
}