# Manosaba Title Screen — Forge 1.12.2

使用《魔法少女ノ魔女裁判》（Magical Girl × Witch Trial）自定义标题画面替换 Minecraft 标题界面。

基于 [YuZuUI-Vintage](https://github.com/RuiXuqi/YuZuUI-Vintage) 的渲染架构和构建体系，移植自 [Manosaba-Title-Screen (Forge 1.20.1)](https://github.com/Richie-liu111/Manosaba-Title-Screen/tree/1.20.1forge)。

## 功能

- **2560×1440 虚拟画布**：基于原游戏 CanvasScaler ReferenceResolution，所有 UI 元素使用原游戏坐标
- **背景 ContentScale.Crop**：2:1（4096×2048）纹理裁切为 16:9，1.1× → 1.0× 缩放动画
- **TitleOverlay + TitleLogo**：画框延迟淡入，Logo 随语言改变（简中 = 中文 Logo，其余 = 日文）
- **5 个按钮**：原游戏 anchoredPosition 还原（2560×1440 设计空间内），Hover 高亮 + 中文标签叠加
- **按钮中文标签**：悬停时显示原游戏 Label@ZhHans 精灵（仅简中语言环境）
- **自定义背景音乐**：替换原版标题音乐，使用 Mixin 拦截 MusicTicker

## 按钮功能映射

| 按钮 | 行为 |
|------|------|
| LoadGame | 打开存档选择界面（`GuiWorldSelection`） |
| NewGame | 打开创建世界界面（`GuiCreateWorld`） |
| Gallery | 打开多人游戏界面（`GuiMultiplayer`） |
| Options | 打开设置界面（`GuiOptions`） |
| Exit | 直接退出游戏（`Minecraft.shutdown()`） |

## 构建与运行

### 环境要求

- JDK 21+（运行 Gradle）
- JDK 8（运行游戏，RetroFuturaGradle 提供工具链自动下载）
- Minecraft 1.12.2
- Forge 14.23.5.2859

### 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/manosaba-1.0.4.jar`。

### 开发运行

```bash
./gradlew runClient
```

### GFW 注意事项

如果 Gradle wrapper 无法从 `services.gradle.org` 下载（SSL 证书错误），编辑 `gradle/wrapper/gradle-wrapper.properties`：

```properties
distributionUrl=https\://mirrors.aliyun.com/gradle/gradle-9.3.1-bin.zip
validateDistributionUrl=false
```

或手动下载 Gradle 9.3.1 后配置 `file://` 本地 URL。

## 架构

### 三层渲染架构

```
com.img.*              → 引擎核心（Layer, VirtualScreen, TitleScreenButton, AnimationFunction）
                         跨版本不修改
com.paulzzh.yuzu.*     → 项目核心（渲染主类, Mixin, 配置, 声音注册）
                         1.12.2 适配层
assets/                 → 资源文件（纹理, 音效, JSON）
                         与 forge-1 共享
```

### 界面替换

使用 Forge `GuiOpenEvent`（而非 Mixin），当 `GuiMainMenu` 被打开时替换为 `ManosabaTitleScreen`。

### 渲染管线

使用 1.12.2 原生 `Tessellator` + `GlStateManager`（LWJGL 2），而非现代 `GuiGraphics.blit()`。`RenderUtils.blit()` 封装了 `GL11.GL_QUADS` + `DefaultVertexFormats.POSITION_TEX` 绘制。

### 音乐

- Mixin：`@Redirect` 拦截 `Minecraft.runTick()` → `MusicTicker.update()` 调用处（参考 YuZuUI-Vintage 写法）
- 当 `currentScreen instanceof ManosabaTitleScreen` 时跳过 `MusicTicker.update()`，防止原版 BGM 播放
- 自定义 BGM 由 `ManosabaTitleScreen.updateScreen()` 管理：延迟约 1.2s 启动，子界面切换时不中断

### 动画

全局 `animationStartTime` 时钟控制所有元素动画。`resize` 不重置时钟，`ESC` 返回子界面不重置，仅新实例（游戏退出回标题）重置。

## 架构差异（vs Forge 1.20.1）

| | 1.12.2 Forge | 1.20.1 Forge |
|---|---|---|
| 构建系统 | RetroFuturaGradle 2.0.2 | ForgeGradle 6 (FG6) |
| Gradle 版本 | 9.3.1 | 8.x |
| JDK | 21（编译）+ 8（运行） | 17 |
| Mixin | MixinBooter 10.6（`IEarlyMixinLoader` 注册） | 内建 Mixin + `mods.toml` |
| 映射 | MCP stable_39 | Mojang 官方 |
| 渲染管线 | Tessellator + GL11 + GlStateManager | GuiGraphics + PoseStack + RenderSystem |
| GUI 基类 | `GuiScreen` / `GuiMainMenu` | `Screen` / `TitleScreen` |
| 界面替换 | `GuiOpenEvent`（Forge Event） | `@ModifyVariable` + `@Redirect`（Mixin） |
| 配置系统 | `Configuration`（旧版 Forge API） | `ForgeConfigSpec` |
| 声音注册 | `ForgeRegistries.SOUND_EVENTS.register()` | `DeferredRegister<SoundEvent>` |
| 字体渲染 | `fontRenderer.drawString()` | `guiGraphics.drawString()` |

## 致谢

- [Shiiyuko](https://github.com/Shiiyuko) — 原始 Fabric 模组
- [YuZuUI-Vintage](https://github.com/RuiXuqi/YuZuUI-Vintage) by RuiXuqi — 架构参考
- [YuZuUI-Forge](https://github.com/ming-sc/YuZuUI-Forge) by IMG — 原始 YuZuUI
- [YuZuUI-GTNH](https://github.com/paulzzh/YuZuUI-GTNH) by Paulzzh — GTNH 移植参考
- 原游戏：《魔法少女ノ魔女裁判》

## 许可证

Apache-2.0

资源文件版权归原开发商所有，不在此许可证范围内。
