# SYNC.md — 跨分支同步指南

Manosaba 的 6 个分支是 **同一个 GitHub 仓库的不同分支**，各自独立演进。
没有 submodule，没有共享目录，也没有构建期的同步机制。

这份文档回答一个问题：**我在一支上改了东西，其余几支要不要跟着改？**

配套工具：`drift-report.sh`——**只读脚本，放在工作区根目录，故意不入库**，
因为它硬编码了本机 5 个工作副本的绝对路径（提交到公开仓库属于无谓的信息泄露，
且对别人毫无用处）。下文提到的 `drift-report.sh` 都指这个脚本。

---

## 1. 分支与本地工作副本

| 远端分支 | 本地目录 | MC | 加载器 | 状态 |
|---|---|---|---|---|
| `1.20.1forge` ⭐默认 | `dev/forge1.20.1/` | 1.20.1 | Forge (FG6) | v1.0.5，主分支 |
| `1.21.1neoforge` | `dev/neoforge1.21.1/` | 1.21.1 | NeoForge | v1.0.5 |
| `1.7.10gtnh` | `1.7.10gtnh/` | 1.7.10 | Forge (GTNH) | v1.0.5 |
| `1.12.2forge` | `1.12.2forge/` | 1.12.2 | Forge (RFG) | v1.0.5 |
| `1.21.10fabric` | `fabric-example-mod-1.21.10/` | 1.21.10 | Fabric | v1.0.5 |
| `mc/1.21.1-neoforge` | `Manosaba-Title-Screen/` | 1.21.1 | NeoForge + Kotlin | 已归档，不再维护 |

另有 `fabric-example-mod-1.21.11/`——**不是 git 仓库，远端也没有对应分支**，
目前是纯本地目录。要纳入同步体系得先决定怎么版本化。

跑 `drift-report.sh` 可以随时打印这张表的实时状态（HEAD + 是否干净）。

---

## 2. 哪些文件必须跨支同步

### 引擎核心（`me.shiiyuko.manosaba.gui` / `.function` / `.utils`）

| 文件 | 是否应逐字一致 | 说明 |
|---|---|---|
| `AnimationFunction.java` | ✅ 是 | 6 行函数式接口，全 5 支当前完全一致 |
| `VirtualScreen.java` | ✅ 基本是 | 坐标换算纯数学，只有 1.7.10 因 API 差异不同 |
| `Layer.java` | ⚠️ 逻辑同步 | 渲染后端不同，但**动画时序 / 碰撞 / alpha 数学**必须同步 |
| `TitleScreenButton.java` | ⚠️ 逻辑同步 | 同上；各支都有独立交互适配 |
| `RenderUtils.java` | ❌ 各支重写 | 就是渲染后端的抽象层，天然不同 |
| `ManosabaTitleScreen.java` | ❌ 各支重写 | 布局常量应同步，渲染与事件代码不同 |

### 其余需要留意的共享内容

- `TextureConst`（各支包位置相同）——纹理路径表，新增素材时 5 支都要加
- `assets/manosaba/**`——纹理与音效，各支是副本，改素材要 5 支一起换
- `CHANGELOG.md`——**各支独立**，记录本支的移植差异

### 修一个 bug 之后

1. 在**默认分支 `1.20.1forge`** 上改
2. 跑 `drift-report.sh`，看这个文件的 hash 在哪些支上原本是相同的
3. 原来相同的那些支 → 必须跟着改成相同的（否则就是新引入的漂移）
4. 各支的 `CHANGELOG.md` 补一条
5. 各支 push（CI 会编译验证，见第 4 节）

---

## 3. 本来就该不同的地方（不要"修"）

### 路径差异 —— 同一个类在不同支位置不同

`drift-report.sh` 按 **basename** 查找，就是因为这个：

| 类 | `1.20.1forge` / `1.21.1neoforge` / `1.21.10fabric` | `1.7.10gtnh` | `1.12.2forge` |
|---|---|---|---|
| `RenderUtils` | `manosaba/utils/` | `manosaba/gui/` | `manosaba/gui/` |
| `ManosabaTitleScreen` | `manosaba/gui/screen/` | `manosaba/gui/` | `manosaba/gui/screen/` |
| 其余引擎类 | `manosaba/gui/` | 同 | 同 |

源码根也不同：Fabric 用 `src/client/java/`（`splitEnvironmentSourceSets()`），
其余四支用 `src/main/java/`。

### 渲染后端

| 分支 | 后端 |
|---|---|
| `1.7.10gtnh` / `1.12.2forge` | `Tessellator` + `GL11`（LWJGL 2 立即模式） |
| `1.20.1forge` / `1.21.1neoforge` / `1.21.10fabric` | `GuiGraphics.blit()` / `RenderPipelines` |

### 已知的功能差异（有意为之，不是漂移）

| 功能 | `1.20.1` | `1.21.1` | `1.7.10` | `1.12.2` | `1.21.10` |
|---|---|---|---|---|---|
| 入场模糊（640×360 FBO） | ✅ | ✅ | ✅ | ❌ | ❌ |
| 配置界面 | Forge `ModConfigSpec` | NeoForge `ModConfigSpec` | `ManosabaConfigGuiFactory` | `ManosabaConfigGuiFactory` | Gson + Mod Menu |
| 配置文件 | `config/manosaba-client.toml` | 同 | `config/manosaba.cfg` | 同 | `config/manosaba.json` |

**`Screen.renderBackground` 的调用约定在 1.21.9 反转了**——这是一个反复踩到的坑：

- ≤ 1.21.1：`Screen.render()` 的实现**必须自己**调 `renderBackground(...)`
- ≥ 1.21.9：框架在 `renderWithTooltipAndSubtitles` 里**已经替你调过**，
  实现里再调一次 = `IllegalStateException: Can only blur once per frame`（直接崩游）

`ManosabaConfigScreen` 在 Forge/NeoForge 与 Fabric 之间的 `render()` 因此**不能逐字照抄**。

**`GuiScreen` 时代：覆写 `drawScreen` 不等于禁用原版按钮**（2026-10-07 在 1.12.2 修掉）：

`1.12.2forge` 的 `ManosabaTitleScreen extends GuiMainMenu`，`initGui()` 调 `super.initGui()`
会往 `buttonList` 塞入原版的单人/多人/选项/退出/语言按钮。它覆写了 `drawScreen` 且不调
`super.drawScreen()` → 按钮**看不见**；但 `GuiScreen` 的交互是按 `buttonList` 分发的，
`mouseClicked` 末尾的 `super.mouseClicked()` 照样遍历 → 点到空白处**会误触发原版动作**
（键盘 Tab 也能聚焦到这些隐形按钮）。

**修法：`super.initGui()` 之后加 `this.buttonList.clear();`**。只去掉
`super.mouseClicked()` 是不够的——拦不住键盘路径。

`1.7.10gtnh` 没这个问题，因为它 `extends GuiScreen` 且 `initGui()` 不调 super。
移植到任何 `GuiScreen` 时代的版本时都检查一遍这一点。

---

## 4. CI

| 分支 | workflow | JDK |
|---|---|---|
| `1.20.1forge` | `.github/workflows/build.yml` | 17（toolchain 17） |
| `1.21.1neoforge` | `.github/workflows/build.yml` | 21（toolchain 21） |
| `1.7.10gtnh` | `.github/workflows/build.yml` | 17（Gradle 用；compile toolchain 由 GTNH 插件配） |
| `1.12.2forge` | `.github/workflows/build.yml` | 17（Gradle 用；compile toolchain 8 / Azul 16 靠 foojay 自动下载） |
| `1.21.10fabric` | `.github/workflows/build.yml` | 25（Fabric MDK 模板原样） |

四支的 toolchain 自动下载都靠 `settings.gradle` 里的
`org.gradle.toolchains.foojay-resolver-convention`；1.12.2forge 的构建还指定了
**Azul JDK 16**（`javaCompiler`）和 toolchain 8，CI 首次构建会下载这两个 JDK，比较慢。

---

## 5. 当前漂移快照

`drift-report.sh` 在 2026-10-07 的输出摘要（归一化后 hash，同 hash = 逻辑一致）：

| 文件 | 逻辑一致的分组 |
|---|---|
| `AnimationFunction.java` | **全部 5 支一致** ✅ |
| `VirtualScreen.java` | forge / 1.12.2 / fabric / neoforge 一致；1.7.10 独立 |
| `Layer.java` | 只有 `1.20.1forge` + `1.21.1neoforge` 一致 |
| `TitleScreenButton.java` | **5 支全不同** |
| `RenderUtils.java` | **5 支全不同**（预期内） |
| `ManosabaTitleScreen.java` | **5 支全不同**（预期内） |

`Layer.java` 值得看一眼：它是"逻辑应该同步"的文件，但只有两支一致——
说明第一批次的 `Long` 拆箱修复虽然推到了 5 支，其余历史改动没有一起带过去。
