# Changelog

## v1.0.5 — 2026-07-31

### 还原原游戏四项功能（完整 v1.0.5，含 ExitDialog）

基于 AssetRipper 解包（Boot.unity 场景、TitleUI.prefab、ProvidableDialog.prefab、System_Title.nani 剧本）与游戏截图实测，还原原游戏标题界面的全部四个缺失功能：

#### 1. LoadGame 锁定态

- 无存档时 LoadGame 按钮显示锁定纹理（`button_loadgame_locked.png`），无悬停高亮、无点击音效、不可点击
- 判定：`LevelStorageSource.findLevelCandidates().isEmpty()`

#### 2. 入场模糊 + 时间线对齐

- 时序对齐 `System_Title.nani`：背景 1.05×→1.0×（EaseOutQuad，2700ms）+ 全屏黑幕淡出（1800ms）+ UI 延迟淡入
- **模糊**：低分辨率 RenderTarget（640×360）+ 线性过滤上采样 = 高斯式模糊，blurPower 1→0 交叉淡化

#### 3. 启动 Logo（BootLogoScreen）

- 游戏加载完成后、首次进主界面之前播放发行商/开发商 logo（会话内仅一次，不可跳过）
- 布局还原 Boot.unity 场景：BrandLogo（Acacia）787×309、CompanyLogo（REAER）1000×223，2560×1440 设计空间
- 淡入 500ms → 停留 2500ms → 淡出 500ms

#### 4. 退出确认对话框（ExitDialog）

- 布局还原 ProvidableDialog.prefab（含 pivot 换算）：全屏压暗黑幕（Underlay α≈0.65）→ 亮色条带（Frame y 370..1070）→ 上下装饰暗条 → 消息黑字 → 取消/结束按钮
- 文字用**原游戏字体**烘焙：消息黑字、按钮白字、「结」/「終」游戏强调粉（#E18796）
- 文案：中「即将结束游戏。」取消/结束；日「ゲームを終了します。」キャンセル/終了する
- 退出时序（对齐 `# QuitGame`）：停 BGM → 提交音效 → 关闭对话框 150ms → 主界面 UI 随黑幕同步淡出 1.2s → 退出游戏

### 音乐系统重构

- **MusicManager 抑制 + 标题 BGM 自行管理**：标题屏（含所有子界面）期间 MusicManager 被完全抑制（`MusicTickerMixin`），BGM 由主界面首个 tick 播放（黑幕开始淡出时响起）——消除了其初始延迟导致的音乐迟到
- **子界面切换 BGM 持续**：Options/世界选择等子界面期间音乐不中断
- **进世界停 BGM**：`ClientPlayerNetworkEvent.LoggingIn` 停止标题音乐并放行游戏音乐
- **切语言重载后自动重播**：资源重载（SoundManager 重建）中断 BGM 后，overlay 关闭时自动从头重播
- **退出期间 BGM 停止**：退出确认后音乐保持停止，不会被重播逻辑误触发

### 音效（原游戏系统音效）

- 接入原游戏 `Sfx_System_*_001`：结束=Sfx_System_Submit_001、取消=Sfx_System_Cancel_001、LoadGame=Sfx_System_LoadData_001、NewGame=Sfx_System_StartGame_001

### 其他

- **悬停屏蔽**：`TitleScreenButton` 新增 `setHoverable()`，退出对话框打开时底层按钮不响应悬停高亮
- **黑幕渲染**：新增 `black.png` 纹理 + position-tex 绘制路径
- **版本号**：1.0.4→1.0.5

## v1.0.4 — 2026-07-29

### Mixin 兼容性加固

基于 YuZuUI 上游 PR（[link-fgfgui/YuZuUI-Forge#1](https://github.com/ming-sc/YuZuUI-Forge/pull/1)）的分析和修复，增强标题画面替换的健壮性。

#### `MinecraftMixin`：双重拦截 `TitleScreen`

- 在现有 `@ModifyVariable`（拦截参数）之上新增 `@Redirect`，拦截 `setScreen()` 调用栈内所有 `new TitleScreen()` 构造
- 修复潜在 bug：读取存档→无存档→进入创建世界→按 ESC 返回→回到原版 TitleScreen
- 原 `@ModifyVariable(argsOnly = true)` 只能拦截显式传参，某些代码路径下 TitleScreen 实例不经过参数传递而漏网

> **注**：NeoForge 版的 `MusicsMixin` 在 v1.0.3 已经使用 `@Redirect`，本次无需更改。

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
- **配置界面**：通过 `IConfigScreenFactory` 扩展点注册配置 GUI，Mods 列表「配置」按钮可用，提供游戏内一键切换背景角色

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