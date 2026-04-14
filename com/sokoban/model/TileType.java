// 文件: src/com/sokoban/model/TileType.java
package com.sokoban.model;

/**
 * 地图格子类型枚举
 * 用于表示静态地面类型和动态实体类型
 */
public enum TileType {
    WALL,       // 墙壁
    FLOOR,      // 空地
    TARGET,     // 目标点
    BOX,        // 箱子
    PLAYER,     // 玩家
    BOX_ON_TARGET,   // 箱子在目标点上（用于绘制，实际模型由ground+entity表示）
    PLAYER_ON_TARGET; // 玩家在目标点上（用于绘制）
}