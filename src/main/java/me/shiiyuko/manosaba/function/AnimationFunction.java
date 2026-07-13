package me.shiiyuko.manosaba.function;

/**
 * 动画函数接口：时间 t（0..1）和当前值 → 动画后的值。
 * 来自 YuZuUI 的 AnimationFunction。
 */
@FunctionalInterface
public interface AnimationFunction<T> {
    T apply(float t, T now);
}