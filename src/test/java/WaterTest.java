import core.enviroment.WorldMap;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.lang.reflect.Field;

public class WaterTest {

    public static void main(String[] args) {
        System.out.println("Initializing WorldMap generation...");

        // 1. Initialize world map with a random seed
        int testSeed = (int) (Math.random() * 100000);
        WorldMap map = new WorldMap(940331);

        // 2. Generate the heatmap data
        map.generateWaterHeatMap();

        try {
            // 3. Use reflection to extract the private waterHeatMap array
            Field heatMapField = WorldMap.class.getDeclaredField("waterHeatMap");
            heatMapField.setAccessible(true);
            int[][] waterHeatMap = (int[][]) heatMapField.get(null);

            int chunkCount = waterHeatMap.length;    // Should be 10 (500 / 50)
            int renderScale = 50;                    // Scale chunks back up to 500x500 pixels
            int imgSize = chunkCount * renderScale;

            BufferedImage image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // 4. Find the maximum distance in the array to normalize colors dynamically
            int maxDistance = 1;
            for (int[] row : waterHeatMap) {
                for (int dist : row) {
                    if (dist > maxDistance) maxDistance = dist;
                }
            }

            System.out.println("Processing heatmap values. Max distance found: " + maxDistance);

            // 5. Render the chunks to the graphic buffer
            for (int y = 0; y < chunkCount; y++) {
                for (int x = 0; x < chunkCount; x++) {
                    int distance = waterHeatMap[y][x];
                    Color chunkColor;

                    if (distance == 0) {
                        // Source Water Chunks
                        chunkColor = new Color(30, 144, 255); // Dodger Blue
                    } else if (distance == -1) {
                        // Error/Unreachable Chunk
                        chunkColor = new Color(220, 20, 60);  // Crimson Red
                    } else {
                        // Land Gradient: Closer chunks are bright green, distant are dark
                        float factor = 1.0f - ((float) distance / maxDistance);
                        // Bound it safely between 0.15 (dark green) and 0.9 (bright green)
                        float greenIntensity = 0.15f + (factor * 0.75f);
                        chunkColor = new Color(0, (int)(greenIntensity * 255), 0);
                    }

                    // Draw the scaled block
                    g2d.setColor(chunkColor);
                    g2d.fillRect(x * renderScale, y * renderScale, renderScale, renderScale);

                    // Optional: Draw a subtle grid outline around chunks
                    g2d.setColor(new Color(255, 255, 255, 30));
                    g2d.drawRect(x * renderScale, y * renderScale, renderScale, renderScale);
                }
            }

            g2d.dispose();

            // 6. Save image to the project root directory
            File outputFile = new File("water_heatmap_test.png");
            ImageIO.write(image, "png", outputFile);

            System.out.println("=================================================");
            System.out.println(" SUCCESS: Heatmap image exported successfully! ");
            System.out.println(" Location: " + outputFile.getAbsolutePath());
            System.out.println("=================================================");

        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Reflection Error: Could not access private field 'waterHeatMap'. Check spelling.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during rendering.");
            e.printStackTrace();
        }
    }
}