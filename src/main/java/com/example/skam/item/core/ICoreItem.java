package com.example.skam.item.core;


/**
 * 所有核心物品都应实现此接口。
 * 它提供了获取核心类型和等级的标准方法。
 */
public interface ICoreItem {
    CoreType getCoreType();
    int getLevel();


}
