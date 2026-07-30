# Changelog

## v1.0.4 (2026-07-30) — 1.12.2 Initial Port

### 关于此版本

将 Manosaba Title Screen 从 Forge 1.20.1 移植到 Minecraft 1.12.2 Forge。这是第二个「向下移植」版本（之前已有 GTNH 1.7.10 移植）。

### 与 1.20.1 版本的关键差异

**构建体系**
- 使用 RetroFuturaGradle 2.0.2 替代 ForgeGradle 6（GTNH 体系的向下兼容构建工具）
- Gradle 9.3.1，MCP stable_39 映射（非 Mojang 官方映射）
- MixinBooter 10.6 替代内建 Mixin；通过 `IEarlyMixinLoader`（`ManosabaCore`）显式注册 mixin 配置

**渲染管线**
- 从 `GuiGraphics.blit()` + `PoseStack` + `RenderSystem` 切换为 `Tessellator` + `GL11` + `GlStateManager`（LWJGL 2 固定管线）
- `RenderUtils.blit()` 使用 `GL11.GL_QUADS` + `DefaultVertexFormats.POSITION_TEX`，UV 0~1 整张纹理绘制
- 字体渲染使用 `fontRenderer.drawString()`（1.12.2 无 `drawCenteredString` 方法）

**GUI 体系**
- `extends GuiMainMenu`（1.12.2）vs `extends TitleScreen`（1.20.1）
- 接口方面：1.12.2 无 `Renderable` / `Tickable` / `GuiEventListener` — Layer 和 TitleScreenButton 为纯 Java 类，事件通过 `mouseClicked()` 手动派发
- `GuiOpenEvent`（Forge Event）替换 `@ModifyVariable` + `@Redirect` 的 Mixin 屏幕替换

**配置系统**
- 使用 `net.minecraftforge.common.config.Configuration`（1.12.2 标准 API），而非 `ForgeConfigSpec`
- 通过 `IModGuiFactory` 接口将配置界面接入 Forge ModList

**音乐系统**
- 无 `DeferredRegister` → 使用 `ForgeRegistries.SOUND_EVENTS.register()` 直接注册
- Mixin 目标从 `Musics.<clinit>`（1.20.1 的静态初始化劫持）改为 `Minecraft.runTick()` → `MusicTicker.update()` 的 `@Redirect`（拦截调用处）
- BGM 由 `ManosabaTitleScreen.updateScreen()` 自行管理，不依赖 `MusicTicker` 播放

**动画系统**
- 全局 `animationStartTime` 时钟（static），非 per-instance startTime
- 窗口 resize → 动画不重置
- ESC 返回子界面 → 动画不重置，BGM 继续
- 游戏退出回标题 → 新实例 → 动画+BGM 完整重播

**界面替换**
- 事件驱动（`GuiOpenEvent`），非 Mixin 参数修改
- 点击按钮 → 子界面显示（BGM 持续） → ESC 返回 → 无动画过渡

### 已知问题
- 进入游戏世界后自定义 BGM 仍继续播放（需在 `onGuiClosed` 或 GuiOpenEvent 中检测并停止）
- 启动画面（SplashOverlayRenderer）未移植（1.12.2 的加载流程与 1.20.1 差异较大）
- 退出对话框（ExitDialog）未移植
