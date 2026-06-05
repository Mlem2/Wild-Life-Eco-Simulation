package ui.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import core.enviroment.WorldMap;
import core.enviroment.Chunk;
import entities.base.Entity;
import entities.base.Animals;
import entities.Bush;
import entities.Trees;

import java.lang.reflect.Field;
import java.util.List;

import core.enviroment.Terrain;

public class MapController {

    @FXML 
    private Canvas mapCanvas;
    
    private GraphicsContext gc;
    private AnimationTimer uiRenderLoop;
    
    private WorldMap worldMap; 
    private int gridSize;

    @FXML
    public void initialize() {
        gc = mapCanvas.getGraphicsContext2D();
        initUIRenderLoop();
    }

    public void setWorldContext(WorldMap worldMap, int gridSize) {
        this.worldMap = worldMap;
        this.gridSize = gridSize;
    }

    private void initUIRenderLoop() {
        uiRenderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
            	
                if (worldMap == null || worldMap.chunkMap == null) return;
                renderTerrainMap();
                renderEntitiesFromChunks();
            }
        };
        uiRenderLoop.start();
    }

    private void renderTerrainMap() {
        double tileWidth = mapCanvas.getWidth() / gridSize;
        double tileHeight = mapCanvas.getHeight() / gridSize;

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                Terrain terrain = worldMap.getTile(x, y);
                if (terrain == null) continue;
                switch (terrain) {
                    case WATER:
                        gc.setFill(Color.web("#214066"));
                        break;
                    case MUD:
                        gc.setFill(Color.web("#543d2b"));
                        break;
                    case FOREST:
                        gc.setFill(Color.web("#193819"));
                        break;
                    case GRASSLAND:
                        gc.setFill(Color.web("#2c5e2c"));
                        break;
                    case MOUNTAIN:
                        gc.setFill(Color.web("#4f5254"));
                        break;
                    default:
                        gc.setFill(Color.BLACK);
                        break;
                }
                gc.fillRect(x * tileWidth, y * tileHeight, tileWidth + 0.5, tileHeight + 0.5); 
            }
        }
    }

    private void renderEntitiesFromChunks() {
        Chunk[][] chunkMap = worldMap.chunkMap;
        
        for (int cy = 0; cy < chunkMap.length; cy++) {
            for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                Chunk chunk = chunkMap[cy][cx];
                if (chunk == null) continue;

                List<Entity> entityList = chunk.getEntityList();
                
                synchronized (entityList) {
                    for (Entity entity : entityList) {
                        if (entity == null || !entity.checkAlive()) continue;

                        int x = getPrivateIntField(Entity.class, "x", entity);
                        int y = getPrivateIntField(Entity.class, "y", entity);

                        double pixelX = ((double) x / gridSize) * mapCanvas.getWidth();
                        double pixelY = ((double) y / gridSize) * mapCanvas.getHeight();

                        if (entity instanceof Animals) {
                            drawAnimal(entity, pixelX, pixelY);
                        } else if (entity instanceof Bush) {
                            gc.setFill(Color.web("#1e4a1e")); 
                            gc.fillOval(pixelX - 4, pixelY - 4, 8, 8);
                        } else if (entity instanceof Trees) {
                            gc.setFill(Color.web("#2d7a2d"));
                            gc.fillRect(pixelX - 6, pixelY - 6, 12, 12);
                        }
                    }
                }
            }
        }
    }

    private void drawAnimal(Entity animal, double px, double py) {
        String className = animal.getClass().getSimpleName().toLowerCase();
        double radius = 6.0;

        switch (className) {
            case "rabbit":
                gc.setFill(Color.WHITE);
                radius = 5.0;
                break;
            case "wolf":
                gc.setFill(Color.GRAY);
                radius = 7.0;
                break;
            case "tiger":
                gc.setFill(Color.ORANGE);
                radius = 8.0;
                break;
            case "deer":
                gc.setFill(Color.BROWN);
                radius = 7.5;
                break;
            case "elephant":
                gc.setFill(Color.CADETBLUE);
                radius = 12.0;
                break;
            default:
                gc.setFill(Color.LIGHTPINK);
                break;
        }

        gc.fillOval(px - radius, py - radius, radius * 2, radius * 2);
        
        // trạng thái
        try {
            Field fieldState = Animals.class.getDeclaredField("state");
            fieldState.setAccessible(true);
            Object stateObj = fieldState.get(animal);
            
            if (stateObj != null) {
                String stateStr = stateObj.toString().toLowerCase();
                switch (stateStr) {
                    case "running": gc.setFill(Color.YELLOW); break;
                    case "eating": gc.setFill(Color.GREEN); break;
                    case "hunting": gc.setFill(Color.RED); break;
                    case "sleeping": gc.setFill(Color.LIGHTBLUE); break;
                    default: gc.setFill(Color.TRANSPARENT); break;
                }
                // Cho 1 chấm thể hiện trạng thái ở giữa con vật
                gc.fillOval(px - 2, py - 2, 4, 4);
            }
        } catch (Exception ignored) {}
    }

    private int getPrivateIntField(Class<?> clazz, String fieldName, Object target) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (int) field.get(target);
        } catch (Exception e) {
            return 0;
        }
    }

    public void stopRender() {
        if (uiRenderLoop != null) uiRenderLoop.stop();
    }
}