package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import core.enviroment.*;

public class MapViewer extends Application {

    private static final int GRID_SIZE = 500; // The resolution of your data
    private WorldMap worldMap;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private final int baseTileSize = 1;
    private static final double ZOOM_INTENSITY = 0.1;
    private double lastMouseX, lastMouseY;

    @Override
    public void start(Stage primaryStage) {
        worldMap = new WorldMap(94033111, 500);

        // 1. Create a Resizable Canvas
        Canvas canvas = new Canvas();
        StackPane root = new StackPane(canvas);

        // === MISSING PIECE 1: Bind canvas size to the window ===
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // === MISSING PIECE 2: Redraw when window size changes ===
        canvas.widthProperty().addListener(obs -> drawMap(canvas));
        canvas.heightProperty().addListener(obs -> drawMap(canvas));

        canvas.setOnScroll(event -> {
            double delta = event.getDeltaY();
            double zoomFactor = (delta > 0) ? 1.1 : 0.9;

            double oldScale = scale;
            double newScale = scale * zoomFactor;

            // Calculate the scale that makes the map exactly fit the window
            double minScaleX = canvas.getWidth() / (GRID_SIZE * baseTileSize);
            double minScaleY = canvas.getHeight() / (GRID_SIZE * baseTileSize);
            double minScale = Math.max(minScaleX, minScaleY);

            // Max scale (e.g., 50.0) prevents zooming into a single pixel's atoms
            double maxScale = 50.0;

            // Apply the limit
            newScale = Math.max(minScale, Math.min(maxScale, newScale));

            // If the scale didn't actually change (we hit a limit), don't update offsets
            if (newScale == oldScale) return;

            scale = newScale;

            // Update offsets to zoom on mouse point
            offsetX = event.getX() - (event.getX() - offsetX) * (scale / oldScale);
            offsetY = event.getY() - (event.getY() - offsetY) * (scale / oldScale);

            // Re-clamp offsets after zoom to ensure no white space appeared
            double mapWidth = GRID_SIZE * (baseTileSize * scale);
            double mapHeight = GRID_SIZE * (baseTileSize * scale);
            offsetX = Math.min(0, Math.max(canvas.getWidth() - mapWidth, offsetX));
            offsetY = Math.min(0, Math.max(canvas.getHeight() - mapHeight, offsetY));

            drawMap(canvas);
        });
        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;

            double mapWidth = GRID_SIZE * (baseTileSize * scale);
            double mapHeight = GRID_SIZE * (baseTileSize * scale);

            // Update offsets with boundaries
            // offsetX should never be > 0 (map starts right of screen)
            // offsetX should never be < (windowWidth - mapWidth) (map ends left of screen)
            offsetX = Math.min(0, Math.max(canvas.getWidth() - mapWidth, offsetX + dx));
            offsetY = Math.min(0, Math.max(canvas.getHeight() - mapHeight, offsetY + dy));

            lastMouseX = e.getX();
            lastMouseY = e.getY();
            drawMap(canvas);
        });

        Scene scene = new Scene(root, 800, 800);
        primaryStage.setTitle("Map Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        drawMap(canvas);

    }

    private void drawMap(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // 1. Clear everything
        gc.clearRect(0, 0, width, height);

        // 2. Base tile size (How big a tile is if scale = 1.0)
        // Use a fixed base size rather than canvas-dependent size
        // to make zooming more predictable.
        double baseTileSize = 1.0;
        double tileW = baseTileSize * scale;
        double tileH = baseTileSize * scale;

        // 3. Optimization: Calculate which indices are actually visible
        // This prevents the loop from running 250,000 times if only 100 tiles are visible.
        int startX = (int) Math.max(0, -offsetX / tileW);
        int startY = (int) Math.max(0, -offsetY / tileH);
        int endX = (int) Math.min(GRID_SIZE, (width - offsetX) / tileW + 1);
        int endY = (int) Math.min(GRID_SIZE, (height - offsetY) / tileH + 1);

        // 4. Draw only the visible range
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                double screenX = offsetX + (x * tileW);
                double screenY = offsetY + (y * tileH);

                gc.setFill(getColorJavaFxValue(worldMap.getTile(x, y).getTerrainColor()));

                // +0.5 to width/height removes the "grid line" artifacts
                gc.fillRect(screenX, screenY, tileW + 0.5, tileH + 0.5);
            }
        }
    }
    private Color getColorJavaFxValue(java.awt.Color value) {
        return Color.rgb(value.getRed(), value.getGreen(), value.getBlue());
    }

    public static void main(String[] args) {
        launch(args);
    }
}