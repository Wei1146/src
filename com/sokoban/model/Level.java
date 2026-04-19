package com.sokoban.model;

import java.awt.Point;

public class Level {
    private int width, height;
    private TileType[][] map;
    private Point playerPos;
    private int boxCount;
    private int boxesOnTarget;
    private int levelNumber;

    public Level(int width, int height, int levelNumber) {
        this.width = width;
        this.height = height;
        this.levelNumber = levelNumber;
        map = new TileType[height][width];
    }

    public void setTile(int x, int y, TileType type) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        map[y][x] = type;
        // 注意：这里不自动更新 playerPos，避免干扰
        if (type == TileType.BOX) boxCount++;
        else if (type == TileType.PLAYER) playerPos = new Point(x, y);
        else if (type == TileType.BOX_ON_TARGET) { boxCount++; boxesOnTarget++; }
        else if (type == TileType.PLAYER_ON_TARGET) playerPos = new Point(x, y);
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return TileType.WALL;
        return map[y][x];
    }

    public boolean move(int dx, int dy) {
        int oldX = playerPos.x;
        int oldY = playerPos.y;
        int newX = oldX + dx;
        int newY = oldY + dy;
        if (newX < 0 || newX >= width || newY < 0 || newY >= height) return false;
        TileType target = getTile(newX, newY);
        if (target == TileType.WALL) return false;

        // 推箱子
        if (target == TileType.BOX || target == TileType.BOX_ON_TARGET) {
            int pushX = newX + dx;
            int pushY = newY + dy;
            if (pushX < 0 || pushX >= width || pushY < 0 || pushY >= height) return false;
            TileType pushTile = getTile(pushX, pushY);
            if (pushTile == TileType.WALL || pushTile == TileType.BOX || pushTile == TileType.BOX_ON_TARGET)
                return false;

            // 移动箱子
            boolean pushIsTarget = (pushTile == TileType.TARGET);
            if (pushIsTarget) {
                map[pushY][pushX] = TileType.BOX_ON_TARGET;
                boxesOnTarget++;
            } else {
                map[pushY][pushX] = TileType.BOX;
            }

            // 玩家移动到箱子原位置
            boolean boxWasOnTarget = (target == TileType.BOX_ON_TARGET);
            map[newY][newX] = boxWasOnTarget ? TileType.PLAYER_ON_TARGET : TileType.PLAYER;

            // 清除玩家原位置
            boolean playerWasOnTarget = (map[oldY][oldX] == TileType.PLAYER_ON_TARGET);
            map[oldY][oldX] = playerWasOnTarget ? TileType.TARGET : TileType.FLOOR;

            // 更新玩家坐标
            playerPos = new Point(newX, newY);
            return true;
        }

        // 普通移动
        boolean playerWasOnTarget = (map[oldY][oldX] == TileType.PLAYER_ON_TARGET);
        boolean newIsTarget = (target == TileType.TARGET);
        map[oldY][oldX] = playerWasOnTarget ? TileType.TARGET : TileType.FLOOR;
        map[newY][newX] = newIsTarget ? TileType.PLAYER_ON_TARGET : TileType.PLAYER;
        playerPos = new Point(newX, newY);
        return true;
    }

    public boolean isWin() { return boxesOnTarget == boxCount; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Point getPlayerPos() { return playerPos; }
    public int getLevelNumber() { return levelNumber; }
}