package com.paulzzh.yuzu.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class ManosabaSounds {

    public static SoundEvent TITLE_MUSIC;
    public static SoundEvent BUTTON_CLICK_SUBMIT;
    public static SoundEvent BUTTON_CLICK_START_GAME;
    public static SoundEvent SFX_SYSTEM_LOADDATA;
    public static SoundEvent SFX_SYSTEM_STARTGAME;

    private ManosabaSounds() {
    }

    public static void registerSounds() {
        TITLE_MUSIC = register("music");
        BUTTON_CLICK_SUBMIT = register("button_click_submit");
        BUTTON_CLICK_START_GAME = register("button_click_start_game");
        SFX_SYSTEM_LOADDATA = register("sfx_system_loaddata_001");
        SFX_SYSTEM_STARTGAME = register("sfx_system_startgame_001");
    }

    private static SoundEvent register(String name) {
        ResourceLocation location = new ResourceLocation("manosaba", name);
        SoundEvent event = new SoundEvent(location).setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
