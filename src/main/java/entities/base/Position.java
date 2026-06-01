package entities.base;

import core.enviroment.WorldMap;
import java.util.Objects;

public class Position {
    private static Position[][] GRID;

    public static void initializeGrid(int size) {
        GRID = new Position[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                GRID[y][x] = new Position(x, y);
            }
        }
    }

    public static Position of(int x, int y) {
        if (GRID != null && x >= 0 && x < GRID.length && y >= 0 && y < GRID.length) {
            return GRID[y][x];
        }
        return new Position(x, y);
    }

    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{" + "x=" + x + ", y=" + y + '}';
    }
}
