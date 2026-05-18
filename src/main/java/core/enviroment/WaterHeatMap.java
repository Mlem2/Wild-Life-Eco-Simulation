package core.enviroment;

import java.util.*;

public class WaterHeatMap {
    private int[][] waterHeatMap;

    public WaterHeatMap() {
        int sourceSize = 500;
        int targetSize = 50;
        waterHeatMap = new int[targetSize][targetSize];

        // 1. Initialize map with a value representing "unvisited" (-1)
        for (int[] row : waterHeatMap) Arrays.fill(row, -1);

        int blockSize = sourceSize / targetSize; // 10
        Queue<int[]> queue = new LinkedList<>();

        // 2. Identify all initial water sources (Clumps)
        for (int y = 0; y < targetSize; y++) {
            for (int x = 0; x < targetSize; x++) {
                boolean hasWater = false;

                // Scan the 10x10 block in the source map
                for (int i = 0; i < blockSize && !hasWater; i++) {
                    for (int j = 0; j < blockSize && !hasWater; j++) {
                        int sourceY = (y * blockSize) + i;
                        int sourceX = (x * blockSize) + j;

                        if (WorldMap.worldMap[sourceY][sourceX] == Terrain.WATER) {
                            hasWater = true;
                        }
                    }
                }

                if (hasWater) {
                    waterHeatMap[y][x] = 0; // Source distance is 0
                    queue.add(new int[]{x, y});
                }
            }
        }

        // 3. Multi-Source BFS to spread the "heat" (distance)
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int curX = current[0];
            int curY = current[1];

            for (int[] dir : directions) {
                int nextX = curX + dir[0];
                int nextY = curY + dir[1];

                // If within bounds and not yet visited
                if (nextX >= 0 && nextX < targetSize && nextY >= 0 && nextY < targetSize) {
                    if (waterHeatMap[nextY][nextX] == -1) {
                        waterHeatMap[nextY][nextX] = waterHeatMap[curY][curX] + 1;
                        queue.add(new int[]{nextX, nextY});
                    }
                }
            }
        }
    }

    public Integer getWaterHeatLevel(int x, int y) {
        // Return the distance value
        // Note: Lower values mean closer to water (higher "heat")
        return waterHeatMap[y][x];
    }
}