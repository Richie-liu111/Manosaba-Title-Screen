# Changelog

本 mod 为 Manosaba 标题屏替换 mod（灵感来自《魔法少女ノ魔女裁判》）的 **Fabric 1.21.11** 移植版。
源码移植自 `1.21.10fabric`（v1.0.5），纯客户端 mod。

## [1.0.5-fabric-1.21.11+config] - 2026-10-07

### 配置界面与 Mod Menu 集成

- **恢复配置功能**：新增 `config/ManosabaConfig`，用 Gson 持久化到 `config/manosaba.json`，
  目前仅一项 `backgroundCharacter`（`HIRO` / `EMA`，默认 `HIRO`）。Fabric 侧没有 Forge 的
  `ModConfigSpec`，故手写读写；文件不存在或损坏时静默退回默认值，不打断启动。
- **新增配置界面** `config/ManosabaConfigScreen`，内容与 Forge / NeoForge 版逐字一致
  （切换背景角色按钮 + 完成按钮）。
- **Mod Menu 集成**：新增 `compat/ManosabaModMenu`，实现 `ModMenuApi#getModConfigScreenFactory`，
  在 Mod Menu（https://github.com/TerraformersMC/ModMenu）的模组列表里为本模组提供「配置」按钮。
- **依赖策略**：Mod Menu 用 `modCompileOnly` + `modLocalRuntime`（`modmenu_version=17.0.1`，
  对应 MC 1.21.11）。未安装 Mod Menu 时 Fabric 不会请求 `"modmenu"` 这组 entrypoint，
  兼容类不会被加载，本模组对 Mod Menu **无硬依赖**（`fabric.mod.json` 里声明为 `suggests`）。
- `TextureConst.background()` 恢复 HIRO / EMA 切换（`background_ema.png` 本就在资源里）。
- `build.gradle` 新增 TerraformersMC maven 仓库。

## [1.0.5-fabric-1.21.11] - 2026-09-28

### 移植（Fabric 1.21.11 / Loom 1.18.2 / Java 21 / Mojang mappings）

从 `1.21.10fabric` 分支整体迁移，源码结构、类名、包名、渲染数学与动画时序全部保持一致
（`src/client/java/me/shiiyuko/manosaba/`，单一 client entrypoint `ManosabaClient`）。

**渲染层零改动。** 1.21.10 引入的 blaze3d GPU 流水线在 1.21.11 中未变：
`RenderPipelines.GUI_TEXTURED` 仍在，`GuiGraphics.blit` 的 11 参（整图 + ARGB alpha）
与 13 参（UV 裁切 + ARGB alpha）重载签名完全一致，因此 `RenderUtils.blit` / `RenderUtils.blitCrop`
及背景 ContentScale.Crop、退出对话框亮带裁切等全部逻辑原样保留。

### API 适配（逐项经 `javap` 对照 1.21.11 remap 产物核实）

| # | 1.21.10 | 1.21.11 | 影响文件 |
|---|---------|---------|----------|
| 1 | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier`（整体改名，`fromNamespaceAndPath` 不变） | `TextureConst` / `Layer` / `TitleScreenButton` / `ManosabaSounds` / `RenderUtils` / `ManosabaTitleScreen` |
| 2 | `net.minecraft.Util` | `net.minecraft.util.Util`（换包，`getMillis` / `getEpochMillis` 不变） | `Layer` / `TitleScreenButton` / `ExitDialog` / `LoadingOverlayMixin` |
| 3 | `net.minecraft.client.renderer.texture.Tickable` | **原版接口被移除**（仅余语义收窄为纹理动画的 `TickableTexture`） | 自建 `me.shiiyuko.manosaba.gui.Tickable`（`void tick()`），`Layer` / `TitleScreenButton` 改实现它 |
| 4 | `SimpleSoundInstance.forMusic(SoundEvent, float)` | `forMusic(SoundEvent)`（收敛为单参，音量/音高取默认） | `ManosabaTitleScreen` |
| 5 | `Screen.resize(Minecraft, int, int)` | `Screen.resize(int, int)`（去掉 `Minecraft` 首参） | `LoadingOverlayMixin` |

第 3 项是本分支唯一的结构性改动：原先复用原版 `Tickable` 是借用了一个通用标记接口，
现在该接口被原版删除，故在 `me.shiiyuko.manosaba.gui` 下自建同语义接口——本 mod 的 tick
与纹理动画无关，因此不复用 `TickableTexture`。

### 构建配置

- `gradle.properties`：`minecraft_version=1.21.11`、`loader_version=0.19.5`、`loom_version=1.18-SNAPSHOT`、
  `fabric_api_version=0.141.6+1.21.11`、`mod_version=1.0.5`、`maven_group=me.shiiyuko.manosaba`。
- `settings.gradle`：`rootProject.name = 'manosaba'`。
- `build.gradle`：`version`/`group` 改从 properties 读取，loom `mods` 块改名为 `manosaba`。
- `fabric.mod.json`：`id=manosaba`、`minecraft: ~1.21.11`、`fabricloader: >=0.19.5`。
- `LICENSE`：替换 MDK 自带的 CC0 模板为 Apache-2.0 全文，与仓库其余 6 个分支对齐。
- 移除 Fabric MDK 脚手架（`com/example/**`、`modid.mixins.json`、`assets/modid/`）。

### 运行时注入点校验（编译期无法发现 mixin 目标失配）

- **`MinecraftMixin`** — `Minecraft.setScreen` 字节码偏移 80 处仍为
  `new TitleScreen()` + `invokespecial TitleScreen.<init>()V`，`@ModifyVariable`（HEAD, ordinal 0, argsOnly）
  与 `@Redirect`（`NEW TitleScreen`）两个注入点均有效。1.21.11 新增的 `setScreenAndShow(Screen)`
  内部仅 `setScreen` + `runTick(false)`，不构成绕过路径。
- **`MusicsMixin`** — `Musics.<clinit>` 仍为 `SoundEvents.MUSIC_MENU` → `new Music(Holder,int,int,boolean)`
  → `putstatic Musics.MENU`，`@Redirect` 的 `(Lnet/minecraft/core/Holder;IIZ)Lnet/minecraft/sounds/Music;`
  描述符逐字节匹配。
- **`ScreenMixin`** — `Screen.renderables` / `children` / `narratables` 字段名与可见性未变。
- **`MusicTickerMixin`** — `MusicManager.tick()` 与 public 字段 `Minecraft.screen` 未变。
- **`LoadingOverlayMixin`** — `minecraft` / `reload` / `onFinish` / `fadeIn` / `fadeOutStart` / `fadeInStart`
  六个字段名与 `render(GuiGraphics,int,int,float)` 签名未变。

### 沿用 1.21.10 分支的行为（相对原版 v1.0.5 Forge 1.20.1）

- **跳过入场模糊**：删除 640×360 FBO 离屏模糊，背景保留 EaseOutQuad 缩放动画（2700ms）。
- **logo 只播一次**：高版本原版加载界面已原生显示开发者 logo，删除 `BootLogoScreen`，
  发行商/开发商 logo 仅在加载界面播放一次。
- **NewGame 取消 / ESC 返回主菜单**：`CreateWorldScreen.openFresh(mc, () -> mc.setScreen(this))`。
- **BGM 时机**：`ManosabaTitleScreen.tick` 在 loading overlay 移除前延迟启动动画/BGM，
  `MusicTickerMixin` 同步抑制该窗口内的原版菜单音乐。
