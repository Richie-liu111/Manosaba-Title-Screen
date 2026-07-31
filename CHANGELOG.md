# Changelog

## v1.0.5 — 2026-07-31

### 还原原游戏四项功能（同步 forge-1.20.1 + gtnh-1.7.10）

基于原游戏 `System_Title.nani` 剧本与 `Boot.unity` 场景数据，还原标题界面的四个缺失功能：

#### 1. 启动 Logo（BootLogoScreen）

- 游戏加载完成后、首次进主界面之前播放发行商/开发商 logo（会话内仅一次，不可跳过）
- 布局还原 Boot.unity 场景：BrandLogo（Acacia）787×309 @ (−552, +24)、CompanyLogo（REAER）1000×223 @ (+536, −32)，2560×1440 设计空间
- 淡入 500ms → 停留 2500ms → 淡出 500ms；通过 `GuiOpenEvent` 首次路由
- 继承 `GuiScreen`：logo 阶段不触发菜单音乐

#### 2. LoadGame 锁定态

- 无存档时 LoadGame 按钮显示锁定纹理（`button_loadgame_locked.png`），双态均为锁定纹理 → 无悬停高亮
- `setHoverable(false)` + 无 `clickSound` + 无 `onClick` 回调，完全不可交互
- 判定：直接检查 `gameDir/saves/` 目录是否存在子目录

#### 3. 入场时间线对齐

- 时序对齐 `System_Title.nani`：背景 1.05×→1.0×（EaseOutQuad，2700ms）+ 全屏黑幕淡出（1800ms）
- **BGM 对齐**：`@bgm` 在 time:0 即播放（首 tick，黑幕覆盖时音乐已响起），去掉旧版 1200ms 延迟
- **BGM 切语言重播**：检测 `isSoundPlaying()`，SoundHandler 重建后自动重播

#### 4. 音效完善

- 按钮独立音效：LoadGame→Sfx_System_LoadData_001、NewGame→Sfx_System_StartGame_001、其余→button_click_submit
- `ManosabaSounds` 新增 `SFX_SYSTEM_LOADDATA`、`SFX_SYSTEM_STARTGAME` 注册

### 其他改进

- **`TitleScreenButton` 新增 `hoverable` 字段** + `setHoverable()`：锁定态按钮完全禁用悬停和点击
- **`EventHandler` 启动 logo 路由**：`onGuiOpen` 检查 `Manosaba.bootSequencePlayed` → 首次先进 BootLogoScreen
- **黑幕渲染**：新增 `black.png` 纹理 + `drawBlackOverlay()` 方法
- **纹理常量**：`TextureConst` 新增 `BLACK`、`BUTTON_LOAD_GAME_LOCKED`、`BRAND_LOGO`、`COMPANY_LOGO`
- **版本号**：1.0.4→1.0.5

### 已知限制

- **入场模糊未移植**：FBO 渲染在 1.12.2 下投影矩阵处理复杂且方向错误，保留黑幕淡出效果
- **退出确认对话框（ExitDialog）未移植**：装饰条布局与 FBO 渲染复杂度较高
- **启动画面（SplashOverlayRenderer）未移植**：1.12.2 加载流程差异大

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
- **`inGame` 守卫模式**：当 `!Manosaba.inGame`（菜单界面），跳过 `MusicTicker.update()` 并停掉残留声音。`inGame` 在标题画面显示时设为 `false`，进入世界/服务器时设为 `true` → 无论 Options/世界选择等任何子界面，原版 MusicTicker 都不会响起
- `MusicTickerAccessor`（`@Accessor` mixin）访问 `MusicTicker.currentMusic`，返回标题界面时清理残留音乐

**动画系统**
- 全局 `animationStartTime` 时钟（static），非 per-instance startTime
- 窗口 resize → 动画不重置
- ESC 返回子界面 → 动画不重置，BGM 继续
- 游戏退出回标题 → 新实例 → 动画+BGM 完整重播

**界面替换**
- 事件驱动（`GuiOpenEvent`），非 Mixin 参数修改
- 点击按钮 → 子界面显示（BGM 持续） → ESC 返回 → 无动画过渡

### 已知问题
- 启动画面（SplashOverlayRenderer）未移植（1.12.2 的加载流程与 1.20.1 差异较大）【实际上是代替 Mojang 红白加载条，不过一般在资源包加载时才看得到，也不算是“启动画面”】
- 退出对话框（ExitDialog）未移植
