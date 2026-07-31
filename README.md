# Manosaba Title Screen (Forge 1.20.1)

一个 Minecraft Forge 1.20.1 客户端模组，用于自定义标题屏幕和启动画面，灵感来源于游戏《魔法少女ノ魔女裁判》。

这是原 [Manosaba-Title-Screen](https://github.com/Shiiyuko/Manosaba-Title-Screen)（Fabric + Compose Desktop）项目的 Forge 1.20.1 移植版。由于原项目依赖 `androidx.compose.runtime.SnapshotStateKt`，在 Minecraft 环境中无法运行，本项目使用原生 Minecraft GUI（`GuiGraphics` + OpenGL）重新实现，UI 渲染骨架参考了 [YuZuUI-Forge](https://github.com/ming-sc/YuZuUI-Forge)。

## 功能

- **启动 Logo（BootLogoScreen）**：游戏加载完成后、首次进主界面之前播放发行商/开发商 logo（会话内一次，任意键跳过），布局还原原游戏 Boot.unity 场景
- **自定义~~启动~~（资源包加载）画面**：替换原版 Mojang 红白加载条，显示 BrandLogo 与 CompanyLogo
- **自定义标题画面**：
  - 背景图 ContentScale.Crop + 1.05×→1.0× 缩放动画（EaseOutQuad）+ 入场模糊（低分辨率离屏上采样）+ 全屏黑幕淡出
  - TitleOverlay 全屏画框淡入
  - TitleLogo 右上角显示（简中/日文自适应）
  - 5 个按钮横排于左下角（LoadGame / NewGame / Gallery / Options / Exit），交替 Y 偏移形成 zigzag
  - 无存档时 LoadGame 显示锁定态（原游戏 Locked 状态）
  - 右下角版本号
- **退出确认对话框**：点击 Exit 弹出 2 级菜单（压暗黑幕 + 亮色条带 + 装饰条 + 消息黑字 + 取消/结束按钮，布局还原 ProvidableDialog.prefab），确认后主界面 UI 随黑幕淡出并退出游戏
- **自定义背景音乐**：标题 BGM 由主界面自行管理（黑幕淡出时响起、子界面不中断、进世界停止、切语言重载后自动重播）；音效使用原游戏 Sfx_System_*_001 系统音效

## 按钮功能映射

| 按钮 | 行为 |
|------|------|
| LoadGame | 打开存档选择界面（`SelectWorldScreen`） |
| NewGame | 打开创建世界界面（`CreateWorldScreen`） |
| Gallery | 打开多人游戏界面（`JoinMultiplayerScreen`） |
| Options | 打开设置界面（`OptionsScreen`） |
| Exit | 打开退出确认对话框 → 确认后退出游戏 |

## 构建与运行

### 环境要求

- JDK 17
- Minecraft 1.20.1
- Forge 47.4.10

### 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/manosaba-1.0.0.jar`。

### 开发运行

```bash
./gradlew runClient
```

## 资源声明

**`src/main/resources/assets/manosaba/textures/gui/` 目录下的所有资源文件均来自游戏《魔法少女ノ魔女裁判》解包获得。**

**这些资源的版权全部归属于原开发商所有。**

本项目仅供学习和个人使用，不得用于任何商业用途。如有侵权，请联系删除。

## 许可证

本项目代码部分采用 [Apache-2.0](LICENSE) 许可证。

资源文件版权归原开发商所有，不在此许可证范围内。

## 致谢

- [Shiiyuko](https://github.com/Shiiyuko) — 原 Fabric 项目作者
- [YuZuUI-Forge](https://github.com/ming-sc/YuZuUI-Forge) — UI 渲染骨架参考
