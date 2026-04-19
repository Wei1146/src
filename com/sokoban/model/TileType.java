package com.sokoban.model;

public enum TileType {
    WALL(1),
    FLOOR(0),
    BOX(2),
    TARGET(3),
    PLAYER(4),
    BOX_ON_TARGET(5),
    PLAYER_ON_TARGET(6);

    private final int value;
    TileType(int value) { this.value = value; }
    public int getValue() { return value; }
    public static TileType fromValue(int value) {
        for (TileType t : values()) {
            if (t.value == value) return t;
        }
        return FLOOR;
    }
}