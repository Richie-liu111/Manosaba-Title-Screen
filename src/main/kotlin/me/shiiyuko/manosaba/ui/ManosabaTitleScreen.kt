package me.shiiyuko.manosaba.ui

import me.shiiyuko.manosaba.Manosaba
import me.shiiyuko.manosaba.utils.UnitySpriteParser
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import kotlin.math.max
import kotlin.math.roundToInt

class ManosabaTitleScreen : Screen(Component.literal("")) {

    companion object {
        val BACKGROUND = ResourceLocation.fromNamespaceAndPath("manosaba", "textures/ui/background.png")
        val UI_TITLE = ResourceLocation.fromNamespaceAndPath("manosaba", "textures/ui/ui_title.png")
        const val TEX_W = 4096; const val TEX_H = 2048
        const val BTN_SCALE = 0.375f; const val LOGO_SCALE = 0.5f
    }

    private var json: String? = null
    private val atlas by lazy { json?.let { UnitySpriteParser.parseAtlas(it) } }

    private data class Btn(val name: String, val yOff: Int, val click: () -> Unit) {
        val n get() = "Button_${name}_Normal"
        val h get() = "Button_${name}_Highlighted"
    }

    private val btns = listOf(
        Btn("LoadGame", 20) { minecraft!!.setScreen(SelectWorldScreen(this)) },
        Btn("NewGame", -20) { CreateWorldScreen.openFresh(minecraft!!, this) },
        Btn("Gallery", 20) { minecraft!!.setScreen(JoinMultiplayerScreen(this)) },
        Btn("Options", -20) { minecraft!!.setScreen(OptionsScreen(this, minecraft!!.options)) },
        Btn("Exit", 20) { }
    )
    private var hovered = -1

    override fun init() {
        json = runCatching {
            javaClass.getResourceAsStream("/assets/manosaba/textures/ui/ui_title.json")?.bufferedReader()?.readText()
        }.onFailure { it.printStackTrace() }.getOrNull()
        playMusic()
    }

    private fun playMusic() {
        val music = Manosaba.getTitleMusic() ?: return
        val m = Minecraft.getInstance(); m.soundManager.stop(); m.musicManager.stopPlaying()
        m.soundManager.play(SimpleSoundInstance.forMusic(music))
    }

    override fun render(gfx: GuiGraphics, mx: Int, my: Int, delta: Float) {
        // Background: ContentScale.Crop from 4096x2048 source
        val s = max(width.toFloat() / TEX_W, height.toFloat() / TEX_H)
        val cw = (width / s).roundToInt()
        val ch = (height / s).roundToInt()
        val cx = (TEX_W - cw) / 2f
        val cy = (TEX_H - ch) / 2f
        // blit: (tex, dstX, dstY, dstW, dstH, srcU, srcV, srcW, srcH, texW, texH)
        gfx.blit(BACKGROUND, 0, 0, width, height, cx, cy, cw, ch, TEX_W, TEX_H)

        val a = atlas ?: return

        // Unity sprite Y is measured from BOTTOM of texture; Minecraft V is from TOP
        fun flipY(y: Float, h: Float) = TEX_H - y - h

        a.sprites["TitleOverlay"]?.let { s ->
            gfx.blit(UI_TITLE, 0, 0, width, height, s.x, flipY(s.y, s.height), s.width.toInt(), s.height.toInt(), TEX_W, TEX_H)
        }

        a.sprites["TitleLogo@Ja"]?.let { s ->
            val lw = (s.width * LOGO_SCALE).toInt(); val lh = (s.height * LOGO_SCALE).toInt()
            gfx.blit(UI_TITLE, width - lw - 24, 24, lw, lh, s.x, flipY(s.y, s.height), s.width.toInt(), s.height.toInt(), TEX_W, TEX_H)
        }

        btns.forEachIndexed { i, b ->
            val key = if (i == hovered) b.h else b.n
            a.sprites[key]?.let { s ->
                val sw = (s.width * BTN_SCALE).toInt(); val sh = (s.height * BTN_SCALE).toInt()
                gfx.blit(UI_TITLE, 24, height - 24 - sh + b.yOff, sw, sh, s.x, flipY(s.y, s.height), s.width.toInt(), s.height.toInt(), TEX_W, TEX_H)
            }
        }

        gfx.drawString(font, "Ver.${SharedConstants.getCurrentVersion().name}", width - 48, height - 24, 0xFFFFFF)
    }

    override fun mouseClicked(mx: Double, my: Double, b: Int): Boolean {
        btns.forEach { btn ->
            atlas?.sprites?.get(btn.n)?.let { s ->
                val sw = (s.width * BTN_SCALE).toInt(); val sh = (s.height * BTN_SCALE).toInt()
                val sy = height - 24 - sh + btn.yOff
                if (mx.toInt() in 24..<24 + sw && my.toInt() in sy..<sy + sh) { btn.click(); return true }
            }
        }
        return false
    }

    override fun mouseMoved(mx: Double, my: Double) {
        hovered = -1
        btns.forEachIndexed { i, b ->
            atlas?.sprites?.get(b.n)?.let { s ->
                val sw = (s.width * BTN_SCALE).toInt(); val sh = (s.height * BTN_SCALE).toInt()
                val sy = height - 24 - sh + b.yOff
                if (mx.toInt() in 24..<24 + sw && my.toInt() in sy..<sy + sh) hovered = i
            }
        }
    }

    override fun shouldCloseOnEsc() = false
}
