package me.shiiyuko.manosaba.ui

import me.shiiyuko.manosaba.Manosaba
import me.shiiyuko.manosaba.utils.UnitySpriteParser
import net.minecraft.Util
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
import net.minecraft.util.Mth

class ManosabaTitleScreen : Screen(Component.literal("")) {

    companion object {
        private val BACKGROUND_TEX = ResourceLocation.fromNamespaceAndPath("manosaba", "textures/ui/background.png")
        private val UI_TITLE_TEX = ResourceLocation.fromNamespaceAndPath("manosaba", "textures/ui/ui_title.png")
        private val UI_DIALOG_TEX = ResourceLocation.fromNamespaceAndPath("manosaba", "textures/ui/ui_dialog.png")
        private val UI_COMMON_TEX = ResourceLocation.fromNamespaceAndPath("manosaba", "textures/ui/ui_common.png")

        private const val BUTTON_SCALE = 0.375f
        private const val LOGO_SCALE = 0.5f
        private const val ANIM_DURATION = 2500L
    }

    private var titleJsonData: String? = null
    private var dialogJsonData: String? = null
    private var commonJsonData: String? = null

    private data class ButtonDef(
        val name: String,
        val yOffset: Int,
        val normal: String,
        val highlighted: String,
        val action: () -> Unit
    )

    private val buttons = mutableListOf<ButtonDef>()
    private var hoveredButtonIndex = -1
    private var openTime = 0L
    private var showExitDialog = false

    override fun init() {
        openTime = Util.getMillis()
        loadResourceJson()
        setupButtons()
        playBgm()
    }

    private fun setupButtons() {
        buttons.clear()
        buttons.add(ButtonDef("LoadGame", 20, "Button_LoadGame_Normal", "Button_LoadGame_Highlighted") {
            minecraft!!.setScreen(SelectWorldScreen(this))
        })
        buttons.add(ButtonDef("NewGame", -20, "Button_NewGame_Normal", "Button_NewGame_Highlighted") {
            CreateWorldScreen.openFresh(minecraft!!, this)
        })
        buttons.add(ButtonDef("Gallery", 20, "Button_Gallery_Normal", "Button_Gallery_Highlighted") {
            minecraft!!.setScreen(JoinMultiplayerScreen(this))
        })
        buttons.add(ButtonDef("Options", -20, "Button_Options_Normal", "Button_Options_Highlighted") {
            minecraft!!.setScreen(OptionsScreen(this, minecraft!!.options))
        })
        buttons.add(ButtonDef("Exit", 20, "Button_Exit_Normal", "Button_Exit_Highlighted") {
            showExitDialog = true
        })
    }

    private fun loadResourceJson() = runCatching {
        titleJsonData = javaClass.getResourceAsStream("/assets/manosaba/textures/ui/ui_title.json")
            ?.bufferedReader()?.readText()
        dialogJsonData = javaClass.getResourceAsStream("/assets/manosaba/textures/ui/ui_dialog.json")
            ?.bufferedReader()?.readText()
        commonJsonData = javaClass.getResourceAsStream("/assets/manosaba/textures/ui/ui_common.json")
            ?.bufferedReader()?.readText()
    }.onFailure { it.printStackTrace() }

    private fun playBgm() {
        val music = Manosaba.getTitleMusic() ?: return
        val mc = Minecraft.getInstance()
        mc.soundManager.stop()
        mc.musicManager.stopPlaying()
        mc.soundManager.play(SimpleSoundInstance.forMusic(music))
    }

    // Lazy-parsed sprite data (parsed once on first access from render thread)
    private val titleAtlas by lazy { titleJsonData?.let { UnitySpriteParser.parseAtlas(it) } }
    private val dialogAtlas by lazy { dialogJsonData?.let { UnitySpriteParser.parseAtlas(it) } }
    private val commonAtlas by lazy { commonJsonData?.let { UnitySpriteParser.parseAtlas(it) } }

    override fun render(gfx: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val elapsed = Util.getMillis() - openTime
        val dt = Mth.clamp(elapsed / ANIM_DURATION.toFloat(), 0f, 1f)
        val alpha = dt
        val scale = 1.1f - (0.1f * dt)

        // Background with scale animation
        val bw = (width * scale).toInt()
        val bh = (height * scale).toInt()
        val ox = (bw - width) / 2
        val oy = (bh - height) / 2
        gfx.blit(BACKGROUND_TEX, -ox, -oy, 0f, 0f, bw, bh, bw, bh)

        val atlasSize = 4096 // UI_Title.png is 4096 pixels tall
        val titleTexW = 512 // approximate width of UI_Title.png

        // Title Overlay
        titleAtlas?.sprites?.get("TitleOverlay")?.let { sp ->
            gfx.setColor(1f, 1f, 1f, alpha)
            gfx.blit(UI_TITLE_TEX, 0, 0, sp.x, sp.y, sp.width.toInt(), sp.height.toInt(), titleTexW, atlasSize)
        }

        // Logo (top right)
        titleAtlas?.sprites?.get("TitleLogo@Ja")?.let { sp ->
            val lw = (sp.width * LOGO_SCALE).toInt()
            val lh = (sp.height * LOGO_SCALE).toInt()
            gfx.setColor(1f, 1f, 1f, alpha)
            gfx.blit(UI_TITLE_TEX, width - lw - 24, 24, sp.x, sp.y, lw, lh, sp.width.toInt(), sp.height.toInt())
        }

        // Buttons
        val buttonBaseX = 24
        buttons.forEachIndexed { idx, btn ->
            val spriteName = if (idx == hoveredButtonIndex) btn.highlighted else btn.normal
            titleAtlas?.sprites?.get(spriteName)?.let { sp ->
                val sw = (sp.width * BUTTON_SCALE).toInt()
                val sh = (sp.height * BUTTON_SCALE).toInt()
                val sy = height - 24 - sh + btn.yOffset
                gfx.setColor(1f, 1f, 1f, alpha)
                gfx.blit(UI_TITLE_TEX, buttonBaseX, sy, sp.x, sp.y, sw, sh, sp.width.toInt(), sp.height.toInt())
            }
        }

        // Version text
        val version = "Ver. ${SharedConstants.getCurrentVersion().name}"
        gfx.setColor(1f, 1f, 1f, alpha)
        gfx.drawString(font, version, width - font.width(version) - 48, height - 24, 0xFFFFFF)

        // Reset color
        gfx.setColor(1f, 1f, 1f, 1f)
    }

    override fun mouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (showExitDialog) return true

        val bx = 24
        buttons.forEachIndexed { idx, btn ->
            titleAtlas?.sprites?.get(btn.normal)?.let { sp ->
                val sw = (sp.width * BUTTON_SCALE).toInt()
                val sh = (sp.height * BUTTON_SCALE).toInt()
                val sy = height - 24 - sh + btn.yOffset
                if (mx.toInt() in bx..<bx + sw && my.toInt() in sy..<sy + sh) {
                    btn.action()
                    return true
                }
            }
        }
        return super.mouseClicked(mx, my, button)
    }

    override fun mouseMoved(mx: Double, my: Double) {
        hoveredButtonIndex = -1
        val bx = 24
        buttons.forEachIndexed { idx, btn ->
            titleAtlas?.sprites?.get(btn.normal)?.let { sp ->
                val sw = (sp.width * BUTTON_SCALE).toInt()
                val sh = (sp.height * BUTTON_SCALE).toInt()
                val sy = height - 24 - sh + btn.yOffset
                if (mx.toInt() in bx..<bx + sw && my.toInt() in sy..<sy + sh) {
                    hoveredButtonIndex = idx
                }
            }
        }
    }

    override fun shouldCloseOnEsc() = false
}
