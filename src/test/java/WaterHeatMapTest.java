import core.enviroment.WaterHeatMap;
import core.enviroment.WorldMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class WaterHeatMapTest {
    static void main(String[] args) {
        WorldMap worldMap = new WorldMap(940331988);
        WaterHeatMap waterHeatMap = new WaterHeatMap();

        exportToImage("my_world_heat_map", waterHeatMap);
    }

    public static void exportToImage(String filename, WaterHeatMap waterHeatMap) {
        int SIZE = 50;
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int t = waterHeatMap.getWaterHeatLevel(x, y);
                if (t >= 0) {
                    image.setRGB(x, y, new Color(0, 0, 255 - t * 10).getRGB());
                } else {
                    image.setRGB(x, y, new Color(255, 255, 255).getRGB());
                }
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
