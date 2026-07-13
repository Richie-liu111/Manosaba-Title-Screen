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

    private ManosabaSounds() {
    }
}