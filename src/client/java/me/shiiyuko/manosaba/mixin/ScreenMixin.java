package me.shiiyuko.manosaba.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * 暴露 {@link Screen} 的私有组件列表（renderables 供自定义渲染循环按序遍历）。
 */
@Mixin(Screen.class)
public interface ScreenMixin {

    @Accessor("renderables")
    List<Renderable> getRenderables();

    @Accessor("children")
    List<GuiEventListener> getChildren();

    @Accessor("narratables")
    List<NarratableEntry> getNarratableEntries();
}
