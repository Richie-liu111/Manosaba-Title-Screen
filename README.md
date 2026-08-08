# Manosaba Title Screen (Fabric 1.21.10)

一个 Minecraft Fabric 1.21.10 纯客户端模组，用于自定义标题屏幕和启动画面，灵感来源于游戏《魔法少女ノ魔女裁判》。

这是 [Manosaba-Title-Screen](https://github.com/Richie-liu111/Manosaba-Title-Screen) 仓库的 **Fabric 1.21.10** 分支，源码移植自同仓库 `1.20.1forge` 分支（v1.0.5）。渲染层基于 1.21.10 的 blaze3d GPU 流水线重写（`GuiGraphics.blit(RenderPipelines.GUI_TEXTURED, ...)`，不再使用已移除的 `RenderSystem.setShader*` 立即模式 API）。

## 功能

- **启动画面**：替换原版加载界面为发行商/开发商 logo（Acacia + REAER，布局还原原游戏 Boot.unity 场景，随加载进度淡入淡出）
- **自定义标题画面**：
  - 背景图 ContentScale.Crop + 1.05×→1.0× 缩放动画（EaseOutQuad，2700ms）+ 全屏黑幕淡出
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

## 与原版 v1.0.5（Forge 1.20.1）的差异

- **去掉配置功能**：固定 HIRO 背景，删除 ManosabaConfig / ManosabaConfigScreen
- **跳过入场模糊**：删除 640×360 FBO 离屏模糊，背景保留 EaseOutQuad 缩放动画
- **logo 只播一次**：高版本原版加载界面已原生显示开发者 logo，删除 `BootLogoScreen`，发行商/开发商 logo 仅在加载界面播放一次（详见 CHANGELOG）

## 构建与运行

### 环境要求

- JDK 21
- Minecraft 1.21.10
- Fabric Loader 0.19.3+ / Fabric API 0.138.4+

### 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/manosaba-1.0.5.jar`。

### 开发运行

```bash
./gradlew runClient
```

## 资源声明

**`src/main/resources/assets/manosaba/` 目录下的所有资源文件（贴图、音效、sounds.json）均来自游戏《魔法少女ノ魔女裁判》解包获得。**

**这些资源的版权全部归属于原开发商所有。**

本项目仅供学习和个人使用，不得用于任何商业用途。如有侵权，请联系删除。

## 许可证

本项目代码部分采用 [Apache-2.0](LICENSE) 许可证。

资源文件版权归原开发商所有，不在此许可证范围内。

## 致谢

- [Shiiyuko](https://github.com/Shiiyuko) — 原 Fabric 项目作者
- [ming-sc](https://github.com/ming-sc) — YuZuUI-Forge UI 渲染骨架参考
- [yiyuyan](https://github.com/yiyuyan) — [NeoYuZu-UI](https://github.com/yiyuyan/NeoYuZu-UI/) 1.21.9 Fabric 参考（1.21.9+ 输入 API 与渲染流水线）
