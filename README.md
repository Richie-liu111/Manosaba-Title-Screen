# Manosaba Title Screen — GTNH 1.7.10

使用《魔法少女ノ魔女裁判》自定义标题画面替换 Minecraft 标题界面。

基于 [YuZuUI-GTNH](https://github.com/paulzzh/YuZuUI-GTNH) 的构建架构，移植自 [Manosaba-Title-Screen (Forge 1.20.1)](https://github.com/Richie-liu111/Manosaba-Title-Screen/tree/1.20.1forge)。

## 依赖

- [UniMixins](https://github.com/LegacyModdingMC/UniMixins)
- [GTNHLib](https://github.com/GTNewHorizons/GTNHLib)

两者均由 GTNH Convention Plugin 自动包含，无需手动安装。

## 构建

```bash
# 需要 JDK 21（Gradle 8.8 要求）
./gradlew build
# 输出：build/libs/manosaba-*.jar
```

`./gradlew runClient` 启动的是**裸 Forge 1.7.10**（非完整 GTNH 整合包）。模组在裸 Forge 下正常工作，但音效系统可能不稳定（LWJGL 2 ARM64 OpenAL 兼容性问题）。推荐测试方式：

```bash
./gradlew build  # 构建 JAR
# 将 build/libs/manosaba-*.jar 放入 HMCL 的 GTNH 实例 mods/ 文件夹
# 启动 GTNH 整合包测试
```

## 功能

- **2560×1440 虚拟画布**：基于原游戏 CanvasScaler ReferenceResolution，所有 UI 元素使用精确坐标
- **背景缩放动画**：2:1 纹理 ContentScale.Crop 到 16:9，1.1× → 1.0× 呼吸效果
- **TitleOverlay + TitleLogo**：画框延迟淡入，Logo 随语言自适应（简中=中文 Logo，其余=日文）
- **5 个按钮**：原游戏 anchoredPosition 精确还原，Hover 高亮 + 中文标签叠加
- **按钮中文标签**：悬停时显示原游戏 Label@ZhHans 精灵（仅简中语言）
- **背景角色切换**：希罗 / 艾玛，游戏内配置即时生效
- **自定义背景音乐**：原游戏标题曲（GTNH 整合包中由 GTNH 音乐系统接管）

## 配置

通过游戏内 Mod Options 或编辑 `config/manosaba.cfg`：

| 选项 | 说明 | 默认值 |
|---|---|---|
| `useEmaBackground` | 使用艾玛背景（关闭则为希罗） | false |
| `bgm` | 背景音乐开关 | true |
| `justExit` | 退出直接关闭游戏（关闭则回到原版主菜单） | false |

## 架构差异（vs Forge 1.20.1）

| | GTNH 1.7.10 | Forge 1.20.1 |
|---|---|---|
| 构建系统 | GTNH Convention Plugin + RetroFuturaGradle | ForgeGradle + MDK |
| Mixin 加载 | UniMixins (IEarlyMixinLoader CoreMod) | 内建 MixinBootstrap |
| 配置系统 | GTNHLib @Config 注解 | ForgeConfigSpec |
| 渲染管线 | GL11 + Tessellator 直接绘制 | GuiGraphics + RenderSystem |
| GUI 基类 | GuiScreen | TitleScreen |
| 音效注册 | sounds.json | DeferredRegister\<SoundEvent\> |
| 模组元数据 | mcmod.info | neoforge.mods.toml / mods.toml |
| 屏幕替换 | @Inject RETURN + 单例 | @ModifyVariable + @Redirect |

## 致谢

- 原始 Fabric 模组：[Shiiyuko](https://github.com/Shiiyuko)
- Forge / NeoForge / GTNH 移植：[Richie](https://github.com/Richie-liu111)
- GTNH 1.7.10 构建基于 [YuZuUI-GTNH](https://github.com/paulzzh/YuZuUI-GTNH) by Paulzzh
- YuZuUI 参考：[ming-sc](https://github.com/ming-sc/YuZuUI-Forge)
- 原始游戏素材：《魔法少女ノ魔女裁判》

## 许可证

Apache-2.0
