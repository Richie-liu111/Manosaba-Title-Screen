package me.shiiyuko.manosaba.splash

import me.shiiyuko.manosaba.utils.UnitySpriteParser
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import kotlin.math.max

object SplashOverlayRenderer {

    private const val DESIGN_WIDTH = 1920f
    private const val DESIGN_HEIGHT = 1080f

    private val SPLASH_TEX = ResourceLocation.fromNamespaceAndPath("manosaba", "textures/ui/splash_screen.png")
    private val SPLASH_JSON = "/assets/manosaba/textures/ui/splash_screen.json"

    private data class SpriteInfo(val x: Float, val y: Float, val w: Float, val h: Float)

    private var brandLogo: SpriteInfo? = null
    private var companyLogo: SpriteInfo? = null
    private var loaded = false

    private fun load() {
        if (loaded) return
        runCatching {
            val json = javaClass.getResourceAsStream(SPLASH_JSON)
                ?.bufferedReader()?.readText() ?: return

            val parsed = UnitySpriteParser.parseAtlas(json)

            parsed.sprites["BrandLogo_Acacia"]?.let {
                brandLogo = SpriteInfo(it.x, it.y, it.width, it.height)
            }
            parsed.sprites["CompanyLogo_ReAER"]?.let {
                companyLogo = SpriteInfo(it.x, it.y, it.width, it.height)
            }
            loaded = true
        }.onFailure { it.printStackTrace() }
    }

    @JvmStatic
    fun cleanup() {
        loaded = false
        brandLogo = null
        companyLogo = null
    }

    @JvmStatic
    fun render(gfx: GuiGraphics, loadProgress: Float, alpha: Float) {
        load()

        val w = gfx.guiWidth()
        val h = gfx.guiHeight()

        // Black background
        gfx.fill(0, 0, w, h, 0xFF000000.toInt())

        gfx.setColor(1f, 1f, 1f, alpha)

        // Center both logos vertically
        val brandH = brandLogo?.h?.toInt() ?: 0
        val compH = companyLogo?.h?.toInt() ?: 0
        val totalH = brandH + 64 + compH
        var y = (h - totalH) / 2

        fun flipY(yy: Float, h: Float) = 1024f - yy - h

        brandLogo?.let { logo ->
            val x = (w - logo.w.toInt()) / 2
            gfx.blit(SPLASH_TEX, x, y, logo.x, flipY(logo.y, logo.h), logo.w.toInt(), logo.h.toInt(), 1024, 1024)
            y += logo.h.toInt() + 64
        }

        companyLogo?.let { logo ->
            val x = (w - logo.w.toInt()) / 2
            gfx.blit(SPLASH_TEX, x, y, logo.x, flipY(logo.y, logo.h), logo.w.toInt(), logo.h.toInt(), 1024, 1024)
        }

        gfx.setColor(1f, 1f, 1f, 1f)
    }
}
