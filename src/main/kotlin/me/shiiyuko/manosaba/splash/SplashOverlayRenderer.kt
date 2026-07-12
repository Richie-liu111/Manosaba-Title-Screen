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

        val screenW = gfx.guiWidth()
        val screenH = gfx.guiHeight()

        // Black background
        gfx.fill(0, 0, screenW, screenH, 0xFF000000.toInt())

        // Calculate scale and center
        val scaleX = screenW / DESIGN_WIDTH
        val scaleY = screenH / DESIGN_HEIGHT
        val renderScale = max(scaleX, scaleY)
        val offsetX = ((screenW - DESIGN_WIDTH * renderScale) / 2f).toInt()
        val offsetY = ((screenH - DESIGN_HEIGHT * renderScale) / 2f).toInt()

        gfx.setColor(1f, 1f, 1f, alpha)

        // Draw brand logo
        brandLogo?.let { logo ->
            val blh = (companyLogo?.h ?: 0f)
            val lx = offsetX + ((DESIGN_WIDTH * renderScale - logo.w) / 2f).toInt()
            val ly = offsetY + ((DESIGN_HEIGHT * renderScale - (logo.h + 64f + blh)) / 2f).toInt()
            gfx.blit(SPLASH_TEX, lx, ly, logo.x, logo.y, logo.w.toInt(), logo.h.toInt(), logo.w.toInt(), logo.h.toInt())
        }

        // Draw company logo (below brand logo)
        companyLogo?.let { logo ->
            val blh = (brandLogo?.h ?: 0f)
            val lx = offsetX + ((DESIGN_WIDTH * renderScale - logo.w) / 2f).toInt()
            val ly = offsetY + ((DESIGN_HEIGHT * renderScale - (logo.h + 64f + blh)) / 2f).toInt() + blh.toInt() + 64
            gfx.blit(SPLASH_TEX, lx, ly, logo.x, logo.y, logo.w.toInt(), logo.h.toInt(), logo.w.toInt(), logo.h.toInt())
        }

        gfx.setColor(1f, 1f, 1f, 1f)
    }
}
