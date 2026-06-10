package view.graphic.ui.controller;

import java.util.ArrayList;
import java.util.List;

import core.enviroment.WorldMap;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class MapController {

    @FXML
    private Canvas mapCanvas;
    
    private MainController mainController;

    private GraphicsContext gc;
    private WorldMap worldMap;
    private int gridSize = 100;
    private boolean isContextSet = false;

    // --- HỆ THỐNG ZOOM & DI CHUYỂN TOÀN DIỆN ---
    private double scale = 2.5; // Đặt mặc định phóng to 2.5 lần để nhìn rõ ngay từ đầu
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private double lastMouseX;
    private double lastMouseY;

    private final double MIN_SCALE = 0.2;
    private final double MAX_SCALE = 10.0;

    // Kiểm soát thời gian vẽ: Giới hạn khung hình vẽ giúp Core ngầm chạy mượt 100%
    private long lastUpdateTime = 0;
    private final long RENDER_DELAY_NS = 66_666_666L; // ~15 FPS cực nhẹ máy

    private AnimationTimer renderLoop;

    @FXML
    public void initialize() {
        if (mapCanvas != null) {
            gc = mapCanvas.getGraphicsContext2D();

            mapCanvas.setOnScroll(this::handleScroll);
            mapCanvas.setOnMousePressed(this::handleMousePressed);
            mapCanvas.setOnMouseDragged(this::handleMouseDragged);
        }

        // Khởi tạo vòng lặp render độc nhất
        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isContextSet || worldMap == null) {
                    return;
                }
                if (now - lastUpdateTime >= RENDER_DELAY_NS) {
                    renderMap();
                    lastUpdateTime = now;
                }
            }
        };
        renderLoop.start();
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void handleScroll(ScrollEvent event) {
        double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
        double oldScale = scale;

        // SỬA ĐỔI AN TOÀN: Tính toán clamp thủ công để tránh lỗi trên một số bản JDK cũ
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

        // Tải tài nguyên hình ảnh (Cỏ, Nước, Sinh vật) lên RAM
        AssetManager.loadAssets();

        // Reset vị trí camera về chuẩn trung tâm khi mới mở
        this.scale = 2.5;
        this.offsetX = 0.0;
        this.offsetY = 0.0;

        // Phất cờ hiệu cho phép render luồng giao diện
        this.isContextSet = true;
    }

    /**
     * HÀM GIẢI PHÓNG TOÀN DIỆN: Khi về Basic Mode, bắt buộc phải tắt hẳn luồng vẽ
     * để trả lại 100% tài nguyên cho Core backend xử lý di chuyển!
     */
    public void shutdownTimeline() {
        this.isContextSet = false;
        this.worldMap = null;

        if (gc != null && mapCanvas != null) {
            // Xóa sạch canvas để không chiếm dụng RAM của card đồ họa
            gc.save();
            gc.setTransform(1, 0, 0, 1, 0, 0); // Reset ma trận về gốc
            gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
            gc.restore();
        }
        System.out.println("📺 [MapController] Đã tắt và giải phóng toàn bộ khóa luồng đồ họa.");
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
        Image stoneImg = AssetManager.get("stone"); // Đồng bộ tên key ảnh map cũ

        // --- GIAI ĐOẠN 1: VẼ NỀN ĐỊA HÌNH ---
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                double dx = x * tileSize;
                double dy = y * tileSize;

                // THUẬT TOÁN CULLING: Chỉ vẽ ô đất nằm trong vùng nhìn thấy để cứu CPU máy tính
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
                        if (terrain.isGrass() && grassImg != null) {
                            gc.drawImage(grassImg, dx, dy, tileSize, tileSize);
                            drawn = true;
                        } else if (!terrain.isPassable() && waterImg != null) {
                            gc.drawImage(waterImg, dx, dy, tileSize, tileSize);
                            drawn = true;
                        }
                    }
                } catch (Exception e) {}

                if (!drawn && grassImg != null) {
                    gc.drawImage(grassImg, dx, dy, tileSize, tileSize);
                }
            }
        }

        // --- GIAI ĐOẠN 2: SAO CHÉP SIÊU TỐC VÀ VẼ SINH VẬT ---
        if (worldMap.chunkMap != null) {
            for (int cy = 0; cy < worldMap.chunkMap.length; cy++) {
                for (int cx = 0; cx < worldMap.chunkMap[cy].length; cx++) {
                    core.enviroment.Chunk chunk = worldMap.chunkMap[cy][cx];
                    if (chunk == null || chunk.getEntityList() == null) continue;

                    // Chỉ clone mảng cực nhanh rồi nhả khóa ngay lập tức
                    List<entities.base.Entity> safeEntities = null;
                    synchronized (chunk.getEntityList()) {
                        if (!chunk.getEntityList().isEmpty()) {
                            safeEntities = new ArrayList<>(chunk.getEntityList());
                        }
                    }

                    if (safeEntities == null) continue;

                    for (entities.base.Entity entity : safeEntities) {
                        if (entity == null) continue;
                        
                        int ex = entity.getX();
                        int ey = entity.getY();

                        if (ex < 0 || ex >= gridSize || ey < 0 || ey >= gridSize) continue;

                        double edx = ex * tileSize;
                        double edy = ey * tileSize;

                        // Culling sinh vật
                        double entityScreenX = edx * scale + offsetX;
                        double entityScreenY = edy * scale + offsetY;
                        if (entityScreenX + tileSize * scale < 0 || entityScreenX > canvasWidth ||
                                entityScreenY + tileSize * scale < 0 || entityScreenY > canvasHeight) {
                            continue;
                        }

                        javafx.scene.image.Image entitySprite = null;
                        String typeName = entity.getClass().getSimpleName().toLowerCase();

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
                            case "deer":
                                entitySprite = AssetManager.get("animal_deer");
                                break;
                            case "tiger":
                                entitySprite = AssetManager.get("animal_tiger");
                                break;
                            case "duck":
                                entitySprite = AssetManager.get("animal_duck");
                                break;
                            case "elephant":
                                entitySprite = AssetManager.get("animal_elephant");
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
    
    // Thêm mới
    @FXML
    private void onMapClicked(MouseEvent event) {
        if (!event.isStillSincePress()) return; // Tránh nhầm lẫn giữa kéo chuột (Pan) và Click

        // ĐỌC THÔNG QUA MASTER CONTROLLER AN TOÀN
        String activeTool = (mainController != null) ? mainController.getSelectedTool() : "NONE";

        if ("NONE".equals(activeTool) || activeTool == null) return;

        double mouseX = event.getX();
        double mouseY = event.getY();
        final double tileSize = 32.0;

        // Thuật toán đổi pixel màn hình sang tọa độ ô lưới game
        int gridX = (int) Math.floor((mouseX - offsetX) / (tileSize * scale));
        int gridY = (int) Math.floor((mouseY - offsetY) / (tileSize * scale));

        if (gridX >= 0 && gridX < gridSize && gridY >= 0 && gridY < gridSize) {
        	core.enviroment.Chunk targetChunk = worldMap.getChunkMap()[gridY][gridX];;
            
            if (targetChunk != null) {
                // 2. Sử dụng đồng bộ hóa để thêm thực thể an toàn
                synchronized (targetChunk.getEntityList()) {
                    switch (activeTool) {
                        case "ANIMAL_RABBIT":
                            System.out.println("🐇 [Spawn] Sinh Thỏ tại: [" + gridX + ", " + gridY + "]");
                            targetChunk.getEntityList().add(new entities.Rabbit(gridX, gridY));
                            break;
                            
                        case "ANIMAL_WOLF":
                            System.out.println("🐺 [Spawn] Sinh Sói tại: [" + gridX + ", " + gridY + "]");
                            targetChunk.getEntityList().add(new entities.Wolf(gridX, gridY));
                            break;
                            
                        case "ANIMAL_TIGER":
                            System.out.println("🐯 [Spawn] Sinh Hổ tại: [" + gridX + ", " + gridY + "]");
                            targetChunk.getEntityList().add(new entities.Tiger(gridX, gridY));
                            break;

                        case "ANIMAL_ELEPHANT":
                            System.out.println("🐘 [Spawn] Sinh Voi tại: [" + gridX + ", " + gridY + "]");
                            targetChunk.getEntityList().add(new entities.Elephant(gridX, gridY));
                            break;
                    }
                }
            }
        }
    }
}