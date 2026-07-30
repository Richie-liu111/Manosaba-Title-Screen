package me.shiiyuko.manosaba.gui;

/**
 * 虚拟画布：将设计空间（如 2560×1440）的坐标等比映射到实际屏幕坐标，
 * 自动处理 letterbox 偏移。
 */
public class VirtualScreen {
    private int virtualWidth;
    private int virtualHeight;
    private int practicalWidth;
    private int practicalHeight;
    private int currentX;
    private int currentY;

    public VirtualScreen() {
    }

    public VirtualScreen(int virtualWidth, int virtualHeight) {
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
    }

    public float toPracticalX(float x) {
        return currentX + x * (float) practicalWidth / (float) virtualWidth;
    }

    public float toPracticalY(float y) {
        return currentY + y * (float) practicalHeight / (float) virtualHeight;
    }

    public float toPracticalWidth(float width) {
        return width * (float) practicalWidth / (float) virtualWidth;
    }

    public float toPracticalHeight(float height) {
        return height * (float) practicalHeight / (float) virtualHeight;
    }

    public float toVirtualX(float x) {
        return (x - currentX) * (float) virtualWidth / (float) practicalWidth;
    }

    public float toVirtualY(float y) {
        return (y - currentY) * (float) virtualHeight / (float) practicalHeight;
    }

    public int getVirtualWidth() { return virtualWidth; }
    public void setVirtualWidth(int w) { this.virtualWidth = w; }
    public int getVirtualHeight() { return virtualHeight; }
    public void setVirtualHeight(int h) { this.virtualHeight = h; }
    public int getPracticalWidth() { return practicalWidth; }
    public void setPracticalWidth(int w) { this.practicalWidth = w; }
    public int getPracticalHeight() { return practicalHeight; }
    public void setPracticalHeight(int h) { this.practicalHeight = h; }
    public int getCurrentX() { return currentX; }
    public void setCurrentX(int x) { this.currentX = x; }
    public int getCurrentY() { return currentY; }
    public void setCurrentY(int y) { this.currentY = y; }
}
