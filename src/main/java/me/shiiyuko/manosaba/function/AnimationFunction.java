package me.shiiyuko.manosaba.function;

/**
 * 缓动动画函数接口：根据时间进度 (0..1) 和当前值计算下一帧的值。
 */
@FunctionalInterface
public interface AnimationFunction<T> {

    /**
     * @param t   归一化时间进度，0 到 1
     * @param now 当前状态值
     * @return 动画后的状态值
     */
    T apply(float t, T now);
}
