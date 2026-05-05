import core.enviroment.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MapTest {
    static void main(String[] args) {
        WorldMap worldMap = new WorldMap(94033);

        exportToImage("my_world", worldMap);
    }

    public static void exportToImage(String filename, WorldMap worldMap) {
        int SIZE = 500;
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Terrain t = worldMap.getTile(x, y);
                Color color = t.getTerrainColor();
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
}
