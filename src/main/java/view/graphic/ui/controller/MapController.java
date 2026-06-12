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

    private GraphicsContext gc;
    private WorldMap worldMap;
    private int gridSize = 100;
    private boolean isContextSet = false;

    private static final double TILE_SIZE = 32.0;
    private double scale = 2.5;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private double lastMouseX;
    private double lastMouseY;

    private final double MIN_SCALE = 0.2;
    private final double MAX_SCALE = 10.0;

    private long lastUpdateTime = 0;
    private long lastStatusRefreshTime = 0;
    private final long RENDER_DELAY_NS = 16_666_666L; // ~60 FPS
    private final long STATUS_REFRESH_DELAY_NS = 100_000_000L; // ~10 updates per second

    private AnimationTimer renderLoop;
    private view.graphic.ui.controller.ControlPanel controlPanel;
    private MainController mainController;
    private entities.base.Animals selectedAnimal;
    private core.enviroment.Chunk selectedChunk;
    private int selectedGridX = -1;
    private int selectedGridY = -1;

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
                if (now - lastStatusRefreshTime >= STATUS_REFRESH_DELAY_NS) {
                    updateControlPanelTime();
                    lastStatusRefreshTime = now;
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
            handleSelection(gridX, gridY);
            return;
        }

        System.out.println("🎯 [Tạo thực thể]: Đặt " + SELECTED_TOOL + " tại ô: [" + gridX + ", " + gridY + "]");

        entities.base.Entity newEntity = null;

        // CHUẨN HÓA TOÀN BỘ CHUỖI CASE THEO ĐÚNG GIÁ TRỊ TRUYỀN SANG TỪ CONTROLPANEL
        switch (SELECTED_TOOL.toUpperCase().trim()) {
            case "BUSH" -> newEntity = new entities.Bush(gridX, gridY); // Khớp với onToolGrass từ ControlPanel
            case "TREE" -> newEntity = new entities.Trees(gridX, gridY); // Khớp với onToolTree từ ControlPanel
            case "ROCK" -> newEntity = new entities.Rock(gridX, gridY); // Khớp với onToolRock từ ControlPanel
            case "RABBIT" -> newEntity = new entities.Rabbit(gridX, gridY); // Khớp với onToolRabbit từ ControlPanel
            case "WOLF" -> newEntity = new entities.Wolf(gridX, gridY); // Khớp với onToolWolf từ ControlPanel
            case "TIGER" -> newEntity = new entities.Tiger(gridX, gridY); // Khớp với onToolTiger từ ControlPanel
            case "ELEPHANT" -> newEntity = new entities.Elephant(gridX, gridY); // Khớp với onToolElephant từ ControlPanel
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

    private void applyViewerTransform() {
        if (view.MapViewer.instance == null) return;
        double centerX = view.MapViewer.instance.getViewCenterX();
        double centerY = view.MapViewer.instance.getViewCenterY();

        Runnable update = () -> {
            if (mapCanvas == null) return;
            if (mapCanvas.getWidth() <= 0 || mapCanvas.getHeight() <= 0) return;
            if (centerX >= 0 && centerY >= 0) {
                this.offsetX = (mapCanvas.getWidth() / 2.0) - centerX * (TILE_SIZE * scale);
                this.offsetY = (mapCanvas.getHeight() / 2.0) - centerY * (TILE_SIZE * scale);
            } else {
                this.offsetX = view.MapViewer.instance.getViewOffsetX();
                this.offsetY = view.MapViewer.instance.getViewOffsetY();
            }
        };

        if (mapCanvas != null && mapCanvas.getWidth() > 0 && mapCanvas.getHeight() > 0) {
            update.run();
        } else {
            javafx.application.Platform.runLater(update);
        }
    }

    public double getScale() {
        return scale;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getCanvasWidth() {
        return mapCanvas != null ? mapCanvas.getWidth() : 0.0;
    }

    public double getCanvasHeight() {
        return mapCanvas != null ? mapCanvas.getHeight() : 0.0;
    }

    public double getTileSize() {
        return TILE_SIZE;
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
        if (view.MapViewer.instance != null) {
            this.scale = view.MapViewer.instance.getGraphicViewScale();
            applyViewerTransform();
        } else {
            this.scale = 2.5;
            this.offsetX = 0.0;
            this.offsetY = 0.0;
        }
        this.isContextSet = true;
    }

    public void setWorldMapContext(core.enviroment.WorldMap worldMap, int gridSize) {
        this.setWorldContext(worldMap, gridSize);
    }

    public void setControlPanel(view.graphic.ui.controller.ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    private void updateControlPanelTime() {
        if (controlPanel != null) {
            controlPanel.refreshTimeStatus();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void resetView() {
        if (mapCanvas == null || view.MapViewer.instance == null) return;
        this.scale = 2.5;
        this.offsetX = (mapCanvas.getWidth() - (WorldMap.SIZE * TILE_SIZE * scale)) / 2.0;
        this.offsetY = (mapCanvas.getHeight() - (WorldMap.SIZE * TILE_SIZE * scale)) / 2.0;
        view.MapViewer.instance.setGraphicViewScale(this.scale);
    }

    private void handleSelection(int gridX, int gridY) {
        this.selectedGridX = gridX;
        this.selectedGridY = gridY;
        this.selectedAnimal = findEntityAt(gridX, gridY);
        this.selectedChunk = findChunkAt(gridX, gridY);
        if (this.selectedAnimal != null) {
            this.selectedChunk = findChunkAt(this.selectedAnimal.getX(), this.selectedAnimal.getY());
        }
        if (controlPanel != null) {
            controlPanel.setSelectedInfo(buildSelectedAnimalInfo(), buildSelectedChunkInfo());
        }
        if (mainController != null) {
            mainController.setSelectionInfo(buildSelectedAnimalInfo(), buildSelectedChunkInfo());
        }
    }

    private String buildSelectedAnimalInfo() {
        if (selectedAnimal == null || !selectedAnimal.checkAlive()) {
            return "No animal selected. Click an animal to inspect its status.";
        }
        StringBuilder info = new StringBuilder();
        info.append("Type: ").append(selectedAnimal.getClass().getSimpleName()).append("\n");
        info.append("Position: (").append(selectedAnimal.getX()).append(", ").append(selectedAnimal.getY()).append(")\n");
        info.append("Hunger: ").append(String.format("%.1f", selectedAnimal.getHunger())).append("\n");
        info.append("Thirst: ").append(String.format("%.1f", selectedAnimal.getThirst())).append("\n");
        info.append("Cooldown: ").append(selectedAnimal.getCurrentMoveCooldown()).append("\n");
        return info.toString();
    }

    private String buildSelectedChunkInfo() {
        if (selectedChunk == null) {
            return "No chunk selected. Click an empty tile or an animal to inspect the chunk contents.";
        }
        StringBuilder info = new StringBuilder();
        int chunkX = selectedGridX / core.enviroment.WorldMap.CHUNK_SIZE;
        int chunkY = selectedGridY / core.enviroment.WorldMap.CHUNK_SIZE;
        info.append("Chunk: [").append(chunkX).append(", ").append(chunkY).append("]\n");
        info.append("Entities: ").append(selectedChunk.getEntityList().size()).append("\n");
        info.append("Distance to water: ").append(selectedChunk.getDistanceToWater()).append("\n");
        return info.toString();
    }

    private entities.base.Animals findEntityAt(int gridX, int gridY) {
        if (worldMap == null || worldMap.chunkMap == null) return null;
        for (core.enviroment.Chunk[] chunkRow : worldMap.chunkMap) {
            if (chunkRow == null) continue;
            for (core.enviroment.Chunk chunk : chunkRow) {
                if (chunk == null || chunk.getEntityList() == null) continue;
                synchronized (chunk.getEntityList()) {
                    for (entities.base.Entity entity : chunk.getEntityList()) {
                        if (entity instanceof entities.base.Animals animal && animal.checkAlive() && animal.getX() == gridX && animal.getY() == gridY) {
                            return animal;
                        }
                    }
                }
            }
        }
        return null;
    }

    private core.enviroment.Chunk findChunkAt(int gridX, int gridY) {
        if (worldMap == null || worldMap.chunkMap == null) return null;
        int chunkX = gridX / core.enviroment.WorldMap.CHUNK_SIZE;
        int chunkY = gridY / core.enviroment.WorldMap.CHUNK_SIZE;
        if (chunkY < 0 || chunkY >= worldMap.chunkMap.length || chunkX < 0 || chunkX >= worldMap.chunkMap[chunkY].length) {
            return null;
        }
        return worldMap.chunkMap[chunkY][chunkX];
    }

    private void drawSelectionOverlay() {
        if (gc == null) return;
        gc.save();
        gc.setStroke(javafx.scene.paint.Color.YELLOW);
        gc.setLineWidth(2.0 / scale);

        if (selectedAnimal != null && selectedAnimal.checkAlive()) {
            double x = selectedAnimal.getRenderX();
            double y = selectedAnimal.getRenderY();
            gc.strokeOval(x + TILE_SIZE * 0.08, y + TILE_SIZE * 0.08, TILE_SIZE * 0.84, TILE_SIZE * 0.84);
        } else if (selectedGridX >= 0 && selectedGridY >= 0) {
            double x = selectedGridX * TILE_SIZE;
            double y = selectedGridY * TILE_SIZE;
            gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
        }

        gc.restore();
    }

    private void refreshSelectionInfo() {
        if (selectedAnimal != null && selectedAnimal.checkAlive()) {
            selectedGridX = selectedAnimal.getX();
            selectedGridY = selectedAnimal.getY();
            selectedChunk = findChunkAt(selectedGridX, selectedGridY);
        }

        String animalInfo = buildSelectedAnimalInfo();
        String chunkInfo = buildSelectedChunkInfo();

        if (controlPanel != null) {
            controlPanel.setSelectedInfo(animalInfo, chunkInfo);
        }
        if (mainController != null) {
            mainController.setSelectionInfo(animalInfo, chunkInfo);
        }
    }

    private void drawAnimalStatusBars(entities.base.Animals animal, double edx, double edy, double width, double height) {
        if (animal == null || gc == null) return;
        double hungerPct = Math.max(0, Math.min(1, animal.getHungerPercentage() / 100.0));
        double thirstPct = Math.max(0, Math.min(1, animal.getThirstPercentage() / 100.0));

        double barWidth = width * 0.85;
        double barHeight = Math.max(2.0, width * 0.08);
        double barX = edx + (width - barWidth) / 2.0;
        double barY = edy - (barHeight * 2.5);

        gc.save();
        gc.setGlobalAlpha(0.9);
        gc.setFill(javafx.scene.paint.Color.rgb(30, 30, 30, 0.9));
        gc.fillRoundRect(barX, barY, barWidth, barHeight * 2 + 4, 3, 3);

        // Hunger bar
        gc.setFill(javafx.scene.paint.Color.rgb(220, 80, 80));
        gc.fillRoundRect(barX + 1, barY + 1, Math.max(1, (barWidth - 2) * hungerPct), barHeight - 1, 2, 2);
        gc.setStroke(javafx.scene.paint.Color.rgb(110, 110, 110));
        gc.setLineWidth(0.8 / scale);
        gc.strokeRoundRect(barX + 1, barY + 1, barWidth - 2, barHeight - 1, 2, 2);

        // Thirst bar
        double thirstY = barY + barHeight + 3;
        gc.setFill(javafx.scene.paint.Color.rgb(80, 170, 230));
        gc.fillRoundRect(barX + 1, thirstY, Math.max(1, (barWidth - 2) * thirstPct), barHeight - 1, 2, 2);
        gc.setStroke(javafx.scene.paint.Color.rgb(110, 110, 110));
        gc.strokeRoundRect(barX + 1, thirstY, barWidth - 2, barHeight - 1, 2, 2);
        gc.restore();
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

    private String getCurrentSeasonKey() {
        String season = core.TimeSystem.season;
        if (season == null) {
            return "spring";
        }
        return switch (season.toLowerCase()) {
            case "summer" -> "summer";
            case "autumn", "fall" -> "autumn";
            case "winter" -> "winter";
            default -> "spring";
        };
    }

    private String getTerrainTileKey(core.enviroment.Terrain terrain, String seasonKey) {
        return switch (terrain) {
            case WATER -> "tile_water_" + seasonKey;
            case MOUNTAIN -> "tile_mountain_" + seasonKey;
            case MUD -> "tile_mud_" + seasonKey;
            case FOREST -> "tile_forest_" + seasonKey;
            case GRASSLAND -> "tile_grass_" + seasonKey;
            default -> "tile_grass_" + seasonKey;
        };
    }

    private void drawNeighborTerrainBlend(int x, int y, double dx, double dy, double tileSize, String seasonKey, core.enviroment.Terrain terrain) {
        int[][] neighbors = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] offset : neighbors) {
            int nx = x + offset[0];
            int ny = y + offset[1];
            if (nx < 0 || nx >= gridSize || ny < 0 || ny >= gridSize) continue;
            core.enviroment.Terrain neighbor = worldMap.getTile(nx, ny);
            if (neighbor != null && neighbor != terrain) {
                Image neighborTile = AssetManager.get(getTerrainTileKey(neighbor, seasonKey));
                if (neighborTile != null) {
                    gc.setGlobalAlpha(0.18);
                    gc.drawImage(neighborTile, dx, dy, tileSize, tileSize);
                    gc.setGlobalAlpha(1.0);
                    break;
                }
            }
        }
    }

    private void drawDecorationLayer(double tileSize, String seasonKey) {
        if (worldMap == null) return;

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                String decorKey = worldMap.getDecoration(x, y);
                if (decorKey == null || decorKey.isEmpty()) continue;
                Image decorImage = AssetManager.get(decorKey);
                if (decorImage == null) continue;

                double dx = x * tileSize;
                double dy = y * tileSize;
                double decorSize = tileSize * 0.72;
                double decorOffset = (tileSize - decorSize) / 2.0;
                double screenX = dx * scale + offsetX;
                double screenY = dy * scale + offsetY;
                if (screenX + tileSize * scale < 0 || screenX > mapCanvas.getWidth() ||
                        screenY + tileSize * scale < 0 || screenY > mapCanvas.getHeight()) {
                    continue;
                }

                gc.setGlobalAlpha(0.75);
                gc.drawImage(decorImage, dx + decorOffset, dy + decorOffset, decorSize, decorSize);
                gc.setGlobalAlpha(1.0);
            }
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
        String seasonKey = getCurrentSeasonKey();

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
                        Image terrainTile = AssetManager.get(getTerrainTileKey(terrain, seasonKey));
                        if (terrainTile != null) {
                            gc.drawImage(terrainTile, dx, dy, tileSize, tileSize);
                            drawn = true;
                        }

                        // Blending overlay: vẽ lai ảnh ô láng giềng với alpha nhỏ nếu khác loại
                        drawNeighborTerrainBlend(x, y, dx, dy, tileSize, seasonKey, terrain);
                    }
                } catch (Exception e) {
                    // ignore render fallback
                }

                if (!drawn) {
                    Image fallback = AssetManager.get("tile_grass_" + seasonKey);
                    if (fallback != null) {
                        gc.drawImage(fallback, dx, dy, tileSize, tileSize);
                    }
                }
            }
        }

        // 2. VẼ LỚP DECORATION trước entities để bị che khuất.
        drawDecorationLayer(tileSize, seasonKey);

        // 3. VẼ CÁC THỰC THỂ TỪ CHUNK
        //    - Sắp xếp chung theo trục Y để cây/ bụi che các vật phía sau đúng
        //    - Culling dùng bounding box sprite thực tế để render khi phần thân tràn ra ô
        // =========================================================================
        if (worldMap.chunkMap != null) {
            class RenderEntry {
                final entities.base.Entity entity;
                final double drawX;
                final double drawY;
                final double renderWidth;
                final double renderHeight;
                final int sortY;
                final int order;
                final String spriteKey;

                RenderEntry(entities.base.Entity entity, double drawX, double drawY, double renderWidth, double renderHeight, int sortY, int order, String spriteKey) {
                    this.entity = entity;
                    this.drawX = drawX;
                    this.drawY = drawY;
                    this.renderWidth = renderWidth;
                    this.renderHeight = renderHeight;
                    this.sortY = sortY;
                    this.order = order;
                    this.spriteKey = spriteKey;
                }
            }

            List<RenderEntry> renderList = new ArrayList<>();

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

                    for (entities.base.Entity entity : safeEntities) {
                        if (entity == null || !entity.checkAlive()) continue;

                        int ex = entity.getX();
                        int ey = entity.getY();
                        if (ex < 0 || ex >= gridSize || ey < 0 || ey >= gridSize) continue;

                        double edx = ex * tileSize;
                        double edy = ey * tileSize;
                        double renderWidth = tileSize;
                        double renderHeight = tileSize;
                        double offsetXTree = 0;
                        double offsetYTree = 0;
                        int order = (entity instanceof entities.base.Animals) ? 0 : 1;
                        String spriteKey = null;

                        if (entity instanceof entities.base.Animals animal) {
                            animal.updateAnimation();
                            edx = animal.getRenderX();
                            edy = animal.getRenderY();
                            renderWidth = tileSize;
                            renderHeight = tileSize;
                        } else {
                            String typeName = entity.getClass().getSimpleName().toLowerCase().trim();
                            int stage = 1;
                            if (entity instanceof entities.base.Tree tree) {
                                stage = tree.getGrowthStage();
                            }
                            switch (typeName) {
                                case "tree":
                                case "trees":
                                    spriteKey = switch (stage) {
                                        case 0 -> "tree_small";
                                        case 1 -> "tree_medium";
                                        default -> "tree_big";
                                    };
                                    break;
                                case "bush":
                                    spriteKey = "bush";
                                    break;
                                case "stone":
                                case "rock":
                                    spriteKey = "stone";
                                    break;
                            }

                            Image entitySprite = (spriteKey != null) ? AssetManager.get(spriteKey) : null;
                            if (entitySprite != null) {
                                double nativeWidth = entitySprite.getWidth();
                                double nativeHeight = entitySprite.getHeight();
                                double targetWidth = tileSize;

                                if ("tree_medium".equals(spriteKey)) {
                                    targetWidth = tileSize * 2;
                                } else if ("tree_big".equals(spriteKey)) {
                                    targetWidth = tileSize * 3;
                                }

                                renderWidth = targetWidth;
                                renderHeight = nativeHeight * (renderWidth / nativeWidth);
                                offsetXTree = (tileSize - renderWidth) / 2.0;
                                offsetYTree = tileSize - renderHeight;
                            } else {
                                switch (typeName) {
                                    case "tree":
                                    case "trees":
                                        renderWidth = tileSize * 2;
                                        renderHeight = tileSize * 2;
                                        offsetXTree = -tileSize / 2.0;
                                        offsetYTree = -tileSize;
                                        break;
                                    case "tree_big":
                                    case "bigtree":
                                        renderWidth = tileSize * 3;
                                        renderHeight = tileSize * 3;
                                        offsetXTree = -tileSize;
                                        offsetYTree = -tileSize * 2;
                                        break;
                                    case "bush":
                                    case "stone":
                                    case "rock":
                                        renderWidth = tileSize;
                                        renderHeight = tileSize;
                                        break;
                                }
                            }

                            edx += offsetXTree;
                            edy += offsetYTree;
                        }

                        double entityScreenX = edx * scale + offsetX;
                        double entityScreenY = edy * scale + offsetY;
                        double entityScreenMaxX = entityScreenX + renderWidth * scale;
                        double entityScreenMaxY = entityScreenY + renderHeight * scale;
                        if (entityScreenMaxX < 0 || entityScreenX > canvasWidth ||
                                entityScreenMaxY < 0 || entityScreenY > canvasHeight) {
                            continue;
                        }

                        renderList.add(new RenderEntry(entity, edx, edy, renderWidth, renderHeight, ey, order, spriteKey));
                    }
                }
            }

            renderList.sort((a, b) -> {
                int cmp = Integer.compare(a.sortY, b.sortY);
                if (cmp != 0) return cmp;
                return Integer.compare(a.order, b.order);
            });

            for (RenderEntry entry : renderList) {
                entities.base.Entity entity = entry.entity;
                double edx = entry.drawX;
                double edy = entry.drawY;
                double renderWidth = entry.renderWidth;
                double renderHeight = entry.renderHeight;
                String spriteKey = entry.spriteKey;

                if (entity instanceof entities.base.Animals animal) {
                    String typeName = entity.getClass().getSimpleName().toLowerCase().trim();
                    Image entitySprite = null;
                    String dir = animal.getCurrentDirection();
                    int frame = animal.getCurrentAnimationFrame();

                    if (animal.isMoving()) {
                        entitySprite = AssetManager.get(typeName + "_walk_" + dir + "_" + frame);
                    }
                    if (entitySprite == null) {
                        entitySprite = AssetManager.get("animal_" + typeName);
                    }
                    if (entitySprite != null) {
                        gc.drawImage(entitySprite, edx, edy, renderWidth, renderHeight);
                        drawAnimalStatusBars(animal, edx, edy, renderWidth, renderHeight);
                    }
                } else {
                    Image entitySprite = null;
                    if (spriteKey != null) {
                        entitySprite = AssetManager.get(spriteKey);
                    }
                    if (entitySprite != null) {
                        gc.drawImage(entitySprite, edx, edy, renderWidth, renderHeight);
                    }
                }
            }

            drawSelectionOverlay();
        }

        gc.restore();
    }
}