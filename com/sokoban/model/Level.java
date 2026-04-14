// 文件: src/com/sokoban/model/Level.java
package com.sokoban.model;

import java.util.Stack;

/**
 * 关卡数据模型
 * 管理地图、实体、玩家位置，支持移动、撤销、胜利判定
 */
public class Level {
    private int width;
    private int height;
    private TileType[][] ground;      // 地面类型（墙、地板、目标点）
    private TileType[][] entity;      // 实体类型（箱子、玩家、null）
    private int playerX, playerY;     // 玩家当前坐标
    private int moveCount;            // 当前移动步数

    // 历史记录栈，用于撤销
    private Stack<GameState> history;
    private boolean gameWin;           // 是否已胜利

    /**
     * 内部类，保存游戏状态快照
     */
    private static class GameState {
        TileType[][] entitySnapshot;
        int playerX, playerY;
        int moveCount;

        GameState(TileType[][] entity, int px, int py, int moves) {
            // 深拷贝实体数组
            this.entitySnapshot = new TileType[entity.length][entity[0].length];
            for (int i = 0; i < entity.length; i++) {
                System.arraycopy(entity[i], 0, this.entitySnapshot[i], 0, entity[i].length);
            }
            this.playerX = px;
            this.playerY = py;
            this.moveCount = moves;
        }
    }

    public Level(int width, int height) {
        this.width = width;
        this.height = height;
        this.ground = new TileType[height][width];
        this.entity = new TileType[height][width];
        this.history = new Stack<>();
        this.gameWin = false;
        this.moveCount = 0;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getMoveCount() { return moveCount; }
    public boolean isGameWin() { return gameWin; }

    /**
     * 获取指定位置显示的类型（用于绘制）
     */
    public TileType getDisplayTile(int x, int y) {
        if (ground[y][x] == TileType.WALL) {
            return TileType.WALL;
        }
        if (entity[y][x] == TileType.PLAYER) {
            return (ground[y][x] == TileType.TARGET) ? TileType.PLAYER_ON_TARGET : TileType.PLAYER;
        }
        if (entity[y][x] == TileType.BOX) {
            return (ground[y][x] == TileType.TARGET) ? TileType.BOX_ON_TARGET : TileType.BOX;
        }
        return ground[y][x]; // FLOOR or TARGET
    }

    /**
     * 设置地面类型
     */
    public void setGround(int x, int y, TileType type) {
        ground[y][x] = type;
    }

    /**
     * 设置实体类型
     */
    public void setEntity(int x, int y, TileType type) {
        entity[y][x] = type;
        if (type == TileType.PLAYER) {
            playerX = x;
            playerY = y;
        }
    }

    /**
     * 获取实体类型
     */
    public TileType getEntity(int x, int y) {
        return entity[y][x];
    }

    /**
     * 获取地面类型
     */
    public TileType getGround(int x, int y) {
        return ground[y][x];
    }

    /**
     * 保存当前状态到历史栈（用于撤销）
     */
    public void saveState() {
        history.push(new GameState(entity, playerX, playerY, moveCount));
    }

    /**
     * 撤销上一步操作
     */
    public boolean undo() {
        if (history.isEmpty() || gameWin) return false;
        GameState state = history.pop();
        // 恢复实体数组
        for (int i = 0; i < height; i++) {
            System.arraycopy(state.entitySnapshot[i], 0, entity[i], 0, width);
        }
        playerX = state.playerX;
        playerY = state.playerY;
        moveCount = state.moveCount;
        gameWin = false; // 撤销后清除胜利状态
        return true;
    }

    /**
     * 尝试移动玩家
     * @param dx x方向偏移
     * @param dy y方向偏移
     * @return 是否移动成功
     */
    public boolean movePlayer(int dx, int dy) {
        if (gameWin) return false;

        int newX = playerX + dx;
        int newY = playerY + dy;

        // 超出边界
        if (newX < 0 || newX >= width || newY < 0 || newY >= height) return false;

        // 下一个格子是墙壁
        if (ground[newY][newX] == TileType.WALL) return false;

        // 下一个格子有箱子
        if (entity[newY][newX] == TileType.BOX) {
            int pushX = newX + dx;
            int pushY = newY + dy;
            if (pushX < 0 || pushX >= width || pushY < 0 || pushY >= height) return false;
            // 箱子前方是墙壁或有其他箱子
            if (ground[pushY][pushX] == TileType.WALL) return false;
            if (entity[pushY][pushX] == TileType.BOX) return false;

            // 可以推动：保存当前状态，执行推动
            saveState();
            // 移动箱子
            entity[pushY][pushX] = TileType.BOX;
            // 原箱子位置变为玩家
            entity[newY][newX] = TileType.PLAYER;
            // 原玩家位置清空
            entity[playerY][playerX] = null;
            // 更新玩家坐标
            playerX = newX;
            playerY = newY;
            moveCount++;
            checkWin();
            return true;
        }

        // 下一个格子是空地或目标点且无实体
        if (entity[newY][newX] == null) {
            saveState();
            // 移动玩家
            entity[newY][newX] = TileType.PLAYER;
            entity[playerY][playerX] = null;
            playerX = newX;
            playerY = newY;
            moveCount++;
            checkWin();
            return true;
        }

        return false;
    }

    /**
     * 检查是否胜利（所有箱子都在目标点上）
     */
    private void checkWin() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (entity[y][x] == TileType.BOX && ground[y][x] != TileType.TARGET) {
                    gameWin = false;
                    return;
                }
            }
        }
        gameWin = true;
    }

    /**
     * 重置关卡到初始状态（需要外部提供初始实体和玩家坐标）
     */
    public void reset(TileType[][] initialEntity, int initPlayerX, int initPlayerY) {
        // 清空历史
        history.clear();
        // 重置实体
        for (int i = 0; i < height; i++) {
            System.arraycopy(initialEntity[i], 0, entity[i], 0, width);
        }
        playerX = initPlayerX;
        playerY = initPlayerY;
        moveCount = 0;
        gameWin = false;
    }

    /**
     * 获取实体数组的副本（用于重置）
     */
    public TileType[][] getEntityCopy() {
        TileType[][] copy = new TileType[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(entity[i], 0, copy[i], 0, width);
        }
        return copy;
    }
}