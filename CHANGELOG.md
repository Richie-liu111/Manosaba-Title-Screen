# Changelog

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
- **GTNH 兼容**：在完整 GTNH 整合包中由 GTNH 音乐系统接管

#### 已知限制

- **LWJGL 2 ARM64**：`runClient` 裸 Forge 下可能不稳定（OpenAL native 兼容性）
- **JDK 要求**：构建需 JDK 17+（推荐 21）；GTNH Convention Plugin 通过 Jabel 编译现代语法到 JVM 8 字节码
