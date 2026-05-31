import core.enviroment.*;
import entities.base.EntityMap;
import entities.base.Entity;
import entities.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MapTest {
    public static void main(String[] args) {
        int SIZE = 500;

        // 1. Initialize the terrain map
        WorldMap worldMap = new WorldMap(9403312, 500);

        // 2. Pass it to EntityMap to distribute entities based on your spawn weights
        EntityMap entityMap = new EntityMap(worldMap, 500);

        // 3. Export the combined maps to an image
        exportToImage("my_world_with_entities", worldMap, entityMap, SIZE);
    }

    public static void exportToImage(String filename, WorldMap worldMap, EntityMap entityMap, int SIZE) {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        // Retrieve the internal 2D array from your EntityMap
        // Note: Make sure entityMap has a getter method `getEntityMap()` returning Entity[][]
        // If it's private and unreadable, add a public getter to EntityMap or use a public helper method.
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                // Default back to the underlying terrain color
                Terrain t = worldMap.getTile(x, y);
                Color color = t.getTerrainColor();

                // If an entity exists at these coordinates, overwrite the color
                Entity entity = entityMap.getEntity(y, x);
                if (entity != null) {
                    color = getEntityColor(entity);
                }

                image.setRGB(x, y, color.getRGB());
            }
        }

        try {
            File outputFile = new File(filename + ".png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Map saved as " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines the visual color representation for each entity type.
     */
    private static Color getEntityColor(Entity entity) {
        return switch (entity) {
            case Wolf w -> Color.RED;           // Aggressive/Predator
            case Rabbit r -> Color.PINK;        // Soft/Small prey
            case Elephant e -> Color.GRAY;      // Large mammal
            case Fish f -> Color.CYAN;          // Contrast against dark blue water
            case Trees t -> new Color(34, 139, 34);  // Forest Green
            case Bush b -> new Color(124, 252, 0);   // Lawn/Bright Green
            default -> Color.MAGENTA;           // Fallback for unexpected entities
        };
    }
}