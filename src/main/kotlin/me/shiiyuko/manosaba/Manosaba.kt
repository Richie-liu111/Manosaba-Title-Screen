package me.shiiyuko.manosaba

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.RegistryObject
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

        /** Title screen BGM sound event */
        val TITLE_MUSIC: RegistryObject<SoundEvent> = SOUND_EVENTS.register("music") {
            SoundEvent.createVariableRangeEvent(TITLE_MUSIC_ID)
        }
    }

    init {
        SOUND_EVENTS.register(modBus)
        logger.info("Manosaba (NeoForge) initialized!")
    }
}
