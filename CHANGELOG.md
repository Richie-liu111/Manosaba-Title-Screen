# Changelog

本 mod 为 Manosaba 标题屏替换 mod（灵感来自《魔法少女ノ魔女裁判》）的 **Fabric 1.21.10** 移植版。
源码移植自 `forge1.20.1`（v1.0.5），纯客户端 mod。

## [1.0.5-fabric+config] - 2026-10-07

### 配置界面与 Mod Menu 集成

- **恢复配置功能**：新增 `config/ManosabaConfig`，用 Gson 持久化到 `config/manosaba.json`，
  目前仅一项 `backgroundCharacter`（`HIRO` / `EMA`，默认 `HIRO`）。Fabric 侧没有 Forge 的
  `ModConfigSpec`，故手写读写；文件不存在或损坏时静默退回默认值，不打断启动。
- **新增配置界面** `config/ManosabaConfigScreen`，内容与 Forge / NeoForge 版逐字一致
  （切换背景角色按钮 + 完成按钮）。
- **Mod Menu 集成**：新增 `compat/ManosabaModMenu`，实现 `ModMenuApi#getModConfigScreenFactory`，
  在 Mod Menu（https://github.com/TerraformersMC/ModMenu）的模组列表里为本模组提供「配置」按钮。
- **依赖策略**：Mod Menu 用 `modCompileOnly` + `modLocalRuntime`（`modmenu_version=16.0.1`，
  对应 MC 1.21.9–1.21.10）。未安装 Mod Menu 时 Fabric 不会请求 `"modmenu"` 这组 entrypoint，
  兼容类不会被加载，本模组对 Mod Menu **无硬依赖**（`fabric.mod.json` 里声明为 `suggests`）。
- `TextureConst.background()` 恢复 HIRO / EMA 切换（`background_ema.png` 本就在资源里）。
- `build.gradle` 新增 TerraformersMC maven 仓库。

## [1.0.5-fabric] - 2026-08-08

### 移植（Fabric 1.21.10 / Loom 1.17 / Java 21 / Mojang mappings）

- 全部代码位于 `src/client/java/me/shiiyuko/manosaba/`，单一 client entrypoint（`ManosabaClient`）。
- 引擎核心（`AnimationFunction` / `VirtualScreen` / `Layer` / `TitleScreenButton` / `TextureConst`）原样移植。
- **渲染路径重写**：1.21.10 移除 `RenderSystem.setShader/setShaderColor/setShaderTexture` 与 `BufferUploader` 等
  立即模式 API，全部改为 `GuiGraphics.blit(RenderPipelines.GUI_TEXTURED, ...)` 流水线：
  - alpha 通过 blit 的 ARGB color 参数实现（`RenderUtils.color(alpha)`）；
  - 背景 ContentScale.Crop / 退出对话框亮带 UV 裁切通过 13 参 blit 重载实现（`RenderUtils.blitCrop`）。
- **输入 API**：`mouseClicked(MouseButtonEvent, boolean)` / `keyPressed(KeyEvent)`（1.21.9+ 重写）。
- **音乐替换**：`MusicsMixin` @Redirect `Musics.<clinit>` 的 `new Music(...)`，命中
  `SoundEvents.MUSIC_MENU` 时替换为自定义标题曲（1.21.10 字段改名 `MENU`，mixins 只比对常量不受影响）。
- **BGM 停止**：Forge 版 `ClientPlayerNetworkEvent.LoggingIn` → Fabric `ClientPlayConnectionEvents.JOIN`。

### 与原版 v1.0.5 的差异

- **跳过入场模糊**：删除 640×360 FBO 离屏模糊，背景保留 EaseOutQuad 缩放动画（2700ms）。

### 修复（问题排查后）

- **NewGame 取消 / ESC 无法返回主菜单**：`CreateWorldScreen.openFresh(mc, () -> {})` 传入的
  空 onClose 回调导致 ESC/取消无操作（原版 `popScreen()` → `onClose.run()` 为空）。
  改为 `() -> mc.setScreen(this)`，对齐原版 TitleScreen 的 `method_73413` 模式。
- **开发商/发行商 logo 重复**：验证原版 1.21.10 `LoadingOverlay` 原生渲染 Mojang Studios
  logo（`MOJANG_STUDIOS_LOGO_LOCATION` + `RenderPipelines.MOJANG_LOGO`），且我们的
  SplashOverlayRenderer 与 BootLogoScreen 会连续两次播放同一组 Acacia/REAER logo。
  按用户决定**只保留加载界面 logo**：删除 `BootLogoScreen`，`MinecraftMixin` 直进
  `ManosabaTitleScreen`，移除 `bootSequencePlayed` 标志。
- **BGM 时机**：原版 `onFinish` 在 LoadingOverlay 淡出（约 2s）前即 `setScreen` 标题屏；
  为避免 BGM 在 logo 淡出期间提前响起、入场动画在 logo 背后空转，
  `ManosabaTitleScreen.tick` 在 overlay 移除前延迟启动动画/BGM（对应原游戏
  Boot.unity 静音 → System_Title BGM 起播）；`MusicTickerMixin` 同步补上此窗口的
  原版菜单音乐抑制。
