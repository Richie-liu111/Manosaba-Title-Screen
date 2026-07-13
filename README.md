# Manosaba Title Screen — 存档分支

> ⚠️ **此分支已归档，不再维护。**
> 
> 这是 Manosaba Title Screen 模组的 Kotlin + Unity 图集方案原型，包含三个已知 Bug，已被后续重写取代。详情见下方说明。

## 分支状态

| 项目 | 状态 |
|------|------|
| 语言 | Kotlin + Java |
| 架构 | UnitySpriteParser + GuiGraphics.blit() 从图集抠图 |
| 构建 | ✅ 编译通过 |
| 运行 | ⚠️ 有 3 个 Bug，见下 |
| 维护 | ❌ **已停止** |

## 已知 Bug（未修复）

1. **按钮全叠在一起** — 所有按钮渲染在 x=24 同一位置，点击总触发 LoadGame（[原因](ARCHIVE.md#bug-1按钮全叠在一起最高优先级)）
2. **标题覆盖层无淡入** — TitleOverlay 立即满透明度渲染，背景被完全遮挡（[原因](ARCHIVE.md#bug-2标题覆盖层缺少淡入动画)）
3. **Exit 对话框未写** — 空的 lambda，点击无反应（[原因](ARCHIVE.md#bug-3exit-对话框未实现)）

完整 Bug 记录见 [ARCHIVE.md](ARCHIVE.md)。

## 为何放弃此分支

### 1. 原项目本身不可用

此移植基于 [Shiiyuko/Manosaba-Title-Screen](https://github.com/Shiiyuko/Manosaba-Title-Screen)（Fabric 1.21.4 + Compose Desktop）。该原项目[从未能成功运行](ARCHIVE.md#第一阶段compose-desktop失败)：
- 依赖 `androidx.compose.runtime.SnapshotStateKt`，该运行时在 Minecraft 环境中不存在
- 项目没有 gradlew、没有 release，甚至无法自行构建
- 非官方 fork 的 release JAR 同样因缺失 Compose 运行时类而崩溃

因此，本项目最初的实现（保留原项目的 UnitySpriteParser + Compose 式布局）是建立在一个不可行的前提之上的，继续修复的成本高于重写。

### 2. 架构重写

开发过程中，我们参考了 [YuZuUI-Forge](https://github.com/) / SenrenBanka-UI 的渲染骨架（`VirtualScreen` + `Layer` + `TitleScreenButton` + `RenderUtils`），该架构更适合 Minecraft 原生 GUI 环境。重写带来的变化：

| 方面 | 此分支（已放弃） | 新分支 |
|------|-----------------|--------|
| 语言 | Kotlin | Java |
| 纹理 | Unity 图集（`ui_title.png` + JSON 解析） | 独立 PNG，每张直接渲染 |
| 渲染 | `UnitySpriteParser` 动态抠图 | `VirtualScreen` + `Layer` 管线 |
| 动画 | 手动计算 | `AnimationFunction` 缓动系统 |
| 按钮布局 | Bug（x 未累加） | 正常（cursorX 累加） |
| 淡入动画 | 缺失 | 背景缩放 + UI 延迟淡入 |
| Exit 行为 | 空 Lambda | 直接 `mc.stop()` |
| 音效 | 无 | 按钮点击音效 |

### 3. 活跃分支

本项目的当前开发已迁移到以下分支：

- **`1.21.1neoforge`** — NeoForge 1.21.1 版（Java + VirtualScreen 架构，推荐使用）
- **`1.20.1forge`** — Forge 1.20.1 版（相同架构，Forge 环境下使用）

### 4. 资源文件

此分支中的资源文件（`src/main/resources/assets/` 下的 PNG、OGG、JSON、OTF）已迁移到新分支，未做修改。**如果需要这些资源文件，请切换到 `1.21.1neoforge` 分支获取最新版本。**

## 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/Manosaba-NeoForge-1.21.1-1.0.0-all.jar`。

## 资源声明

**`src/main/resources/assets/` 目录下的所有资源文件均来自游戏《魔法少女ノ魔女裁判》解包获得。**

**这些资源的版权全部归属于原开发商所有。**

本项目仅供学习和个人使用，不得用于任何商业用途。如有侵权，请联系删除。

## 许可证

本项目代码部分采用 [Apache-2.0](LICENSE) 许可证。

资源文件版权归原开发商所有，不在此许可证范围内。
