package com.sokoban.util;

import com.sokoban.model.Level;
import com.sokoban.model.TileType;
import java.util.ArrayList;
import java.util.List;

public class LevelLoader {
    public static List<Level> loadAllLevels() {
        List<Level> levels = new ArrayList<>();

        // 关卡1
        Level l1 = new Level(5, 5, 1);
        int[][] d1 = {
                {1,1,1,1,1},
                {1,0,0,0,1},
                {1,4,2,3,1},
                {1,0,0,0,1},
                {1,1,1,1,1}
        };
        fill(l1, d1);
        levels.add(l1);

        // 关卡2
        Level l2 = new Level(5, 5, 2);
        int[][] d2 = {
                {1,1,1,1,1},
                {1,0,0,0,1},
                {1,4,2,0,1},
                {1,0,3,0,1},
                {1,1,1,1,1}
        };
        fill(l2, d2);
        levels.add(l2);

        // 关卡3
        Level l3 = new Level(7, 7, 3);
        int[][] d3 = {
                {1,1,1,1,1,1,1},
                {1,0,0,0,0,0,1},
                {1,4,2,0,3,0,1},
                {1,0,0,0,0,0,1},
                {1,0,2,0,3,0,1},
                {1,0,0,0,0,0,1},
                {1,1,1,1,1,1,1}
        };
        fill(l3, d3);
        levels.add(l3);

        return levels;
    }

    private static void fill(Level level, int[][] data) {
        for (int y = 0; y < data.length; y++) {
            for (int x = 0; x < data[0].length; x++) {
                level.setTile(x, y, TileType.fromValue(data[y][x]));
            }
        }
    }
}