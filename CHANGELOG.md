# Changelog

## v1.0.4 — 2026-07-29

### Mixin 兼容性加固

基于 YuZuUI 上游 PR（link-fgfgui#1）的分析和修复，增强标题画面替换的健壮性。

#### `MinecraftMixin`：双重拦截 `TitleScreen`

- 在现有 `@ModifyVariable`（拦截参数）之上新增 `@Redirect`，拦截 `setScreen()` 调用栈内所有 `new TitleScreen()` 构造
- 修复潜在 bug：读取存档→无存档→进入创建世界→按 ESC 返回→回到原版 TitleScreen
- 原 `@ModifyVariable(argsOnly = true)` 只能拦截显式传参，某些代码路径下 TitleScreen 实例不经过参数传递而漏网

#### `MusicsMixin`：`@Shadow @Final @Mutable` → `@Redirect`

- 不再通过 Mixin 反射修改 `static final` 字段，改为 `@Redirect` 拦截 `new Music(...)` 构造器
- 更干净的字节码注入方式，跨版本兼容性更好
- Forge 版通过 `RegistryObject.getHolder().orElseThrow()` 桥接 `Holder<SoundEvent>` 类型

### 按钮悬停中文标签 + 语言自适应 Logo（2026-07-30）

基于原游戏 AssetRipper 解包的 TitleUI.prefab 完整分析，还原按钮悬停中文标签和语言自适应 Logo。

#### 按钮悬停中文标签

- **`TitleScreenButton` 新增 `setLabelTexture()`**：悬停时在按钮上方叠加独立的中文标签精灵（原游戏 `Label@ZhHans`）
- **位置精确还原**：从 TitleUI.prefab 提取 Normal 容器偏移 + Highlighted 容器偏移 + Label@ZhHans anchoredPosition，三次联立换算为按钮相对坐标
- **修复按钮精灵位置**：原代码假设精灵从按钮根节点中心展开，实际 Unity 中 Normal 子容器有独立 anchoredPosition 偏移（2-11px），已修正按钮初始坐标
- **仅简中显示**：检查 `Minecraft.options.languageCode`，zh* 前缀时显示标签

#### Logo 语言切换

- **动态 Logo 选择**：简中时显示中文 Logo（`titlelogo_zhhans.png`），其余语言显示日文 Logo
- **屏幕复用修复**：切语言后标题画面实例被复用，改为在 `render()` 中动态更新纹理

#### 标签素材

- 复用图集拆分阶段导出的 `label_*_zhhans.png`（5 个按钮 + 预留 WitchBook）
- 中文 Logo 素材：`titlelogo_zhhans.png`（已随 v1.0.2 导入）

## v1.0.3 — 2026-07-14

### 背景角色切换 + 模组图标 + 配置界面

- **背景角色配置**：新增 `background_ema.png`（艾玛），通过 `config/manosaba-client.toml` 中 `backgroundCharacter` 项在希罗/艾玛间切换，无需重新编译
- **模组图标**：添加 `icon.png`（来自原游戏 PlayerIcon.icns），Mods 列表显示
- **配置界面**：新增 `MinecraftForge.registerConfigScreen()` / `container.registerExtensionPoint()`，Mods 列表「配置」按钮可用，提供游戏内一键切换背景角色的 GUI

## v1.0.2 — 2026-07-13

### 布局完全还原

基于原游戏（魔法少女ノ魔女裁判）AssetRipper 解包的 TitleUI.prefab 数据，将标题画面的设计空间从自定 1920×1080 切换为原游戏的 2560×1440 参考分辨率，所有 UI 元素位置使用原游戏 RectTransform 的精确坐标。

**适用分支：** `forge-1` (Forge 1.20.1) / `manosaba-neoforge-1.21.1` (NeoForge 1.21.1)

#### 改动点

- **VirtualScreen** `1920×1080` → `2560×1440`（匹配原游戏 CanvasScaler ReferenceResolution）
- **移除** `BUTTON_SCALE=0.6` 和 `LOGO_SCALE=0.65`，素材以原生尺寸放置
- **TitleOverlay** 从 1920×1080 拉伸改为 2560×1440 原生 1:1 填充
- **TitleLogo** 位置使用原游戏 anchoredPosition=(747, 381)，中心锚点
- **5 个按钮** 位置使用原游戏 anchoredPosition，左下角锚定
- **背景 Crop** UV 裁剪算法修正：U 和 V 等比例缩放，保证采样区域始终 16:9，消除缩放动画过程中的横向挤压变形

#### 碰撞箱修正

原游戏按钮使用独立的透明 Image 做命中检测，其 SizeDelta（容器尺寸）小于精灵图尺寸。现 `TitleScreenButton` 新增 `setCollisionSize()` 方法，碰撞检测使用原游戏容器尺寸，居中于渲染矩形内，消除透明区域误触。

| 按钮 | 精灵图（渲染） | 容器（碰撞） | 缩小幅度 |
|---|---|---|---|
| LoadGame | 498×323 | 290×230 | −42%/−29% |
| NewGame | 437×301 | 260×206 | −41%/−32% |
| Gallery | 362×251 | 246×122 | −32%/−51% |
| Options | 338×230 | 248×118 | −27%/−49% |
| Exit | 277×190 | 164×106 | −41%/−44% |

#### 数据来源

所有坐标和尺寸均来自 AssetRipper 解包的原游戏文件：
- `TitleUI.prefab` — 层级结构、RectTransform 锚点和位置
- `UI_Title/*.asset` — 精灵图纹理尺寸
- `MonoBehaviour` 序列化数据 — CanvasGroupButton、CanvasScaler 等组件参数