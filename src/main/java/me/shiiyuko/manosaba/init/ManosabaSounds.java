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

    private ManosabaSounds() {
    }
}