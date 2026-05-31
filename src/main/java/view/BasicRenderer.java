package view;

import java.lang.reflect.Field;
import java.util.List;

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
    private final int MAP_SIZE = 500;
    private Animals selectedAnimal;
    private Chunk selectedChunk;

    public BasicRenderer(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    public void setSelectedAnimal(Animals selectedAnimal) {
        this.selectedAnimal = selectedAnimal;
    }

    public void setSelectedChunk(Chunk selectedChunk) {
        this.selectedChunk = selectedChunk;
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

        Chunk[][] chunkMap = null;
        try {
            Field field = WorldMap.class.getDeclaredField("chunkMap");
            field.setAccessible(true);
            chunkMap = (Chunk[][]) field.get(worldMap);
        } catch (Exception e) {}

        if (chunkMap == null) return;

        // Đổ màu vẽ sinh vật dạng ô lưới chuẩn 1 pixel tương thích
        int startChunkX = startX / 50;
        int startChunkY = startY / 50;
        int endChunkX = Math.min(chunkMap[0].length - 1, endX / 50);
        int endChunkY = Math.min(chunkMap.length - 1, endY / 50);

        for (int cy = startChunkY; cy <= endChunkY; cy++) {
            for (int cx = startChunkX; cx <= endChunkX; cx++) {
                Chunk chunk = chunkMap[cy][cx];
                if (chunk == null) continue;

                if (selectedChunk == chunk) {
                    double chunkScreenX = offsetX + (cx * 50 * tileW);
                    double chunkScreenY = offsetY + (cy * 50 * tileH);
                    gc.setStroke(Color.CYAN);
                    gc.setLineWidth(Math.max(1.2, scale * 0.35));
                    gc.strokeRect(chunkScreenX, chunkScreenY, 50 * tileW, 50 * tileH);
                    gc.setLineWidth(1.0);
                }

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

                        if (selectedAnimal != null && selectedAnimal == entity) {
                            gc.setStroke(Color.YELLOW);
                            gc.setLineWidth(Math.max(1.0, scale * 0.35));
                            gc.strokeRect(screenX - 1.5, screenY - 1.5, finalSize + 3, finalSize + 3);
                            gc.setLineWidth(1.0);
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