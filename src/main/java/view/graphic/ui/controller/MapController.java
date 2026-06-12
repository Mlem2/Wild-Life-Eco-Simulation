package view.graphic.ui.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;          // Thêm import này
import javafx.scene.media.MediaPlayer;    // Thêm import này
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
    private final long RENDER_DELAY_NS = 16_666_666L; // ~60 FPS

    private MediaPlayer backgroundNoisePlayer;

    private AnimationTimer renderLoop;

    public static String SELECTED_TOOL = "NONE";

    @FXML
    public void initialize() {
        if (mapCanvas != null) {
            gc = mapCanvas.getGraphicsContext2D();
            mapCanvas.setOnScroll(this::handleScroll);
            mapCanvas.setOnMousePressed(this::handleMousePressed);
            mapCanvas.setOnMouseDragged(this::handleMouseDragged);

            // Tự động ép Canvas co giãn full 100% theo vùng chứa cha mà không lo viền đen
            javafx.application.Platform.runLater(() -> {
                if (mapCanvas.getParent() instanceof javafx.scene.layout.Pane parent) {
                    mapCanvas.setWidth(parent.getWidth() > 0 ? parent.getWidth() : 800);
                    mapCanvas.setHeight(parent.getHeight() > 0 ? parent.getHeight() : 600);

                    // Lắng nghe thay đổi kích thước cửa sổ để tự co giãn động
                    parent.widthProperty().addListener((obs, oldW, newW) -> mapCanvas.setWidth(newW.doubleValue()));
                    parent.heightProperty().addListener((obs, oldH, newH) -> mapCanvas.setHeight(newH.doubleValue()));
                }
            });
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

    // =========================================================================
    // 🎯 SỰ KIỆN CLICK CHUỘT: ĐÃ ĐỒNG BỘ KHỚP KHÍT 100% VỚI CONTROLPANEL
    // =========================================================================
    @FXML
    void onMapClicked(MouseEvent event) {
        if (!event.isStillSincePress()) return;
        if (!isContextSet || worldMap == null) return;

        final double tileSize = 32.0;

        double mouseX = event.getX();
        double mouseY = event.getY();

        // Biến đổi hệ tọa độ từ Màn hình về Lưới Game
        double worldX = (mouseX - offsetX) / scale;
        double worldY = (mouseY - offsetY) / scale;

        int gridX = (int) Math.floor(worldX / tileSize);
        int gridY = (int) Math.floor(worldY / tileSize);

        if (gridX < 0 || gridX >= gridSize || gridY < 0 || gridY >= gridSize) {
            return;
        }

        if (SELECTED_TOOL == null || SELECTED_TOOL.equals("NONE")) {
            return;
        }

        System.out.println("🎯 [Tạo thực thể]: Đặt " + SELECTED_TOOL + " tại ô: [" + gridX + ", " + gridY + "]");

        entities.base.Entity newEntity = null;

        // CHUẨN HÓA TOÀN BỘ CHUỖI CASE THEO ĐÚNG GIÁ TRỊ TRUYỀN SANG TỪ CONTROLPANEL
        switch (SELECTED_TOOL.toUpperCase().trim()) {
            case "BUSH": // Khớp với onToolGrass từ ControlPanel
                newEntity = new entities.Bush(gridX, gridY);
                break;
            case "TREE": // Khớp với onToolTree từ ControlPanel
                newEntity = new entities.Trees(gridX, gridY);
                break;
            case "ROCK": // Khớp với onToolRock từ ControlPanel
                newEntity = new entities.Rock(gridX, gridY);
                break;
            case "RABBIT": // Khớp với onToolRabbit từ ControlPanel
                newEntity = new entities.Rabbit(gridX, gridY);
                break;
            case "WOLF": // Khớp với onToolWolf từ ControlPanel
                newEntity = new entities.Wolf(gridX, gridY);
                break;
            case "TIGER": // Khớp với onToolTiger từ ControlPanel
                newEntity = new entities.Tiger(gridX, gridY);
                break;
            case "ELEPHANT": // Khớp với onToolElephant từ ControlPanel
                newEntity = new entities.Elephant(gridX, gridY);
                break;
        }

        if (newEntity != null) {
            try {
                int chunkX = gridX / core.enviroment.WorldMap.CHUNK_SIZE;
                int chunkY = gridY / core.enviroment.WorldMap.CHUNK_SIZE;

                var chunk = worldMap.chunkMap[chunkY][chunkX];
                if (chunk != null) {
                    chunk.addEntity(newEntity);

                    // Đăng ký AI di chuyển sinh vật
                    if (newEntity instanceof entities.base.Animals && view.MapViewer.instance != null) {
                        var simManager = view.MapViewer.instance.getSharedSimulationManager();
                        if (simManager != null) {
                            simManager.registerBrainForEntity(newEntity);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi thêm thực thể lên Chunk: " + e.getMessage());
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
            if (mapCanvas.getParent() instanceof javafx.scene.layout.Pane parent) {
                if (parent.getWidth() > 0) mapCanvas.setWidth(parent.getWidth());
                if (parent.getHeight() > 0) mapCanvas.setHeight(parent.getHeight());
            }
        }

        AssetManager.loadAssets();
        this.scale = 2.5;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
        this.isContextSet = true;

        startBackgroundNoise("/sound/nature_ambient.mp3");
    }

    private void startBackgroundNoise(String path) {
        try {
            java.net.URL resource = getClass().getResource(path);
            if (resource != null) {
                // Nếu đang có tiếng cũ chạy, tắt hẳn đi trước khi đổi map
                if (backgroundNoisePlayer != null) {
                    backgroundNoisePlayer.stop();
                }

                Media media = new Media(resource.toExternalForm());
                backgroundNoisePlayer = new MediaPlayer(media);

                // Thiết lập vòng lặp vô tận cho âm thanh nền
                backgroundNoisePlayer.setCycleCount(MediaPlayer.INDEFINITE);

                backgroundNoisePlayer.setVolume(2);

                backgroundNoisePlayer.play();
                System.out.println("🔊 [Audio] Đã kích hoạt tiếng ồn nền vòng lặp thành công!");
            } else {
                System.err.println("⚠️ Không tìm thấy file âm thanh nền tại: " + path);
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khởi tạo hệ thống âm thanh nền: " + e.getMessage());
        }
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

        // 2. VẼ CÁC THỰC THỂ TỪ CHUNK (ĐÃ FIX: CHIA LỚP ĐỂ BỤI CỎ ĐÈ LÊN ĐỘNG VẬT)
        // =========================================================================
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

                    // -----------------------------------------------------------------
                    // LƯỢT 1: CHỈ VẼ ĐỘNG VẬT (LỚP NỀN DƯỚI)
                    // -----------------------------------------------------------------
                    for (entities.base.Entity entity : safeEntities) {
                        if (entity == null) continue;

                        // Chỉ vẽ nếu thực thể là Động vật
                        if (entity instanceof entities.base.Animals) {
                            int ex = entity.getX();
                            int ey = entity.getY();
                            if (ex < 0 || ex >= gridSize || ey < 0 || ey >= gridSize) continue;

                            entities.base.Animals animal = (entities.base.Animals) entity;
                            animal.updateAnimation(); // Cập nhật chuyển động mượt

                            double edx = animal.getRenderX();
                            double edy = animal.getRenderY();

                            // Thuật toán culling tối ưu hiệu năng màn hình
                            double entityScreenX = edx * scale + offsetX;
                            double entityScreenY = edy * scale + offsetY;
                            // SỬA THÀNH THẾ NÀY ĐỂ KHÔNG BỊ NUỐT CÂY KHI DI CHUYỂN CAMERA:
                            if (entityScreenX + (tileSize * 4) * scale < 0 || entityScreenX > canvasWidth + (tileSize * 4) * scale ||
                                    entityScreenY + (tileSize * 4) * scale < 0 || entityScreenY > canvasHeight + (tileSize * 4) * scale) {
                                continue;
                            }
                            String typeName = entity.getClass().getSimpleName().toLowerCase().trim(); //
                            Image entitySprite = null; //

                            // 1. Lấy hướng đi hiện tại ("up", "down", "left", "right") từ con vật
                            String dir = animal.getCurrentDirection();

                            // 2. Lấy số thứ tự ô ảnh bước chân hiện tại (0, 1, 2, 3)
                            int frame = animal.getCurrentAnimationFrame();

                            // 3. Kiểm tra xem con vật có đang chạy/đi bộ không
                            if (animal.isMoving()) {
                                // Nếu đang di chuyển, tự động ghép chuỗi để lấy đúng khung hình trong lưới dải ảnh
                                // Ví dụ: khi typeName là "rabbit", nó sẽ tìm key "rabbit_walk_right_2" trong AssetManager
                                entitySprite = AssetManager.get(typeName + "_walk_" + dir + "_" + frame);
                            }

                            // 4. Nếu con vật đang đứng im hoặc không tìm thấy ảnh động, quay về lấy ảnh đứng yên mặc định
                            if (entitySprite == null) {
                                entitySprite = AssetManager.get("animal_" + typeName);
                            }

                            // Tiến hành vẽ hình ảnh lên màn hình đồ họa Canvas
                            if (entitySprite != null) { //
                                gc.drawImage(entitySprite, edx, edy, tileSize, tileSize); //
                            }
                        }
                    }

                    // -----------------------------------------------------------------
                    // LƯỢT 2: CHỈ VẼ VẬT CẢN / THẢO MỘC (LỚP ĐÈ TRÊN - CÂY, BỤI CỎ, ĐÁ)
                    // -----------------------------------------------------------------
                    for (entities.base.Entity entity : safeEntities) {
                        if (entity == null) continue;

                        // Chỉ vẽ nếu KHÔNG PHẢI là động vật (tức là Cây, Bụi, Đá...)
                        if (!(entity instanceof entities.base.Animals)) {
                            int ex = entity.getX();
                            int ey = entity.getY();
                            if (ex < 0 || ex >= gridSize || ey < 0 || ey >= gridSize) continue;

                            // Vật thể tĩnh đứng im cố định theo ô lưới
                            double edx = ex * tileSize;
                            double edy = ey * tileSize;

                            double entityScreenX = edx * scale + offsetX;
                            double entityScreenY = edy * scale + offsetY;
                            if (entityScreenX + tileSize * scale < 0 || entityScreenX > canvasWidth ||
                                    entityScreenY + tileSize * scale < 0 || entityScreenY > canvasHeight)
                            {
                                continue;
                            }

                            String typeName = entity.getClass().getSimpleName().toLowerCase().trim();
                            Image entitySprite = null;

                            // Mặc định kích thước vẽ bằng 1 ô đất (Hệ 48x48)
                            double renderWidth = tileSize;
                            double renderHeight = tileSize;

                            // Biến bù trừ tọa độ để kéo gốc cây về đúng vị trí
                            double offsetXTree = 0;
                            double offsetYTree = 0;

                            switch (typeName) {
                                case "tree":
                                case "trees":
                                    entitySprite = AssetManager.get("tree_medium");
                                    if (entitySprite != null) {
                                        // Cây vừa chiếm 2 ô lưới (96x96px trong hệ lưới 48)
                                        renderWidth = tileSize * 2;
                                        renderHeight = tileSize * 2;

                                        // Dịch sang trái nửa ô và đẩy ngược lên 1 ô để gốc cắm đúng vị trí ô lưới logic
                                        offsetXTree = -tileSize / 2.0;
                                        offsetYTree = -tileSize;
                                    }
                                    break;

                                case "tree_big":
                                case "bigtree": // Đề phòng sau này cậu có Class cây to
                                    entitySprite = AssetManager.get("tree_big");
                                    if (entitySprite != null) {
                                        // Cây to chiếm 3 ô lưới (144x144px trong hệ lưới 48)
                                        renderWidth = tileSize * 3;
                                        renderHeight = tileSize * 3;

                                        // Dịch sang trái 1 ô và đẩy ngược lên 2 ô
                                        offsetXTree = -tileSize;
                                        offsetYTree = -tileSize * 2;
                                    }
                                    break;

                                case "bush":
                                    entitySprite = AssetManager.get("tree_small");
                                    break;

                                case "stone": case "rock":
                                    entitySprite = AssetManager.get("stone");
                                    break;
                            }

                            // Tiến hành vẽ ảnh lên Canvas sau khi đã khớp từ khóa và tính toán offset
                            if (entitySprite != null) {
                                gc.drawImage(entitySprite,
                                        edx + offsetXTree,
                                        edy + offsetYTree,
                                        renderWidth,
                                        renderHeight);
                            }
                        }
                    }

                }
            }
        }

        gc.restore();
    }
}