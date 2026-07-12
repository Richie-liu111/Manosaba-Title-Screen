package me.shiiyuko.manosaba.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.shiiyuko.manosaba.Manosaba
import me.shiiyuko.manosaba.utils.SpriteAtlas
import me.shiiyuko.manosaba.utils.UnitySpriteParser
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.client.gui.screens.world.CreateWorldScreen
import net.minecraft.client.gui.screens.world.SelectWorldScreen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageFilter

private const val BUTTON_SCALE = 0.375f
private const val LOGO_SCALE = 0.5f
private const val IMAGE_SCALE = 1.3f

val manosabaFont: FontFamily = FontFamily(Font(resource = "assets/TsukushiMincho.otf"))

class ManosabaTitleScreen : ComposeScreen(Component.literal("Manosaba Title Screen")) {

    private var atlasImage: Image? = null
    private var atlasData: SpriteAtlas? = null
    private var buttonSprites: Map<String, ImageBitmap>? = null
    private var titleLogo: ImageBitmap? = null
    private var titleOverlay: ImageBitmap? = null

    private var showExitDialog by mutableStateOf(false)
    private var exitDialog: ExitDialog? = null

    private val buttonNames = listOf(
        "Button_NewGame", "Button_LoadGame", "Button_Options",
        "Button_Exit", "Button_Gallery", "Button_WitchBook"
    )

    init {
        loadAtlasResources()
        exitDialog = ExitDialog(
            onCancel = { showExitDialog = false },
            onConfirm = { Minecraft.getInstance().stop() }
        )
    }

    override fun close() {
        super.close()
    }

    private fun playMusic() {
        val client = Minecraft.getInstance()

        client.soundManager.stopAll()
        client.musicTracker.stop()

        val sound = SimpleSoundInstance.forMusic(Manosaba.TITLE_MUSIC.get())
        client.soundManager.play(sound)
    }

    private fun loadAtlasResources() = runCatching {
        val image = UnitySpriteParser.loadAtlasImageFromResources("/assets/UI_Title.png") ?: return@runCatching
        val data = UnitySpriteParser.loadAtlasDataFromResources("/assets/UI_Title.json") ?: return@runCatching

        atlasImage = image
        atlasData = data

        buttonSprites = buttonNames.flatMap { name ->
            listOf("${name}_Normal", "${name}_Highlighted")
        }.mapNotNull { spriteName ->
            data.sprites[spriteName]?.let { spriteData ->
                spriteName to UnitySpriteParser.cropSprite(image, spriteData).toComposeImageBitmap()
            }
        }.toMap()

        titleLogo = data.sprites["TitleLogo@Ja"]?.let { spriteData ->
            UnitySpriteParser.cropSprite(image, spriteData).toComposeImageBitmap()
        }

        titleOverlay = data.sprites["TitleOverlay"]?.let { spriteData ->
            UnitySpriteParser.cropSprite(image, spriteData).toComposeImageBitmap()
        }
    }.onFailure { it.printStackTrace() }

    @Composable
    override fun Content() {
        val client = Minecraft.getInstance()
        val backgroundImage = remember { loadBackgroundImage() }
        val sprites = remember { buttonSprites }
        val logo = remember { titleLogo }
        val overlay = remember { titleOverlay }
        val versionText = remember { "Ver. ${SharedConstants.getCurrentVersion().name}" }

        val backgroundAnimProgress = remember { Animatable(0f) }
        val uiAlpha = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            playMusic()
            backgroundAnimProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
            )
            delay(250L)
            uiAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }

        val scale = 1.1f - (0.1f * backgroundAnimProgress.value)
        val blurAmount = 20f * (1f - backgroundAnimProgress.value)

        Box(modifier = Modifier.fillMaxSize()) {
            backgroundImage?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            if (blurAmount > 0.1f) {
                                renderEffect = ImageFilter.makeBlur(
                                    blurAmount, blurAmount, FilterTileMode.CLAMP
                                ).asComposeRenderEffect()
                            }
                        },
                    contentScale = ContentScale.Crop
                )
            }

            overlay?.let { overlayBitmap ->
                Image(
                    bitmap = overlayBitmap,
                    contentDescription = "Title Overlay",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = uiAlpha.value },
                    contentScale = ContentScale.FillBounds
                )
            }

            logo?.let { logoBitmap ->
                Image(
                    bitmap = logoBitmap,
                    contentDescription = "Title Logo",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp, end = 24.dp)
                        .size(
                            width = (logoBitmap.width * LOGO_SCALE).dp,
                            height = (logoBitmap.height * LOGO_SCALE).dp
                        )
                        .graphicsLayer { alpha = uiAlpha.value }
                )
            }

            sprites?.let { spriteMap ->
                val buttons = listOf(
                    ButtonConfig("LoadGame", 20) { client.setScreen(SelectWorldScreen(this@ManosabaTitleScreen)) },
                    ButtonConfig("NewGame", -20) { CreateWorldScreen.show(client, this@ManosabaTitleScreen) },
                    ButtonConfig("Gallery", 20) { client.setScreen(MultiplayerScreen(this@ManosabaTitleScreen)) },
                    ButtonConfig("Options", -20) { client.setScreen(OptionsScreen(this@ManosabaTitleScreen, client.options)) },
                    ButtonConfig("Exit", 20) { showExitDialog = true }
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp)
                        .graphicsLayer { alpha = uiAlpha.value },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    buttons.forEach { config ->
                        SpriteButton(
                            normalSprite = spriteMap["Button_${config.name}_Normal"],
                            highlightedSprite = spriteMap["Button_${config.name}_Highlighted"],
                            scale = BUTTON_SCALE,
                            imageScale = IMAGE_SCALE,
                            modifier = Modifier.offset(y = config.yOffset.dp),
                            onClick = config.onClick
                        )
                    }
                }
            }

            BasicText(
                text = versionText,
                style = TextStyle(
                    fontFamily = manosabaFont,
                    color = Color.White,
                    fontSize = 20.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 48.dp, bottom = 24.dp)
                    .graphicsLayer { alpha = uiAlpha.value }
            )

            if (showExitDialog) {
                exitDialog?.Content()
            }
        }
    }

    private fun loadBackgroundImage(): ImageBitmap? = runCatching {
        javaClass.getResourceAsStream("/assets/background_ema.png")?.use {
            Image.makeFromEncoded(it.readBytes()).toComposeImageBitmap()
        }
    }.onFailure { it.printStackTrace() }.getOrNull()
}

private data class ButtonConfig(
    val name: String,
    val yOffset: Int,
    val onClick: () -> Unit
)

@Composable
private fun SpriteButton(
    normalSprite: ImageBitmap?,
    highlightedSprite: ImageBitmap?,
    onClick: () -> Unit,
    scale: Float = 0.5f,
    imageScale: Float = 1f,
    modifier: Modifier = Modifier
) {
    normalSprite ?: return

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val currentSprite = highlightedSprite?.takeIf { isHovered } ?: normalSprite

    Box(
        modifier = modifier
            .size(
                width = (normalSprite.width * scale).dp,
                height = (normalSprite.height * scale).dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = currentSprite,
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
                scaleX = imageScale
                scaleY = imageScale
            }
        )
    }
}
