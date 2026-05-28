package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import core.enviroment.WorldMap;
import core.enviroment.Chunk;
import entities.base.Entity;
import entities.base.Animals;
import entities.Bush;
import entities.Trees;
import entities.Rabbit;
import entities.Fish;
import entities.Wolf;
import entities.Tiger;
import entities.Elephant;

import java.lang.reflect.Field;
import java.util.List;

public class BasicRenderer {
    private final WorldMap worldMap;
    private final int MAP_SIZE = 500;

    public BasicRenderer(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    public void render(Canvas canvas, double scale, double offsetX, double offsetY) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasW = canvas.getWidth();
        double canvasH = canvas.getHeight();

        gc.clearRect(0, 0, canvasW, canvasH);

        double tileW = 1.0 * scale;
        double tileH = 1.0 * scale;

        int startX = (int) Math.max(0, -offsetX / tileW);
        int startY = (int) Math.max(0, -offsetY / tileH);
        int endX = (int) Math.min(MAP_SIZE, (canvasW - offsetX) / tileW + 1);
        int endY = (int) Math.min(MAP_SIZE, (canvasH - offsetY) / tileH + 1);

        // Vẽ nền đất địa hình ma trận
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                java.awt.Color awtColor = worldMap.getTile(x, y).getTerrainColor();
                Color fxColor = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
                gc.setFill(fxColor);
                gc.fillRect(offsetX + (x * tileW), offsetY + (y * tileH), tileW + 0.4, tileH + 0.4);
            }
        }

        // Đổ màu vẽ sinh vật dạng ô lưới chuẩn 1 pixel tương thích
        int startChunkX = startX / 50;
        int startChunkY = startY / 50;
        int endChunkX = Math.min(9, endX / 50);
        int endChunkY = Math.min(9, endY / 50);

        Chunk[][] chunkMap = null;
        try {
            Field field = WorldMap.class.getDeclaredField("chunkMap");
            field.setAccessible(true);
            chunkMap = (Chunk[][]) field.get(worldMap);
        } catch (Exception e) {}

        if (chunkMap == null) return;

        for (int cy = startChunkY; cy <= endChunkY; cy++) {
            for (int cx = startChunkX; cx <= endChunkX; cx++) {
                Chunk chunk = chunkMap[cy][cx];
                if (chunk == null) continue;

                List<Entity> entityList = chunk.getEntityList();
                synchronized (entityList) {
                    for (Entity entity : entityList) {
                        if (entity == null || !entity.checkAlive()) continue;

                        double screenX = offsetX + (entity.getX() * tileW);
                        double screenY = offsetY + (entity.getY() * tileH);
                        double finalSize = tileW;

                        // ĐỊNH HÌNH PHÂN LOẠI ĐỔ MÀU ĐẦY ĐỦ 100% CÁC LOÀI TRÊN Ô LƯỚI
                        if (entity instanceof Bush) {
                            gc.setFill(Color.LIGHTGREEN);
                            gc.fillRect(screenX, screenY, finalSize, finalSize);
                        }
                        else if (entity instanceof Trees) {
                            gc.setFill(Color.DARKGREEN);
                            gc.fillRect(screenX, screenY, finalSize, finalSize);
                        }
                        // Thỏ: Tròn Trắng
                        else if (entity instanceof Rabbit) {
                            gc.setFill(Color.WHITE);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }
                        // Cá: Tròn Xanh Ngọc dưới nước
                        else if (entity instanceof Fish) {
                            gc.setFill(Color.AQUAMARINE);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }
                        // Sói: Tròn Đỏ dữ tợn
                        else if (entity instanceof Wolf) {
                            gc.setFill(Color.RED);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }
                        // Hổ: Tròn Cam viền Đen cảnh báo
                        else if (entity instanceof Tiger) {
                            gc.setFill(Color.ORANGE);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(Math.max(0.3, scale * 0.1));
                            gc.strokeOval(screenX, screenY, finalSize, finalSize);
                        }
                        // Voi: Tròn Xám Khổng Lồ
                        else if (entity instanceof Elephant) {
                            gc.setFill(Color.DARKGRAY);
                            gc.fillOval(screenX, screenY, finalSize, finalSize);
                        }

                        // Vẽ thanh Đói Khát Kép (khi đủ độ phóng to)
                        if (entity instanceof Animals && scale >= 4.0) {
                            Animals animal = (Animals) entity;
                            try {
                                Field fH = Animals.class.getDeclaredField("hunger");
                                Field fT = Animals.class.getDeclaredField("thirst");
                                fH.setAccessible(true); fT.setAccessible(true);

                                double h = (double) fH.get(animal);
                                double t = (double) fT.get(animal);
                                double bH = Math.max(1.5, scale * 0.15);

                                // Thanh đói tầng trên
                                gc.setFill(h > 40.0 ? Color.GREEN : Color.RED);
                                gc.fillRect(screenX, screenY - (bH * 2) - 2.0, finalSize * (h / 100.0), bH);
                                // Thanh khát tầng dưới
                                gc.setFill(t > 30.0 ? Color.BLUE : Color.RED);
                                gc.fillRect(screenX, screenY - bH - 1.0, finalSize * (t / 100.0), bH);
                            } catch (Exception e) {}
                        }
                    }
                }
            }
        }
    }
}