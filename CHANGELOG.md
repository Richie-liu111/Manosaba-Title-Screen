# Changelog

## v1.0.5 — 2026-07-31

### 还原原游戏四项功能（同步 forge-1.20.1 v1.0.5）

基于原游戏 `System_Title.nani` 剧本与 `Boot.unity` 场景数据，还原标题界面的四个缺失功能：

#### 1. 启动 Logo（BootLogoScreen）

- 游戏加载完成后、首次进主界面之前播放发行商/开发商 logo（会话内仅一次）
- 布局还原 Boot.unity 场景：BrandLogo（Acacia）787×309 @ (−552, +24)、CompanyLogo（REAER）1000×223 @ (+536, −32)，2560×1440 设计空间
- 淡入 500ms → 停留 2500ms → 淡出 500ms；任意键/点击跳过
- 继承 `GuiScreen`：logo 阶段不触发菜单音乐（BGM 在标题序列才开始，对齐 `@bgm` 时机）

#### 2. LoadGame 锁定态

- 无存档时 LoadGame 按钮显示锁定纹理（`button_loadgame_locked.png`），双态均为锁定纹理 → 无悬停高亮
- `setHoverable(false)` + 无 `clickSound` + 无 `onClick` 回调，完全不可交互
- 判定：直接检查 `mcDataDir/saves/` 目录是否存在子目录（1.7.10 兼容方式）

#### 3. 入场时间线对齐 + 模糊

- 时序对齐 `System_Title.nani`：背景 1.05×→1.0×（EaseOutQuad，2700ms）+ 入场模糊（low-res FBO + 线性上采样，blurPower 1→0）+ 全屏黑幕淡出（1800ms）
- **BGM 对齐**：`@bgm` 在 time:0 即播放（首 tick，黑幕覆盖时音乐已响起），去掉了旧版 1500ms 延迟
- **模糊**：EXT_framebuffer_object FBO（640×360），GL_LINEAR 上采样，1→0 交叉淡化

#### 4. 音效完善

- 按钮独立音效：LoadGame→Sfx_System_LoadData_001、NewGame→Sfx_System_StartGame_001、其余→button_click_submit
- `TitleScreenButton` 新增 `setClickSound(ResourceLocation)` 方法，各按钮独立指定

### 其他改进

- **`TitleScreenButton` 新增 `hoverable` 字段**：锁定态按钮可完全禁用悬停高亮和点击
- **`MinecraftMixin` 启动 logo 路由**：首次进主界面先播 BootLogoScreen，播完再进 ManosabaTitleScreen 入场
- **`MusicTickerMixin` 增强**：BootLogoScreen 期间抑制 vanilla MusicTicker
- **黑幕渲染**：新增 `black.png` 纹理 + `drawBlackOverlay()`，position-tex 路径保证渲染
- **纹理常量**：`TextureConst` 新增 `BLACK`、`BUTTON_LOAD_GAME_LOCKED`

### 已知限制

- **退出确认对话框（ExitDialog）未移植**：1.7.10 下装饰条纹理合成 + FBO 渲染的复杂度较高，暂时保留旧版 Exit 行为（直接退出/回原版菜单）

## v1.0.0-gtnh — 2026-07-30

### GTNH 1.7.10 移植

基于 Forge 1.20.1 版的 Manosaba Title Screen，完整移植到 Minecraft 1.7.10（GT New Horizons）。

#### 构建与依赖

- **GTNH Convention Plugin**：替代 ForgeGradle，由 `gradle.properties` 驱动，自动配置 RetroFuturaGradle + MCP + Mixin 注记处理
- **UniMixins + GTNHLib**：替代 1.20.1 内建的 MixinBootstrap 和 ForgeConfigSpec
- **CoreMod 入口**：`ManosabaCore` 实现 `IFMLLoadingPlugin` + `IEarlyMixinLoader`，在类加载阶段注入 EARLY mixin
- **GTNHLib Config**：`@Config` 注解驱动配置，自动生成 GUI 配置界面
- **mcmod.info**：1.7.10 标准模组元数据格式

#### API 适配（1.20.1 → 1.7.10）

所有渲染、GUI、音效、事件系统 API 均已完成适配：

| 1.20.1 | 1.7.10 |
|---|---|
| `GuiGraphics.blit()` + `RenderSystem` | `Tessellator.startDrawingQuads()` + `GL11` |
| `TitleScreen` | `GuiScreen`（对应原版 `GuiMainMenu`） |
| `Minecraft.setScreen()` | `Minecraft.displayGuiScreen()` |
| `Minecraft.getInstance()` | `Minecraft.getMinecraft()` |
| `Minecraft.options.languageCode` | `Minecraft.gameSettings.language` |
| `Component` / `Font.drawCenteredString()` | `String` / `FontRenderer.drawStringWithShadow()` |
| `DeferredRegister<SoundEvent>` | `sounds.json` + `PositionedSoundRecord` |
| `Util.getEpochMillis()` | `System.currentTimeMillis()` |
| `@ModifyVariable` + `@Redirect` | `@Inject` at `RETURN` + 单例替换 |

#### Mixin 架构

- **MinecraftMixin**：拦截 `displayGuiScreen` 替换 `GuiMainMenu`；注入 `loadWorld` 追踪 `inGamed` 状态
  - `exit` 守卫：退出时跳过替换，允许回到原版菜单
  - `inGamed` 守卫：从世界返回时重播入场动画
  - CustomMainMenu 兼容（检测 `lumien.custommainmenu.gui.GuiCustom`）
- **MusicTickerMixin**：阻止原版 `MusicTicker` 播放菜单音乐；检测离开标题画面体系时停止自定义音乐
- **条件加载**：仿 YuZuUI-GTNH 的 `Mixins` 枚举 + `TargetedMod` 模式

#### 画面与动画

- **2560×1440 虚拟画布**：与 forge-1.20.1 一致的布局坐标
- **背景 ContentScale.Crop**：2:1 纹理裁剪到 16:9，1.1×→1.0× 缩放动画
- **TitleOverlay + TitleLogo**：2750ms 延迟后 500ms 淡入
- **5 个按钮**：原游戏 anchoredPosition 精确坐标，Normal 容器偏移修正
- **按钮中文标签**：悬停时叠加 Label@ZhHans 精灵（仅简中语言），3 层坐标链换算
- **语言自适应 Logo**：简中=中文 Logo，其余=日文 Logo
- **动画重播**：从世界返回标题画面时完整重播入场动画
- **背景角色切换**：游戏内配置切换希罗/艾玛背景

#### 音效

- **自定义标题曲**：《gDie Divil JIO》
- **音乐生命周期**：标题画面播放 → 子画面（GuiOptions 等）切换不中断 → 离开标题画面体系时停止
- **Vanilla MusicTicker 拦截**（EARLY phase）：`MusicTickerMixin` 无条件 `ci.cancel()`，仿 YuZuUI 不检查 `currentScreen`
- **Galacticraft MusicTickerGC 拦截**（LATE phase）：`MusicTickerGCMixin` 条件加载，针对 GTNH 2.8.4 中 Galacticraft 替换的 `MusicTickerGC`
- **LATE Mixin 加载**：`ManosabaMixins` 实现 `ILateMixinLoader` + `@LateMixin`，GTNHMixins 自动发现
- **音乐退出清理**：Exit 时先停音乐再设 `exit` 标记，避免音乐残留到原版菜单

#### 配置

- **GTNHLib @Config**：`useEmaBackground`（布尔复选框）、`bgm`、`justExit`
- **mcmod.info**：`modListVersion: 2` 格式，完整的作者致谢和 YuZuUI-GTNH 引用

#### 修复记录

- **背景缩放不动**：`backgroundLayer` 未 tick
- **Exit 循环**：`displayGuiScreen(null)` → `GuiMainMenu` → Mixin 再拦截
- **动画每次重置**：`initGui()` 每次重新初始化
- **按钮打断音乐**：`onGuiClosed()` 在切子画面时停音乐
- **GTNH 音乐不拦截**：Galacticraft 替换 `MusicTicker` 为 `MusicTickerGC`，需单独 Mixin
- **版本号截断**：坐标计算修正
- **mod options 花屏**：缺少 `mcmod.info`
- **Mod 图标**：`icon.png` 资源

#### 已知限制

- **LWJGL 2 ARM64**：`runClient` 裸 Forge 下可能不稳定（OpenAL native 兼容性）
- **JDK 要求**：构建需 JDK 17+（推荐 21）；GTNH Convention Plugin 通过 Jabel 编译现代语法到 JVM 8 字节码
