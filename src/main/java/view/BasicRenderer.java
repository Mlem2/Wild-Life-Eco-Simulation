package view;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Bush;
import entities.Elephant;
import entities.Fish;
import entities.Rabbit;
import entities.Tiger;
import entities.Trees;
import entities.Wolf;
import entities.base.Animals;
import entities.base.Entity;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BasicRenderer {
    private final WorldMap worldMap;
    private Animals selectedAnimal;
    private Chunk selectedChunk;

    // Cache to prevent recreating JavaFX Color objects from AWT colors
    private final Map<java.awt.Color, Color> colorCache = new HashMap<>();

    // Cache the reflection fields once so we don't look them up inside loops
    private Field fieldHunger;
    private Field fieldThirst;

    public BasicRenderer(WorldMap worldMap) {
        this.worldMap = worldMap;

        // Pre-initialize and cache your reflection lookup fields
        try {
            fieldHunger = Animals.class.getDeclaredField("hunger");
            fieldThirst = Animals.class.getDeclaredField("thirst");
            fieldHunger.setAccessible(true);
            fieldThirst.setAccessible(true);
        } catch (Exception e) {
            System.err.println("Failed to cache Animal reflection fields: " + e.getMessage());
        }
    }

    public void setSelectedAnimal(Animals selectedAnimal) {
        this.selectedAnimal = selectedAnimal;
    }

    public void setSelectedChunk(Chunk selectedChunk) {
        this.selectedChunk = selectedChunk;
    }

    private Color getCachedFxColor(java.awt.Color awtColor) {
        if (awtColor == null) return Color.BLACK;
        // Compute if absent avoids object allocation if the color key already exists
        return colorCache.computeIfAbsent(awtColor, c -> Color.rgb(c.getRed(), c.getGreen(), c.getBlue()));
    }

    public void renderTerrain(Canvas canvas, double scale, double offsetX, double offsetY) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();

        double tileW = scale;
        double tileH = scale;

        int startX = (int) Math.max(0, -offsetX / tileW);
        int startY = (int) Math.max(0, -offsetY / tileH);
        int endX = (int) Math.min(WorldMap.SIZE, (canvasW - offsetX) / tileW + 1);
        int endY = (int) Math.min(WorldMap.SIZE, (canvasH - offsetY) / tileH + 1);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                java.awt.Color awtColor = worldMap.getTile(x, y).getTerrainColor();

                // CRITICAL FIX: Pull a reusable reference instead of allocating a 'new Color()'
                Color fxColor = getCachedFxColor(awtColor);

                gc.setFill(fxColor);
                gc.fillRect(offsetX + (x * tileW), offsetY + (y * tileH), tileW + 0.4, tileH + 0.4);
            }
        }
    }

    public void renderEntities(Canvas canvas, double scale, double offsetX, double offsetY) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();

        double tileW = scale;
        double tileH = scale;

        int startX = (int) Math.max(0, -offsetX / tileW);
        int startY = (int) Math.max(0, -offsetY / tileH);
        int endX = (int) Math.min(WorldMap.SIZE, (canvasW - offsetX) / tileW + 1);
        int endY = (int) Math.min(WorldMap.SIZE, (canvasH - offsetY) / tileH + 1);

        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return;

        int startChunkX = startX / WorldMap.CHUNK_SIZE;
        int startChunkY = startY / WorldMap.CHUNK_SIZE;
        int endChunkX = Math.min(chunkMap[0].length - 1, endX / WorldMap.CHUNK_SIZE);
        int endChunkY = Math.min(chunkMap.length - 1, endY / WorldMap.CHUNK_SIZE);

        for (int cy = startChunkY; cy <= endChunkY; cy++) {
            for (int cx = startChunkX; cx <= endChunkX; cx++) {
                Chunk chunk = chunkMap[cy][cx];
                if (chunk == null) continue;

                if (selectedChunk == chunk) {
                    double chunkScreenX = offsetX + (cx * WorldMap.CHUNK_SIZE * tileW);
                    double chunkScreenY = offsetY + (cy * WorldMap.CHUNK_SIZE * tileH);
                    gc.setStroke(Color.CYAN);
                    gc.setLineWidth(Math.max(1.2, scale * 0.35));
                    gc.strokeRect(chunkScreenX, chunkScreenY, WorldMap.CHUNK_SIZE * tileW, WorldMap.CHUNK_SIZE * tileH);
                    gc.setLineWidth(1.0);
                }

                List<Entity> entityList = chunk.getEntityList();
                synchronized (entityList) {
                    for (Entity entity : entityList) {
                        if (entity == null || !entity.checkAlive()) continue;

                        double screenX = offsetX + (entity.getX() * tileW);
                        double screenY = offsetY + (entity.getY() * tileH);
                        double finalSize = tileW;

                        if (entity instanceof Bush) {
                            gc.setFill(Color.LIGHTGREEN);
                            gc.fillRect(screenX, screenY, finalSize, finalSize);
                        }
                        else if (entity instanceof Trees) {
                            gc.setFill(Color.GREEN);
                            gc.fillRect(screenX, screenY, finalSize, finalSize);
                        }
                        else if (entity instanceof Rabbit) {
                            gc.setFill(Color.WHITE);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }
                        else if (entity instanceof Fish) {
                            gc.setFill(Color.AQUAMARINE);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }
                        else if (entity instanceof Wolf) {
                            gc.setFill(Color.RED);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }
                        else if (entity instanceof Tiger) {
                            gc.setFill(Color.ORANGE);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(Math.max(0.3, scale * 0.1));
                            gc.strokeOval(screenX, screenY, finalSize, finalSize);
                        }
                        else if (entity instanceof Elephant) {
                            gc.setFill(Color.DARKGRAY);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }

                        if (selectedAnimal != null && selectedAnimal == entity) {
                            gc.setStroke(Color.YELLOW);
                            gc.setLineWidth(Math.max(1.0, scale * 0.35));
                            gc.strokeRect(screenX - 1.5, screenY - 1.5, finalSize + 3, finalSize + 3);
                            gc.setLineWidth(1.0);
                        }

                        // Optimized status bar render
                        if (entity instanceof Animals && scale >= 4.0 && fieldHunger != null && fieldThirst != null) {
                            Animals animal = (Animals) entity;
                            try {
                                // CRITICAL FIX: Reusing pre-cached field lookups
                                double h = (double) fieldHunger.get(animal);
                                double t = (double) fieldThirst.get(animal);
                                double bH = Math.max(1.5, scale * 0.15);

                                gc.setFill(h > 40.0 ? Color.GREEN : Color.RED);
                                gc.fillRect(screenX, screenY - (bH * 2) - 2.0, finalSize * (h / 100.0), bH);

                                gc.setFill(t > 30.0 ? Color.BLUE : Color.RED);
                                gc.fillRect(screenX, screenY - bH - 1.0, finalSize * (t / 100.0), bH);
                            } catch (Exception e) { /* Ignored */ }
                        }
                    }
                }
            }
        }
    }

    public void render(Canvas canvas, double scale, double offsetX, double offsetY) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderTerrain(canvas, scale, offsetX, offsetY);
        renderEntities(canvas, scale, offsetX, offsetY);
    }
}