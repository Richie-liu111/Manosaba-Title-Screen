# Changelog

## v1.0.5 — 2026-10-07（包名对齐 + CI + 交互修复）

### 修复：CI 的 JDK 版本错误（RetroFuturaGradle 需要 Java 25）

workflow 最初给的是 JDK 17（按 Gradle 的最低要求推断），但 RFG 2.0.2 自身是用
Java 25 编译的（class file version 69），在 JDK 17 上连插件都加载不了：

```
java.lang.UnsupportedClassVersionError: com/gtnewhorizons/retrofuturagradle/UserDevPlugin
  has been compiled by a more recent version of the Java Runtime (class file version 69.0),
  this version of the Java Runtime only recognizes class file versions up to 61.0
```

改为 JDK 25（与开发机一致），编译侧的 Java 8 toolchain 与 Azul JDK 16 仍由
foojay resolver 自动下载。

### 修复：Gradle wrapper 指向本机文件，CI 无法构建

`gradle/wrapper/gradle-wrapper.properties` 里的 `distributionUrl` 一直指向
`file:///tmp/gradle-9.3.1-bin.zip`——一个只在作者本机存在的路径（初次移植时
为绕过 GFW 下载失败而留下的 workaround，随初始提交进了仓库）。

后果有两个，第二个当时没被察觉：

1. GitHub Actions 上不存在这个文件，CI 必然失败（首次启用 CI 即暴露）；
2. 本机 `/tmp` 会被系统清理，该文件其实**已经不存在**了——之前能构建只是因为
   Gradle 早先把它解压进了 `~/.gradle/wrapper/dists/` 缓存，一旦缓存失效就再也构建不了。

改为腾讯云镜像（与 `1.21.1neoforge` 分支一致，该分支 CI 已验证可访问）：

```properties
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-9.3.1-bin.zip
validateDistributionUrl=true
```

`README.md` 的 GFW 段一并更正——原先推荐的阿里云地址
`mirrors.aliyun.com/gradle/gradle-9.3.1-bin.zip` 实测是 **404**。

### 修复：原版按钮看不见却仍可交互

**症状**：标题界面上点空白处会误触发原版动作——点到原版「单人游戏」的位置就打开世界选择，
点到「退出」的位置就退出游戏。界面本身渲染正常，所以只有实际点击才会发现。

**根因**（本分支独有，1.7.10 / 1.20.1 / 1.21.x / Fabric 均无此问题）：

- `ManosabaTitleScreen extends GuiMainMenu`，而 `initGui()` 调了 `super.initGui()`，
  `GuiMainMenu.initGui()` 会往 `buttonList` 里塞入原版的
  单人/多人/选项/退出/语言按钮（还有 Forge 的 mods 按钮）；
- `drawScreen()` 被整个覆写且**不调 `super.drawScreen()`** → 那些按钮不被绘制，看不见；
- 但 `GuiScreen` 的交互是按 `buttonList` 分发的：`mouseClicked()` 末尾的
  `super.mouseClicked()` 会遍历它，命中后播按键音并调 `actionPerformed()`。
  键盘也一样——Tab 始终能聚焦到这些隐形按钮，回车即可触发。

**修法**：在 `super.initGui()` 之后加 `this.buttonList.clear();`。
比"不调 `super.mouseClicked()`"更彻底——后者拦不住键盘路径。
`GuiScreen.setWorldAndResolution` 会先清空 `buttonList` 再调 `initGui()`，
所以这里清掉的正好只是 `GuiMainMenu` 刚加进去的那批。

### 新增 CI

`.github/workflows/build.yml`：push / PR 时 `./gradlew build` 并上传 `build/libs/` 产物。
JDK 17（Gradle 9.3.1 运行需求）；编译侧还需要 Java 8 toolchain 与 Azul JDK 16
（`javaCompiler`），由 `settings.gradle` 的 foojay resolver 自动下载——**首次 CI 会比较慢**。
此前本分支从未被自动编译验证过。

### 包名迁移：`com.paulzzh.yuzu.*` / `com.img.*` → `me.shiiyuko.manosaba.*`

本分支是唯一未与其他 4 支对齐包名的分支，现统一到 `me.shiiyuko.manosaba`：

| 原包 | 新包 | 文件数 |
|---|---|---|
| `com.paulzzh.yuzu.*` | `me.shiiyuko.manosaba.*` | 13 |
| `com.img.gui` | `me.shiiyuko.manosaba.gui` | 3（Layer / TitleScreenButton / VirtualScreen） |
| `com.img.function` | `me.shiiyuko.manosaba.function` | 1（AnimationFunction） |

用 `git mv` 移动，git 识别为 rename，历史保留。

**除 `package` / `import` 外，另有 4 处「字符串形式的类名」必须同步改**——编译器不报错，漏掉即运行时崩：

- `build.gradle` — `FMLCorePlugin` manifest 属性的 `ManosabaCore` 全限定名（coremod 加载入口）
- `gradle.properties` — `root_package`（被 `build.gradle` 的 `group =` 读取，也用于 `-ea:` 断言参数）
- `src/main/resources/mixins.manosaba.json` — `"package"` 字段
- `Manosaba.java` — `@Mod(guiFactory = "...")` 注解里的 `ManosabaConfigGuiFactory` 全限定名

另有 `TextureConst.java` 使用内联全限定名（非 import），一并更新。`NOTICE` 与 `README.md` 中的派生文件路径 / 三层架构说明同步改写。

已验证：`./gradlew build` 通过；refmap 重新生成且键名为新包名；产出 jar 的 manifest 为 `FMLCorePlugin: me.shiiyuko.manosaba.ManosabaCore`，类全部位于 `me/shiiyuko/manosaba/**`。
**仍需 `runClient` 验证**：MixinBooter 在运行时按新包名加载 `mixins.manosaba.json` 并应用两个 mixin。

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
