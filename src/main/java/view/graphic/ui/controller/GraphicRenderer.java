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
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double tileW = scale;
        double tileH = scale;
        
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return;

        for (Chunk[] row : chunkMap) {
            for (Chunk chunk : row) {
                if (chunk == null) continue;
                synchronized (chunk.getEntityList()) {
                    for (Entity entity : chunk.getEntityList()) {
                        if (entity == null || !entity.checkAlive()) continue;

                        double screenX = offsetX + (entity.getX() * tileW);
                        double screenY = offsetY + (entity.getY() * tileH);
                        
                        // 1. Vẽ con vật (Sử dụng AssetManager như cũ)
                        drawEntitySprite(gc, entity, screenX, screenY, tileW);

                        // 2. VẼ THANH ĐÓI KHÁT (Đã tích hợp từ BasicRenderer)
                        if (entity instanceof Animals && fieldHunger != null && fieldThirst != null) {
                            try {
                                Animals animal = (Animals) entity;
                                double h = (double) fieldHunger.get(animal);
                                double t = (double) fieldThirst.get(animal);
                                double bH = Math.max(1.5, tileW * 0.15);

                                gc.setFill(h > 40.0 ? Color.GREEN : Color.RED);
                                gc.fillRect(screenX, screenY - (bH * 2) - 2.0, tileW * (h / 100.0), bH);

                                gc.setFill(t > 30.0 ? Color.BLUE : Color.RED);
                                gc.fillRect(screenX, screenY - bH - 1.0, tileW * (t / 100.0), bH);
                            } catch (Exception e) { /* Bỏ qua lỗi */ }
                        }
                    }
                }
            }
        }
    }

    private void drawStatusBar(GraphicsContext gc, Animals animal, double x, double y, double size) {
        double barWidth = size;
        double barHeight = 4.0; // Độ dày cố định để dễ nhìn
        double padding = 2.0;

        // Lấy giá trị (Đảm bảo giá trị trả về nằm trong khoảng 0-100)
        double h = Math.max(0, Math.min(100, animal.getHunger()));
        double t = Math.max(0, Math.min(100, animal.getThirst()));

        // Vẽ khung nền đen (Outline) để chắc chắn thấy được vị trí
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0);
        gc.strokeRect(x, y - 15, barWidth, barHeight * 2 + 2);

        // Vẽ thanh Hunger (Xanh lá)
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y - 15, barWidth * (h / 100.0), barHeight);

        // Vẽ thanh Thirst (Xanh dương)
        gc.setFill(Color.BLUE);
        gc.fillRect(x, y - 10, barWidth * (t / 100.0), barHeight);
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
}