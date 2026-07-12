package me.shiiyuko.manosaba

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.DeferredRegister
import org.slf4j.LoggerFactory

/**
 * Manosaba NeoForge entry point.
 * Replaces the vanilla title screen and splash overlay with 魔法少女ノ魔女裁判 themed UI.
 */
@Mod(Manosaba.MOD_ID)
class Manosaba(modBus: IEventBus) {

    companion object {
        const val MOD_ID = "manosaba"
        private val logger = LoggerFactory.getLogger(MOD_ID)

        /** ResourceLocation for the title BGM sound event */
        val TITLE_MUSIC_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, "music")

        /** DeferredRegister for custom sound events */
        val SOUND_EVENTS: DeferredRegister<SoundEvent> =
            DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID)

        /** Title screen BGM sound event - stored as ResourceKey for lookup */
        private val TITLE_MUSIC_KEY: ResourceKey<SoundEvent> =
            ResourceKey.create(Registries.SOUND_EVENT, TITLE_MUSIC_ID)

        /** Look up the registered sound event. Returns null before registration completes. */
        fun getTitleMusic(): SoundEvent? =
            BuiltInRegistries.SOUND_EVENT.get(TITLE_MUSIC_KEY)
    }

    init {
        SOUND_EVENTS.register(modBus)
        logger.info("Manosaba (NeoForge) initialized!")
    }
}
