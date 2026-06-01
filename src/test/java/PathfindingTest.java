import brain.pathfinder.Pathfinder;
import core.enviroment.Terrain;
import core.enviroment.WorldMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class PathfindingTest {
    public static void main(String[] args) {
        WorldMap worldMap = new WorldMap(9403312, 500);
        Pathfinder pathfinder = new Pathfinder(worldMap);

        long startTime = System.nanoTime();

        Point start = new Point(155, 350);
        Point end = new Point(100, 450);
        java.util.List<Point> path = new java.util.ArrayList<>();
        pathfinder.calculatePath(start, end, path);

        long endTime = System.nanoTime();

        exportToImage("path_finding", worldMap, path);

        // 3. Calculate the difference and convert nanoseconds to milliseconds
        long durationNano = endTime - startTime;
        double durationMilli = durationNano / 1_000_000.0;

        System.out.println("----------------------------------------");
        System.out.printf("Algorithm Execution Time: %.3f ms%n", durationMilli);
        System.out.println("----------------------------------------");
    }


    public static void exportToImage(String filename, WorldMap worldMap, List<Point> path) {
        int SIZE = 500;
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        // 1. Draw the base terrain map
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Terrain t = worldMap.getTile(x, y);
                Color color = t.getTerrainColor();
                image.setRGB(x, y, color.getRGB());
            }
        }

        // 2. Overlay the path in black
        if (path != null) {
            int blackRGB = Color.BLACK.getRGB();
            for (Point p : path) {
                // Safety check: ensure coordinates fit inside the image bounds
                if (p.x >= 0 && p.x < SIZE && p.y >= 0 && p.y < SIZE) {
                    image.setRGB(p.x, p.y, blackRGB);
                }
            }
        }

        // 3. Save the image file
        try {
            File outputFile = new File(filename + ".png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Map saved as " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
