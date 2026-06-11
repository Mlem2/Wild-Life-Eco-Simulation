package view.graphic.ui.controller;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Elephant;
import entities.Fish;
import entities.Rabbit;
import entities.Tiger;
import entities.Wolf;
import entities.base.Animals;
import entities.base.Entity;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GraphicRenderer {
    private final WorldMap worldMap;
    
    private Field fieldHunger;
    private Field fieldThirst;
    
    private Animals selectedAnimal;

    public GraphicRenderer(WorldMap worldMap) {
        this.worldMap = worldMap;
        
        try {
            fieldHunger = Animals.class.getDeclaredField("hunger");
            fieldThirst = Animals.class.getDeclaredField("thirst");
            fieldHunger.setAccessible(true);
            fieldThirst.setAccessible(true);
        } catch (Exception e) {
            System.err.println("Lỗi Reflection: " + e.getMessage());
        }
    }

    public void setSelectedAnimal(Animals selectedAnimal) { this.selectedAnimal = selectedAnimal; }

    public void renderEntities(Canvas canvas, double scale, double offsetX, double offsetY) {
    	System.out.println("renderEntities() called");

        GraphicsContext gc = canvas.getGraphicsContext2D();
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return;

        for (Chunk[] row : chunkMap) {
            for (Chunk chunk : row) {
                if (chunk == null) continue;
                synchronized (chunk.getEntityList()) {
                    for (Entity entity : chunk.getEntityList()) {
                        if (entity == null || !entity.checkAlive()) continue;

                        double screenX = offsetX + (entity.getX() * scale);
                        double screenY = offsetY + (entity.getY() * scale);
                        
                        // 1. Vẽ con vật
                        drawEntitySprite(gc, entity, screenX, screenY, scale);

                        // 2. Vẽ thanh trạng thái (Chỉ vẽ 1 lần duy nhất)
                        if (entity instanceof Animals) {
                            drawStatusBar(gc, (Animals) entity, screenX, screenY, scale);
                        }
                    }
                }
            }
        }
    }

    private void drawStatusBar(GraphicsContext gc, Animals animal, double x, double y, double size) {
        // Kích thước cố định, không phụ thuộc scale quá nhiều
        double barWidth = 16.0; 
        double barHeight = 3.0;

        // Căn giữa thanh trên đầu con vật
        double drawX = x + (size - barWidth) / 2;
        double drawY = y - 4;

        double h = Math.max(0, Math.min(100, animal.getHunger()));
        double t = Math.max(0, Math.min(100, animal.getThirst()));

        // Viền đen để nổi bật
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.strokeRect(drawX - 1, drawY - 1, barWidth + 2, barHeight * 2 + 4);

        // Thanh Hunger
        gc.setFill(h > 40 ? Color.GREEN : Color.RED);
        gc.fillRect(drawX, drawY, barWidth * (h / 100.0), barHeight);

        // Thanh Thirst
        gc.setFill(t > 30 ? Color.BLUE : Color.RED);
        gc.fillRect(drawX, drawY + barHeight + 2, barWidth * (t / 100.0), barHeight);
        
        System.out.println("Drawing bar for " + animal.getClass().getSimpleName());

    }


    private void drawEntitySprite(GraphicsContext gc, Entity e, double x, double y, double size) {
        String key = "animal_rabbit"; // Default
        if (e instanceof Rabbit) key = "animal_rabbit";
        else if (e instanceof Wolf) key = "animal_wolf";
        else if (e instanceof Tiger) key = "animal_tiger";
        else if (e instanceof Elephant) key = "animal_elephant";
        else if (e instanceof Fish) key = "animal_fish";
        
        var img = AssetManager.get(key);
        if (img != null) gc.drawImage(img, x, y, size, size);
    }
    
    public void render(Canvas canvas, double scale, double offsetX, double offsetY) {
        //clearCanvas(canvas);
        //renderTerrain(canvas);
        renderEntities(canvas, scale, offsetX, offsetY);
    }

}