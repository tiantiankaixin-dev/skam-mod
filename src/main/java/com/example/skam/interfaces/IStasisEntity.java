// 在 IStasisEntity.java 文件中

package com.example.skam.interfaces;

public interface IStasisEntity {
    // ... 已有的方法 ...
    int getStasisTicks();
    void setStasisTicks(int ticks);
    void setReleasedForLoyalty(boolean released);
    void resetDealtDamage();
    int getStasisLevel();

    // 【新增下面这个方法】
    /**
     * 将三叉戟的状态设置为空中飞行状态，
     * 主要用于解决它卡在地面时无法再次启动的问题。
     */
    void setAirborne();
    boolean isReleasedForLoyalty();
}
