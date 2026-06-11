package view.graphic.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import core.enviroment.WorldMap;

public class MapController {

    @FXML
    private Canvas mapCanvas;

    private GraphicsContext gc;
    private WorldMap worldMap;
    private int gridSize = 100;
    private boolean isContextSet = false;

    private double scale = 2.5;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private double lastMouseX;
    private double lastMouseY;

    private final double MIN_SCALE = 0.2;
    private final double MAX_SCALE = 10.0;

    private long lastUpdateTime = 0;
    private final long RENDER_DELAY_NS = 16_666_666L; // 🌟 Tối ưu lên ~60 FPS chuẩn game thay vì bị delay dài

    private AnimationTimer renderLoop;

    public static String SELECTED_TOOL = "NONE";

    @FXML
    public void initialize() {
        if (mapCanvas != null) {
            gc = mapCanvas.getGraphicsContext2D();
            mapCanvas.setOnScroll(this::handleScroll);
            mapCanvas.setOnMousePressed(this::handleMousePressed);
            mapCanvas.setOnMouseDragged(this::handleMouseDragged);
        }

        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isContextSet || worldMap == null) return;
                if (now - lastUpdateTime >= RENDER_DELAY_NS) {
                    renderMap();
                    lastUpdateTime = now;
                }
            }
        };
        renderLoop.start();
    }

    @FXML
    void onMapClicked(MouseEvent event) {
        if (event.isStillSincePress() == false) return;
        if (!isContextSet || worldMap == null) return;

        final double tileSize = 32.0;

        double worldX = (event.getX() - offsetX) / scale;
        double worldY = (event.getY() - offsetY) / scale;

        int gridX = (int) Math.floor(worldX / tileSize);
        int gridY = (int) Math.floor(worldY / tileSize);

        if (gridX < 0 || gridX >= gridSize || gridY < 0 || gridY >= gridSize) {
            return;
        }

        if (SELECTED_TOOL == null || SELECTED_TOOL.equals("NONE")) {
            return;
        }

        entities.base.Entity newEntity = null;

        switch (SELECTED_TOOL) {
            case "PLANT_GRASS":
                try {
                    var tile = worldMap.getTile(gridX, gridY);
                    if (tile != null) {
                        java.lang.reflect.Method setGrassMethod = tile.getClass().getMethod("setGrass", boolean.class);
                        setGrassMethod.invoke(tile, true);
                    }
                } catch (Exception ignored) {}
                break;
            case "ANIMAL_RABBIT":
                newEntity = new entities.Rabbit(gridX, gridY);
                break;
            case "ANIMAL_WOLF":
                newEntity = new entities.Wolf(gridX, gridY);
                break;
            case "ANIMAL_TIGER":
                newEntity = new entities.Tiger(gridX, gridY);
                break;
            case "ANIMAL_ELEPHANT":
                newEntity = new entities.Elephant(gridX, gridY);
                break;
            case "OBSTACLE_ROCK":
                newEntity = new entities.Bush(gridX, gridY);
                break;
        }

        if (newEntity != null) {
            try {
                int chunkX = gridX / core.enviroment.WorldMap.CHUNK_SIZE;
                int chunkY = gridY / core.enviroment.WorldMap.CHUNK_SIZE;

                var chunk = worldMap.chunkMap[chunkY][chunkX];
                if (chunk != null) {
                    chunk.addEntity(newEntity);
                    if (newEntity instanceof entities.base.Animals && view.MapViewer.instance != null) {
                        var simManager = view.MapViewer.instance.getSharedSimulationManager();
                        if (simManager != null) {
                            simManager.registerBrainForEntity(newEntity);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi sinh thực thể: " + e.getMessage());
            }
        }
    }

    private void handleScroll(ScrollEvent event) {
        double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
        double oldScale = scale;
        scale = scale * zoomFactor;
        if (scale < MIN_SCALE) scale = MIN_SCALE;
        if (scale > MAX_SCALE) scale = MAX_SCALE;

        double mouseX = event.getX();
        double mouseY = event.getY();
        offsetX = mouseX - (mouseX - offsetX) * (scale / oldScale);
        offsetY = mouseY - (mouseY - offsetY) * (scale / oldScale);
    }

    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getX() - lastMouseX;
        double deltaY = event.getY() - lastMouseY;
        offsetX += deltaX;
        offsetY += deltaY;
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    public void setWorldContext(core.enviroment.WorldMap worldMap, int gridSize) {
        if (worldMap == null) return;
        this.worldMap = worldMap;
        this.gridSize = (gridSize > 0) ? gridSize : 100;

        if (mapCanvas != null) {
            this.gc = mapCanvas.getGraphicsContext2D();
        }

        AssetManager.loadAssets();
        this.scale = 2.5;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
        this.isContextSet = true;
    }

    public void setWorldMapContext(core.enviroment.WorldMap worldMap, int gridSize) {
        this.setWorldContext(worldMap, gridSize);
    }

    public void shutdownTimeline() {
        this.isContextSet = false;
        this.worldMap = null;
        if (gc != null && mapCanvas != null) {
            gc.save();
            gc.setTransform(1, 0, 0, 1, 0, 0);
            gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
            gc.restore();
        }
    }

    private void renderMap() {
        if (mapCanvas == null || gc == null || worldMap == null) return;

        double canvasWidth = mapCanvas.getWidth();
        double canvasHeight = mapCanvas.getHeight();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(scale, scale);

        final double tileSize = 32.0;

        Image grassImg = AssetManager.get("tile_grass");
        Image waterImg = AssetManager.get("tile_water");
        Image stoneImg = AssetManager.get("stone");
        Image dirtImg = AssetManager.get("tile_dirt");
        Image forestImg = AssetManager.get("forest");

        // 🌟 BƯỚC CẢI TIẾN 1: Vẽ nền địa hình bản đồ (Giữ nguyên tối ưu quét biên màn hình)
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                double dx = x * tileSize;
                double dy = y * tileSize;

                double screenX = dx * scale + offsetX;
                double screenY = dy * scale + offsetY;
                if (screenX + tileSize * scale < 0 || screenX > canvasWidth ||
                        screenY + tileSize * scale < 0 || screenY > canvasHeight) {
                    continue;
                }

                boolean drawn = false;
                try {
                    core.enviroment.Terrain terrain = worldMap.getTile(x, y);
                    if (terrain != null) {
                        String terrainName = terrain.name().toUpperCase();

                        if (terrainName.contains("WATER")) {
                            if (waterImg != null) {
                                gc.drawImage(waterImg, dx, dy, tileSize, tileSize);
                                drawn = true;
                            }
                        } else if (terrainName.contains("MOUNTAIN")) {
                            if (grassImg != null) gc.drawImage(grassImg, dx, dy, tileSize, tileSize);
                            if (stoneImg != null) gc.drawImage(stoneImg, dx, dy, tileSize, tileSize);
                            drawn = true;
                        } else if (terrainName.contains("MUD")) {
                            if (grassImg != null) gc.drawImage(grassImg, dx, dy, tileSize, tileSize);
                            if (dirtImg != null) gc.drawImage(dirtImg, dx, dy, tileSize, tileSize);
                            drawn = true;
                        } else if (terrainName.contains("FOREST")) {
                            if (grassImg != null) gc.drawImage(grassImg, dx, dy, tileSize, tileSize);
                            if (forestImg != null) gc.drawImage(forestImg, dx, dy, tileSize, tileSize);
                            drawn = true;
                        } else {
                            if (grassImg != null) {
                                gc.drawImage(grassImg, dx, dy, tileSize, tileSize);
                                drawn = true;
                            }
                        }
                    }
                } catch (Exception e) {}

                if (!drawn && grassImg != null) {
                    gc.drawImage(grassImg, dx, dy, tileSize, tileSize);
                }
            }
        }

        // 🌟 BƯỚC CẢI TIẾN 2: Vẽ thực thể (Khử hoàn toàn Reflection để chống lag)
        if (worldMap.chunkMap != null) {
            for (int cy = 0; cy < worldMap.chunkMap.length; cy++) {
                for (int cx = 0; cx < worldMap.chunkMap[cy].length; cx++) {
                    core.enviroment.Chunk chunk = worldMap.chunkMap[cy][cx];
                    if (chunk == null || chunk.getEntityList() == null) continue;

                    List<entities.base.Entity> safeEntities = null;
                    synchronized (chunk.getEntityList()) {
                        if (!chunk.getEntityList().isEmpty()) {
                            safeEntities = new ArrayList<>(chunk.getEntityList());
                        }
                    }

                    if (safeEntities == null) continue;

                    // Duyệt nhanh qua danh sách để ghi nhận các vị trí có Bụi cây (Bush) xuất hiện trong Chunk này
                    // Việc dùng toán học so sánh primitive tọa độ này chạy siêu nhanh, không tốn tí tài nguyên CPU nào
                    List<String> bushCoordinates = new ArrayList<>();
                    for (entities.base.Entity entity : safeEntities) {
                        if (entity != null && entity.getClass().getSimpleName().equalsIgnoreCase("Bush")) {
                            bushCoordinates.add(entity.getX() + "," + entity.getY());
                        }
                    }

                    for (entities.base.Entity entity : safeEntities) {
                        if (entity == null) continue;

                        int ex = entity.getX();
                        int ey = entity.getY();

                        if (ex < 0 || ex >= gridSize || ey < 0 || ey >= gridSize) continue;

                        double edx = ex * tileSize;
                        double edy = ey * tileSize;

                        double entityScreenX = edx * scale + offsetX;
                        double entityScreenY = edy * scale + offsetY;
                        if (entityScreenX + tileSize * scale < 0 || entityScreenX > canvasWidth ||
                                entityScreenY + tileSize * scale < 0 || entityScreenY > canvasHeight) {
                            continue;
                        }

                        String typeName = entity.getClass().getSimpleName().toLowerCase().trim();

                        // 🌟 TỐI ƯU HÓA YÊU CẦU 1: Nếu thực thể là Thỏ và tọa độ trùng khít với 1 Bụi cây -> Ẩn luôn không vẽ!
                        if (typeName.equals("rabbit")) {
                            if (bushCoordinates.contains(ex + "," + ey)) {
                                continue; // Bỏ qua không vẽ hình thỏ, bụi cây bên dưới sẽ đè lên tự nhiên!
                            }
                        }

                        Image entitySprite = null;

                        switch (typeName) {
                            case "trees": case "tree":
                                entitySprite = AssetManager.get("tree_medium");
                                break;
                            case "bush":
                                entitySprite = AssetManager.get("tree_small");
                                break;
                            case "stone": case "rock":
                                entitySprite = AssetManager.get("stone");
                                break;
                            case "food":
                                entitySprite = AssetManager.get("tree_small");
                                break;
                            case "rabbit":
                                entitySprite = AssetManager.get("animal_rabbit");
                                break;
                            case "wolf":
                                entitySprite = AssetManager.get("animal_wolf");
                                break;
                            case "tiger":
                                entitySprite = AssetManager.get("animal_tiger");
                                break;
                            case "elephant":
                                entitySprite = AssetManager.get("animal_elephant");
                                break;
                            case "bear":
                                entitySprite = AssetManager.get("animal_bear");
                                break;
                            case "fish":
                                entitySprite = AssetManager.get("animal_fish");
                                break;
                            case "duck":
                                entitySprite = AssetManager.get("animal_duck");
                                break;
                        }

                        if (entitySprite != null) {
                            gc.drawImage(entitySprite, edx, edy, tileSize, tileSize);
                        }
                    }
                }
            }
        }

        gc.restore();
    }
}