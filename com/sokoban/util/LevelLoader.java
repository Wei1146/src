package com.sokoban.util;

import com.sokoban.model.TileType;
import com.sokoban.model.Level;

import java.util.*;

public class LevelLoader {

    // ==================== 固定关卡数据 ====================
    private static final String[][] LEVELS = {
            {
                    "#####",
                    "#@  #",
                    "# $ #",
                    "# . #",
                    "#####"
            },
            {
                    "#######",
                    "#@    #",
                    "#  $  #",
                    "#  .  #",
                    "#  $  #",
                    "#  .  #",
                    "#######"
            },
            {
                    "########",
                    "#@     #",
                    "#  $   #",
                    "#  .   #",
                    "#   $  #",
                    "#   .  #",
                    "#      #",
                    "########"
            }
    };

    // 星级阈值
    private static final int[] THREE_STAR_LIMITS = {5, 12, 18};
    private static final int[] TWO_STAR_LIMITS   = {8, 18, 25};

    // ==================== 固定关卡接口 ====================
    public static Level loadLevel(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= LEVELS.length) {
            throw new IllegalArgumentException("关卡索引无效: " + levelIndex);
        }

        String[] mapData = LEVELS[levelIndex];
        int height = mapData.length;
        int width = mapData[0].length();

        Level level = new Level(width, height);

        int playerX = -1, playerY = -1;

        for (int y = 0; y < height; y++) {
            String line = mapData[y];
            for (int x = 0; x < width; x++) {
                char ch = line.charAt(x);
                switch (ch) {
                    case '#':
                        level.setGround(x, y, TileType.WALL);
                        level.setEntity(x, y, null);
                        break;
                    case ' ':
                        level.setGround(x, y, TileType.FLOOR);
                        level.setEntity(x, y, null);
                        break;
                    case '.':
                        level.setGround(x, y, TileType.TARGET);
                        level.setEntity(x, y, null);
                        break;
                    case '$':
                        level.setGround(x, y, TileType.FLOOR);
                        level.setEntity(x, y, TileType.BOX);
                        break;
                    case '@':
                        level.setGround(x, y, TileType.FLOOR);
                        level.setEntity(x, y, TileType.PLAYER);
                        playerX = x;
                        playerY = y;
                        break;
                    case '*':
                        level.setGround(x, y, TileType.TARGET);
                        level.setEntity(x, y, TileType.BOX);
                        break;
                    case '+':
                        level.setGround(x, y, TileType.TARGET);
                        level.setEntity(x, y, TileType.PLAYER);
                        playerX = x;
                        playerY = y;
                        break;
                    default:
                        level.setGround(x, y, TileType.FLOOR);
                        level.setEntity(x, y, null);
                        break;
                }
            }
        }

        if (playerX == -1 || playerY == -1) {
            throw new IllegalStateException("关卡中未找到玩家起始位置");
        }

        return level;
    }

    public static int getLevelCount() {
        return LEVELS.length;
    }

    public static int getFixedLevelStars(int levelIndex, int moves) {
        if (levelIndex < 0 || levelIndex >= THREE_STAR_LIMITS.length) {
            return 0;
        }
        if (moves <= THREE_STAR_LIMITS[levelIndex]) return 3;
        if (moves <= TWO_STAR_LIMITS[levelIndex]) return 2;
        return 1;
    }

    public static int getRandomLevelStars(int boxCount, int moves) {
        int idealMoves = boxCount * 2 + 4;
        int threeStarMax = idealMoves + 2;
        int twoStarMax   = idealMoves + 5;
        if (moves <= threeStarMax) return 3;
        if (moves <= twoStarMax)   return 2;
        return 1;
    }

    // ==================== 随机生成关卡（增强版） ====================
    public static Level generateRandomLevel() {
        final int MAX_TRIES = 50;               // 增加尝试次数
        final int WIDTH = 11;
        final int HEIGHT = 11;
        final int MIN_BOXES = 1;
        final int MAX_BOXES = 3;

        Random rand = new Random();

        for (int attempt = 0; attempt < MAX_TRIES; attempt++) {
            Level level = new Level(WIDTH, HEIGHT);

            // 1. 初始化边界墙壁
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (x == 0 || x == WIDTH-1 || y == 0 || y == HEIGHT-1) {
                        level.setGround(x, y, TileType.WALL);
                        level.setEntity(x, y, null);
                    } else {
                        level.setGround(x, y, TileType.FLOOR);
                        level.setEntity(x, y, null);
                    }
                }
            }

            // 2. 随机添加内部墙壁（密度降低到 10%，避免过于拥挤）
            int wallCount = (WIDTH-2)*(HEIGHT-2) * 10 / 100;
            for (int i = 0; i < wallCount; i++) {
                int x = rand.nextInt(WIDTH-2) + 1;
                int y = rand.nextInt(HEIGHT-2) + 1;
                if (level.getGround(x, y) != TileType.WALL) {
                    level.setGround(x, y, TileType.WALL);
                }
            }

            // 3. 随机箱子数量
            int boxCount = rand.nextInt(MAX_BOXES - MIN_BOXES + 1) + MIN_BOXES;

            // 4. 收集所有空闲格子（非墙壁）
            List<Point> freeCells = new ArrayList<>();
            for (int y = 1; y < HEIGHT-1; y++) {
                for (int x = 1; x < WIDTH-1; x++) {
                    if (level.getGround(x, y) != TileType.WALL) {
                        freeCells.add(new Point(x, y));
                    }
                }
            }
            Collections.shuffle(freeCells, rand);
            if (freeCells.size() < 2 * boxCount + 1) continue;

            // 放置玩家
            Point playerPos = freeCells.remove(0);
            level.setEntity(playerPos.x, playerPos.y, TileType.PLAYER);

            // 放置箱子（要求箱子四周不能有墙壁，且至少有一个空位）
            List<Point> boxPositions = new ArrayList<>();
            boolean boxPlacementOk = true;
            for (int i = 0; i < boxCount; i++) {
                if (freeCells.isEmpty()) {
                    boxPlacementOk = false;
                    break;
                }
                Point p = freeCells.remove(0);
                // 新检查：箱子四周都不能是墙
                if (!hasNoAdjacentWall(level, p.x, p.y)) {
                    boxPlacementOk = false;
                    break;
                }
                level.setEntity(p.x, p.y, TileType.BOX);
                boxPositions.add(p);
            }
            if (!boxPlacementOk) continue;

            // 放置目标点
            for (int i = 0; i < boxCount; i++) {
                if (freeCells.isEmpty()) break;
                Point p = freeCells.remove(0);
                level.setGround(p.x, p.y, TileType.TARGET);
            }

            // 目标点数量必须等于箱子数
            if (countTargets(level) != boxCount) continue;

            // 5. 连通性检查：玩家能否走到所有箱子位置
            if (!isReachableToAllBoxes(level, playerPos, boxPositions)) {
                continue;
            }

            // 6. 箱子可推动性检查：每个箱子至少有一个可推动方向
            if (!areBoxesPushable(level, playerPos, boxPositions)) {
                continue;
            }

            return level;
        }

        // 保底关卡（可解，且箱子不贴墙）
        return createFallbackLevel();
    }

    /**
     * 检查箱子四周是否都没有墙（即不贴墙），并且至少有一个相邻格子是空地（地板或目标点且无实体）
     */
    private static boolean hasNoAdjacentWall(Level level, int x, int y) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        boolean hasEmptyAdjacent = false;
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            // 如果超出边界，视为墙（不可能，因为箱子不会在边界，但安全起见）
            if (nx < 0 || nx >= level.getWidth() || ny < 0 || ny >= level.getHeight()) {
                return false;
            }
            if (level.getGround(nx, ny) == TileType.WALL) {
                return false; // 有墙相邻，不合格
            }
            // 记录是否有空位（地板或目标点且无实体）
            if (level.getEntity(nx, ny) == null) {
                hasEmptyAdjacent = true;
            }
        }
        return hasEmptyAdjacent; // 四周无墙，且至少有一个空位
    }

    // 统计目标点数量
    private static int countTargets(Level level) {
        int count = 0;
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                if (level.getGround(x, y) == TileType.TARGET) {
                    count++;
                }
            }
        }
        return count;
    }

    // BFS 计算玩家可达区域
    private static boolean[][] computeReachableArea(Level level, Point start) {
        int w = level.getWidth();
        int h = level.getHeight();
        boolean[][] visited = new boolean[h][w];
        Queue<Point> queue = new LinkedList<>();
        queue.add(start);
        visited[start.y][start.x] = true;
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visited[ny][nx]) {
                    if (level.getGround(nx, ny) != TileType.WALL) {
                        visited[ny][nx] = true;
                        queue.add(new Point(nx, ny));
                    }
                }
            }
        }
        return visited;
    }

    // 检查玩家能否走到所有箱子位置（简单连通）
    private static boolean isReachableToAllBoxes(Level level, Point player, List<Point> boxes) {
        boolean[][] reachable = computeReachableArea(level, player);
        for (Point box : boxes) {
            if (!reachable[box.y][box.x]) {
                return false;
            }
        }
        return true;
    }

    // 检查每个箱子是否至少有一个可推动方向
    private static boolean areBoxesPushable(Level level, Point player, List<Point> boxes) {
        boolean[][] reachable = computeReachableArea(level, player);
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (Point box : boxes) {
            boolean pushable = false;
            for (int i = 0; i < 4; i++) {
                int pushX = box.x + dx[i];
                int pushY = box.y + dy[i];
                int backX = box.x - dx[i];
                int backY = box.y - dy[i];
                // 前方不是墙，且后方存在且玩家可达
                if (pushX >= 0 && pushX < level.getWidth() && pushY >= 0 && pushY < level.getHeight() &&
                        backX >= 0 && backX < level.getWidth() && backY >= 0 && backY < level.getHeight()) {
                    if (level.getGround(pushX, pushY) != TileType.WALL &&
                            (reachable[backY][backX] || (backX == player.x && backY == player.y))) {
                        pushable = true;
                        break;
                    }
                }
            }
            if (!pushable) {
                return false;
            }
        }
        return true;
    }

    // 保底关卡（简单可解，且箱子不贴墙）
    private static Level createFallbackLevel() {
        Level level = new Level(7, 7);
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (x == 0 || x == 6 || y == 0 || y == 6) {
                    level.setGround(x, y, TileType.WALL);
                } else {
                    level.setGround(x, y, TileType.FLOOR);
                }
            }
        }
        level.setEntity(1, 1, TileType.PLAYER);
        level.setEntity(2, 2, TileType.BOX);   // 箱子四周都是地板，不贴墙
        level.setGround(4, 4, TileType.TARGET);
        return level;
    }

    // 内部点类
    private static class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }
}