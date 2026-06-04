package view;

import java.lang.reflect.Field;
import java.util.List;

import core.TimeSystem;
import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Bush;
import entities.Trees;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.Position;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MapViewer extends Application {
    // Biến lưu trữ chế độ tương tác chuột hiện tại
    private String currentInteractionMode = "VIEW_MAP";
    private WorldMap worldMap;
    private BasicRenderer basicRenderer;

    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private final int baseTileSize = 1;
    private double lastMouseX, lastMouseY;

    // Các thành phần hiển thị thời gian, tọa độ bằng tiếng Anh
    private Label lblClock;
    private Label lblSeason;
    private Label lblPartOfDay;
    private Label lblMouseCoords;
    private Label lblSelectedAnimalDebug;
    private Label lblSelectedChunkDebug;
    private Animals selectedAnimal;
    private Chunk selectedChunk;

    // --- DASHBOARD REALTIME HIỂN THỊ ĐẦY ĐỦ TỪNG LOÀI ---
    private Label lblTotalRabbits;
    private Label lblTotalTigers;
    private Label lblTotalWolves;
    private Label lblTotalElephants;
    private Label lblTotalFishes;
    private Label lblTotalBushes;
    private Label lblTotalTrees;
    private Label lblEcoStatus;
    private final java.util.Map<Animals, brain.controller.AnimalBrainUpdate> brainMap = new java.util.HashMap<>();

    private static WorldMap sharedWorldMap;
    private static brain.controller.SimulationManager sharedSimulationManager;

    public static void setSharedWorldMap(WorldMap worldMap) {
        sharedWorldMap = worldMap;
    }

    public static void setSharedSimulationManager(brain.controller.SimulationManager simulationManager) {
        sharedSimulationManager = simulationManager;
    }

    @Override
    public void start(Stage primaryStage) {
        if (sharedWorldMap != null) {
            this.worldMap = sharedWorldMap;
        } else {
            // Khởi tạo bản đồ thế giới ngầm với Seed cố định
            worldMap = new WorldMap(94033111, WorldMap.SIZE);
        }
        
        // Gắn bản đồ vào bộ xử lý đồ họa chuyên trách Basic Mode
        basicRenderer = new BasicRenderer(worldMap);

        // Khởi tạo Canvas đồ họa JavaFX
        Canvas canvas = new Canvas();
        StackPane canvasHolder = new StackPane(canvas);
        canvasHolder.setStyle("-fx-background-color: #1a1a1a;");

        // Bind kích thước Canvas tự co giãn theo cửa sổ màn hình
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());

        // Đăng ký vẽ lại mỗi khi thay đổi kích thước cửa sổ hiển thị
        canvas.widthProperty().addListener(obs -> drawMap(canvas));
        canvas.heightProperty().addListener(obs -> drawMap(canvas));

        // Khởi tạo thanh điều khiển thông tin bên phải (Right Panel)
        VBox rightPanel = createRightPanel();
        VBox debugPanel = createDebugPanel();

        // Sử dụng cấu trúc bố cục BorderPane chia cắt rõ ràng
        BorderPane root = new BorderPane();
        root.setLeft(debugPanel);
        root.setCenter(canvasHolder); // Bản đồ nằm chính giữa
        root.setRight(rightPanel);    // Bảng theo dõi nằm bên phải

        // --- CÁC SỰ KIỆN TƯƠNG TÁC THU PHÓNG VÀ DI CHUYỂN CHUỘT TRÊN CANVAS ---
        canvas.setOnScroll(event -> {
            double delta = event.getDeltaY();
            double zoomFactor = (delta > 0) ? 1.1 : 0.9;
            double oldScale = scale;
            double newScale = scale * zoomFactor;

            double minScaleX = canvas.getWidth() / (WorldMap.SIZE * baseTileSize);
            double minScaleY = canvas.getHeight() / (WorldMap.SIZE * baseTileSize);
            double minScale = Math.max(minScaleX, minScaleY);
            double maxScale = 50.0;

            newScale = Math.max(minScale, Math.min(maxScale, newScale));
            if (newScale == oldScale) return;

            scale = newScale;
            offsetX = event.getX() - (event.getX() - offsetX) * (scale / oldScale);
            offsetY = event.getY() - (event.getY() - offsetY) * (scale / oldScale);

            double mapWidth = WorldMap.SIZE * (baseTileSize * scale);
            double mapHeight = WorldMap.SIZE * (baseTileSize * scale);
            offsetX = Math.min(0, Math.max(canvas.getWidth() - mapWidth, offsetX));
            offsetY = Math.min(0, Math.max(canvas.getHeight() - mapHeight, offsetY));

            drawMap(canvas);
        });

        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        canvas.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;
            double mapWidth = WorldMap.SIZE * (baseTileSize * scale);
            double mapHeight = WorldMap.SIZE * (baseTileSize * scale);

            offsetX = Math.min(0, Math.max(canvas.getWidth() - mapWidth, offsetX + dx));
            offsetY = Math.min(0, Math.max(canvas.getHeight() - mapHeight, offsetY + dy));

            lastMouseX = e.getX();
            lastMouseY = e.getY();
            drawMap(canvas);
        });

        canvas.setOnMouseMoved(e -> {
            int gridX = (int) ((e.getX() - offsetX) / scale);
            int gridY = (int) ((e.getY() - offsetY) / scale);
            if (gridX >= 0 && gridX < WorldMap.SIZE && gridY >= 0 && gridY < WorldMap.SIZE) {
                lblMouseCoords.setText(String.format("Tile Coords: [%d, %d] (%s)", gridX, gridY, worldMap.getTile(gridX, gridY).getName()));
            } else {
                lblMouseCoords.setText("Tile Coords: Out of bounds");
            }
        });

        // --- SỰ KIỆN CLICK CHUỘT TRÁI THẢ THÚ: KIỂM TRA ĐIỀU KIỆN BIOME NGHIÊM NGẶT ---
        canvas.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                int gridX = (int) ((e.getX() - offsetX) / scale);
                int gridY = (int) ((e.getY() - offsetY) / scale);

                if (gridX >= 0 && gridX < WorldMap.SIZE && gridY >= 0 && gridY < WorldMap.SIZE) {
                    Entity clickedEntity = findEntityAt(gridX, gridY);
                    if ("VIEW_MAP".equals(currentInteractionMode)) {
                        handleSelectionClick(gridX, gridY, clickedEntity);
                        refreshSelectedAnimalDebug();
                        refreshSelectedChunkDebug();
                        drawMap((Canvas) e.getSource());
                        return;
                    }

                    int chunkX = gridX / WorldMap.CHUNK_SIZE;
                    int chunkY = gridY / WorldMap.CHUNK_SIZE;

                    Chunk[][] chunkMap = worldMap.chunkMap;

                    if (chunkMap != null && chunkMap[chunkY][chunkX] != null) {
                        Chunk targetChunk = chunkMap[chunkY][chunkX];

                        // Trích xuất tên vùng đất thực tế đang click chuột vào
                        var currentTile = worldMap.getTile(gridX, gridY);
                        String tileName = (currentTile != null && currentTile.getName() != null) ? currentTile.getName().toLowerCase() : "";

                        // Định vị nhanh tính chất loại địa hình từ chuỗi tên gốc của Backend
                        boolean isWater = tileName.contains("water") || tileName.contains("lake") || tileName.contains("sea") || tileName.contains("nuoc");
                        boolean isStone = tileName.contains("stone") || tileName.contains("rock") || tileName.contains("da") || tileName.contains("mountain");
                        boolean isMud = tileName.contains("mud") || tileName.contains("swamp") || tileName.contains("bun");

                        if ("SPAWN_RABBIT".equals(currentInteractionMode)) {
                            if (isWater || isStone) return;
                            entities.Rabbit ent = new entities.Rabbit(gridX, gridY);
                            targetChunk.addEntity(ent);
                            if (sharedSimulationManager != null) sharedSimulationManager.registerBrainForEntity(ent);
                            else registerBrainForEntity(ent);
                        }
                        else if ("SPAWN_TIGER".equals(currentInteractionMode)) {
                            if (isWater || isStone) return;
                            entities.Tiger ent = new entities.Tiger(gridX, gridY);
                            targetChunk.addEntity(ent);
                            if (sharedSimulationManager != null) sharedSimulationManager.registerBrainForEntity(ent);
                            else registerBrainForEntity(ent);
                        }
                        else if ("SPAWN_WOLF".equals(currentInteractionMode)) {
                            if (isWater || isStone) return;
                            entities.Wolf ent = new entities.Wolf(gridX, gridY);
                            targetChunk.addEntity(ent);
                            if (sharedSimulationManager != null) sharedSimulationManager.registerBrainForEntity(ent);
                            else registerBrainForEntity(ent);
                        }
                        else if ("SPAWN_ELEPHANT".equals(currentInteractionMode)) {
                            if (isWater || isStone) return;
                            entities.Elephant ent = new entities.Elephant(gridX, gridY);
                            targetChunk.addEntity(ent);
                            if (sharedSimulationManager != null) sharedSimulationManager.registerBrainForEntity(ent);
                            else registerBrainForEntity(ent);
                        }
                        else if ("SPAWN_FISH".equals(currentInteractionMode)) {
                            if (!isWater) return;
                            entities.Fish ent = new entities.Fish(gridX, gridY);
                            targetChunk.addEntity(ent);
                            if (sharedSimulationManager != null) sharedSimulationManager.registerBrainForEntity(ent);
                            else registerBrainForEntity(ent);
                        }
                        else if ("SPAWN_BUSH".equals(currentInteractionMode)) {
                            if (isWater || isStone || isMud) return;
                            targetChunk.addEntity(new entities.Bush(gridX, gridY));
                        }
                        else if ("SPAWN_TREE".equals(currentInteractionMode)) {
                            if (isWater || isStone || isMud) return;
                            targetChunk.addEntity(new entities.Trees(gridX, gridY));
                        }
                    }
                }
            }
        });

        Scene scene = new Scene(root, 1180, 820);
        primaryStage.setTitle("Ecosystem Monitor - Full Random Biome Mode");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Vòng lặp đồ họa và logic thời gian thực ngầm định
        AnimationTimer viewRefresher = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateTimeInformation();
                refreshSelectedAnimalDebug();
                refreshSelectedChunkDebug();
                drawMap(canvas);
            }
        };
        viewRefresher.start();
    }

    private void registerAllBrains(Chunk[][] chunkMap) {
        if (chunkMap == null) return;
        for (int cy = 0; cy < chunkMap.length; cy++) {
            for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                Chunk chunk = chunkMap[cy][cx];
                if (chunk == null) continue;
                for (Entity e : chunk.getEntityList()) {
                    registerBrainForEntity(e);
                }
            }
        }
    }

    private void registerBrainForEntity(Entity e) {
        if (e == null) return;
        if (!(e instanceof Animals)) return;
        Animals a = (Animals) e;
        if (brainMap.containsKey(a)) return;
        brain.controller.MapSystem ms = new brain.controller.MapSystem(worldMap);
        brain.controller.ChooseTarget ct = new brain.controller.ChooseTarget(a, ms);
        brain.pathfinder.Pathfinder fp = new brain.pathfinder.Pathfinder(worldMap);
        brain.controller.ActionManager am = new brain.controller.ActionManager(a, ms);
        brain.controller.AnimalBrainUpdate abu = new brain.controller.AnimalBrainUpdate(a, ct, fp, am);
        brainMap.put(a, abu);
    }

    private void handleSelectionClick(int gridX, int gridY, Entity clickedEntity) {
        Animals clickedAnimal = (clickedEntity instanceof Animals) ? (Animals) clickedEntity : null;
        Chunk clickedChunk = findChunkAt(gridX, gridY);

        if (clickedAnimal != null && clickedAnimal == selectedAnimal) {
            selectedAnimal = null;
        } else if (clickedAnimal != null) {
            selectedAnimal = clickedAnimal;
            selectedChunk = findChunkAt(clickedAnimal.getX(), clickedAnimal.getY());
        } else {
            if (clickedChunk != null && clickedChunk == selectedChunk) {
                selectedChunk = null;
            } else {
                selectedChunk = clickedChunk;
            }
        }
    }

    private VBox createDebugPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(360);
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444444; -fx-border-width: 0 2 0 0;");

        Label lblDebugTitle = new Label("SELECTION INSPECTOR");
        lblDebugTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblDebugTitle.setTextFill(Color.GOLD);

        Label lblSelectionTitle = new Label("SELECTED ANIMAL DEBUG");
        lblSelectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblSelectionTitle.setTextFill(Color.LIGHTBLUE);

        lblSelectedAnimalDebug = new Label("Click any animal on the map to inspect its movement state.");
        lblSelectedAnimalDebug.setTextFill(Color.LIGHTGRAY);
        lblSelectedAnimalDebug.setWrapText(true);
        lblSelectedAnimalDebug.setMaxWidth(340);

        VBox selectionDebugBox = new VBox(6, lblSelectionTitle, lblSelectedAnimalDebug);
        selectionDebugBox.setPadding(new Insets(8));
        selectionDebugBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 4; -fx-border-color: #3a3a3a;");

        Label lblChunkTitle = new Label("SELECTED CHUNK DEBUG");
        lblChunkTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblChunkTitle.setTextFill(Color.LIGHTBLUE);

        lblSelectedChunkDebug = new Label("Click any empty tile or an animal to inspect the chunk contents.");
        lblSelectedChunkDebug.setTextFill(Color.LIGHTGRAY);
        lblSelectedChunkDebug.setWrapText(true);
        lblSelectedChunkDebug.setMaxWidth(340);

        VBox chunkDebugBox = new VBox(6, lblChunkTitle, lblSelectedChunkDebug);
        chunkDebugBox.setPadding(new Insets(8));
        chunkDebugBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 4; -fx-border-color: #3a3a3a;");

        panel.getChildren().addAll(lblDebugTitle, selectionDebugBox, chunkDebugBox);
        return panel;
    }

    // Tạo thanh bảng điều khiển bên phải (Đã chuyển sang Tiếng Anh hoàn toàn)
    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(320);
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444444; -fx-border-width: 0 0 0 2;");

        Label lblTitle = new Label("MONITORING SYSTEM");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblTitle.setTextFill(Color.GOLD);

        lblClock = new Label("Time: Loading...");
        lblClock.setTextFill(Color.LIGHTGRAY);
        lblSeason = new Label("Season: Loading...");
        lblSeason.setTextFill(Color.LIGHTGRAY);
        lblPartOfDay = new Label("Cycle: Loading...");
        lblPartOfDay.setTextFill(Color.LIGHTGRAY);

        Separator sep1 = new Separator();

        Label lblActionTitle = new Label("MOUSE INTERACTIONS");
        lblActionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblActionTitle.setTextFill(Color.LIGHTBLUE);

        javafx.scene.control.ToggleGroup actionGroup = new javafx.scene.control.ToggleGroup();
        javafx.scene.control.RadioButton rbView = new javafx.scene.control.RadioButton("View Map Only");
        javafx.scene.control.RadioButton rbSpawnRabbit = new javafx.scene.control.RadioButton("🐇 Spawn Rabbit (White)");
        javafx.scene.control.RadioButton rbSpawnTiger = new javafx.scene.control.RadioButton("🐅 Spawn Tiger (Orange)");
        javafx.scene.control.RadioButton rbSpawnWolf = new javafx.scene.control.RadioButton("🐺 Spawn Wolf (Red)");
        javafx.scene.control.RadioButton rbSpawnElephant = new javafx.scene.control.RadioButton("🐘 Spawn Elephant (Gray)");
        javafx.scene.control.RadioButton rbSpawnFish = new javafx.scene.control.RadioButton("🐟 Spawn Fish (Aquamarine)");
        javafx.scene.control.RadioButton rbSpawnBush = new javafx.scene.control.RadioButton("🌿 Plant Bush");
        javafx.scene.control.RadioButton rbSpawnTree = new javafx.scene.control.RadioButton("🌳 Plant Tree");

        rbView.setToggleGroup(actionGroup); rbView.setSelected(true); rbView.setTextFill(Color.WHITE);
        rbSpawnRabbit.setToggleGroup(actionGroup); rbSpawnRabbit.setTextFill(Color.WHITE);
        rbSpawnTiger.setToggleGroup(actionGroup); rbSpawnTiger.setTextFill(Color.WHITE);
        rbSpawnWolf.setToggleGroup(actionGroup); rbSpawnWolf.setTextFill(Color.WHITE);
        rbSpawnElephant.setToggleGroup(actionGroup); rbSpawnElephant.setTextFill(Color.WHITE);
        rbSpawnFish.setToggleGroup(actionGroup); rbSpawnFish.setTextFill(Color.WHITE);
        rbSpawnBush.setToggleGroup(actionGroup); rbSpawnBush.setTextFill(Color.WHITE);
        rbSpawnTree.setToggleGroup(actionGroup); rbSpawnTree.setTextFill(Color.WHITE);

        actionGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (rbView.isSelected()) currentInteractionMode = "VIEW_MAP";
            else if (rbSpawnRabbit.isSelected()) currentInteractionMode = "SPAWN_RABBIT";
            else if (rbSpawnTiger.isSelected()) currentInteractionMode = "SPAWN_TIGER";
            else if (rbSpawnWolf.isSelected()) currentInteractionMode = "SPAWN_WOLF";
            else if (rbSpawnElephant.isSelected()) currentInteractionMode = "SPAWN_ELEPHANT";
            else if (rbSpawnFish.isSelected()) currentInteractionMode = "SPAWN_FISH";
            else if (rbSpawnBush.isSelected()) currentInteractionMode = "SPAWN_BUSH";
            else if (rbSpawnTree.isSelected()) currentInteractionMode = "SPAWN_TREE";
        });

        VBox actionBox = new VBox(5, rbView, rbSpawnRabbit, rbSpawnTiger, rbSpawnWolf, rbSpawnElephant, rbSpawnFish, rbSpawnBush, rbSpawnTree);
        Separator sep2 = new Separator();

        // --- DASHBOARD REALTIME LIỆT KÊ ĐẦY ĐỦ TẤT CẢ CÁC LOÀI ---
        Label lblDashTitle = new Label("ECOSYSTEM STATISTICS");
        lblDashTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblDashTitle.setTextFill(Color.LIGHTBLUE);

        VBox dashBox = new VBox(4);
        dashBox.setPadding(new Insets(8));
        dashBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 4; -fx-border-color: #3a3a3a;");

        lblTotalRabbits = new Label("🐇 Rabbits: 0"); lblTotalRabbits.setTextFill(Color.WHITE);
        lblTotalTigers = new Label("🐅 Tigers: 0"); lblTotalTigers.setTextFill(Color.WHITE);
        lblTotalWolves = new Label("🐺 Wolves: 0"); lblTotalWolves.setTextFill(Color.WHITE);
        lblTotalElephants = new Label("🐘 Elephants: 0"); lblTotalElephants.setTextFill(Color.WHITE);
        lblTotalFishes = new Label("🐟 Fishes: 0"); lblTotalFishes.setTextFill(Color.WHITE);
        lblTotalBushes = new Label("🌿 Bushes: 0"); lblTotalBushes.setTextFill(Color.WHITE);
        lblTotalTrees = new Label("🌳 Trees: 0"); lblTotalTrees.setTextFill(Color.WHITE);

        lblEcoStatus = new Label("🟢 Status: Stable");
        lblEcoStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblEcoStatus.setTextFill(Color.LIGHTGREEN);

        dashBox.getChildren().addAll(lblTotalRabbits, lblTotalTigers, lblTotalWolves, lblTotalElephants, lblTotalFishes, lblTotalBushes, lblTotalTrees, lblEcoStatus);
        Separator sep3 = new Separator();

        lblMouseCoords = new Label("Tile Coords: Hover over map...");
        lblMouseCoords.setTextFill(Color.ORANGE);

        panel.getChildren().addAll(lblTitle, lblClock, lblSeason, lblPartOfDay, sep1, lblActionTitle, actionBox, sep2, lblDashTitle, dashBox, sep3, lblMouseCoords);
        return panel;
    }

    private void updateTimeInformation() {
        if (sharedSimulationManager == null) return;
        try {
            int year = (int) core.TimeSystem.class.getDeclaredField("year").get(null);
            int month = (int) core.TimeSystem.class.getDeclaredField("month").get(null);
            int day = (int) core.TimeSystem.class.getDeclaredField("day").get(null);
            int hour = (int) core.TimeSystem.class.getDeclaredField("hour").get(null);
            int minute = (int) core.TimeSystem.class.getDeclaredField("minute").get(null);
            String season = (String) core.TimeSystem.class.getDeclaredField("season").get(null);
            String partOfDay = (String) core.TimeSystem.class.getDeclaredField("partOfDay").get(null);

            lblClock.setText(String.format("Time: %02d:%02d (Day %02d/%02d/%d)", hour, minute, day, month, year));
            lblSeason.setText("Current Season: " + season);
            lblPartOfDay.setText("Light Cycle: " + partOfDay);

            updateDashboard();
        } catch (Exception e) {
            lblClock.setText("Time: Sync Error...");
        }
    }

    private void updateDashboard() {
        if (worldMap == null) return;
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return;

        int rCount = 0, tCount = 0, wCount = 0, eCount = 0, fCount = 0, bushCount = 0, treeCount = 0;

        for (Chunk[] row : chunkMap) {
            for (Chunk chunk : row) {
                if (chunk == null) continue;
                synchronized (chunk.getEntityList()) {
                    for (Entity entity : chunk.getEntityList()) {
                        if (entity != null && entity.checkAlive()) {
                            if (entity instanceof entities.Rabbit) rCount++;
                            else if (entity instanceof entities.Tiger) tCount++;
                            else if (entity instanceof entities.Wolf) wCount++;
                            else if (entity instanceof entities.Elephant) eCount++;
                            else if (entity instanceof entities.Fish) fCount++;
                            else if (entity instanceof Bush) bushCount++;
                            else if (entity instanceof Trees) treeCount++;
                        }
                    }
                }
            }
        }

        final int r = rCount, t = tCount, w = wCount, elephant = eCount, f = fCount, b = bushCount, tr = treeCount;
        javafx.application.Platform.runLater(() -> {
            if (lblTotalRabbits != null) lblTotalRabbits.setText("🐇 Rabbits: " + r);
            if (lblTotalTigers != null) lblTotalTigers.setText("🐅 Tigers: " + t);
            if (lblTotalWolves != null) lblTotalWolves.setText("🐺 Wolves: " + w);
            if (lblTotalElephants != null) lblTotalElephants.setText("🐘 Elephants: " + elephant);
            if (lblTotalFishes != null) lblTotalFishes.setText("🐟 Fishes: " + f);
            if (lblTotalBushes != null) lblTotalBushes.setText("🌿 Bushes: " + b);
            if (lblTotalTrees != null) lblTotalTrees.setText("🌳 Trees: " + tr);

            if (lblEcoStatus != null) {
                int totalAnimals = r + t + w + elephant + f;
                if (totalAnimals == 0) {
                    lblEcoStatus.setText("🔴 Status: Extinct"); lblEcoStatus.setTextFill(Color.RED);
                } else if (r < 5 || (b + tr) < 10) {
                    lblEcoStatus.setText("⚠️ Status: Imbalanced"); lblEcoStatus.setTextFill(Color.ORANGE);
                } else {
                    lblEcoStatus.setText("🟢 Status: Stable"); lblEcoStatus.setTextFill(Color.LIGHTGREEN);
                }
            }
        });
    }

    private Entity findEntityAt(int gridX, int gridY) {
        if (worldMap == null) return null;
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return null;

        for (int cy = 0; cy < chunkMap.length; cy++) {
            for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                Chunk chunk = chunkMap[cy][cx];
                if (chunk == null) continue;
                synchronized (chunk.getEntityList()) {
                    for (Entity entity : chunk.getEntityList()) {
                        if (entity != null && entity.checkAlive() && entity.getX() == gridX && entity.getY() == gridY) {
                            return entity;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void refreshSelectedAnimalDebug() {
        if (lblSelectedAnimalDebug == null) return;

        if (selectedAnimal == null || !selectedAnimal.checkAlive()) {
            lblSelectedAnimalDebug.setText("No animal selected. Click an animal on the map to inspect its movement state.");
            if (basicRenderer != null) basicRenderer.setSelectedAnimal(null);
            if (selectedAnimal == null && selectedChunk == null) {
                if (basicRenderer != null) basicRenderer.setSelectedChunk(null);
            }
            return;
        }

        selectedChunk = findChunkAt(selectedAnimal.getX(), selectedAnimal.getY());

        brain.controller.AnimalBrainUpdate brain = (sharedSimulationManager != null) ?
                sharedSimulationManager.getBrainMap().get(selectedAnimal) : brainMap.get(selectedAnimal);
        String strategyName = (brain != null) ? brain.getCurrentStrategyName() : selectedAnimal.getMoveStrategyName();
        Position target = (brain != null) ? brain.getCurrentAnchorTarget() : selectedAnimal.getLastLockedTargetPosition();
        java.util.List<Position> path = (brain != null) ? brain.getCurrentPath() : java.util.Collections.emptyList();
        int nextStepIndex = 0;
        if (path != null && !path.isEmpty()) {
            nextStepIndex = path.size();
        }

        StringBuilder content = new StringBuilder();
        content.append("Type: ").append(selectedAnimal.getClass().getSimpleName()).append("\n");
        content.append("Position: (").append(selectedAnimal.getX()).append(", ").append(selectedAnimal.getY()).append(")\n");
        content.append("Chunk: [").append(selectedAnimal.getX() / WorldMap.CHUNK_SIZE).append(", ").append(selectedAnimal.getY() / WorldMap.CHUNK_SIZE).append("]\n");
        content.append("Hunger: ").append(String.format("%.1f", selectedAnimal.getHunger())).append(" / Thirst: ").append(String.format("%.1f", selectedAnimal.getThirst())).append("\n");
        content.append("Cooldown: ").append(selectedAnimal.getCurrentMoveCooldown()).append("\n");
        content.append("Strategy: ").append(strategyName).append("\n");
        content.append("Current target: ").append(target == null ? "None" : "(" + target.getX() + ", " + target.getY() + ")").append("\n");
        content.append("Path remaining: ").append(path == null ? "0" : path.size()).append("\n");
        content.append("Next step: ").append(path == null || path.isEmpty() ? "None" : "(" + path.get(0).getX() + ", " + path.get(0).getY() + ")").append("\n");

        lblSelectedAnimalDebug.setText(content.toString());
        if (basicRenderer != null) basicRenderer.setSelectedAnimal(selectedAnimal);
    }

    private Chunk findChunkAt(int gridX, int gridY) {
        Chunk[][] chunkMap = worldMap.chunkMap;
        if (chunkMap == null) return null;
        int chunkX = gridX / WorldMap.CHUNK_SIZE;
        int chunkY = gridY / WorldMap.CHUNK_SIZE;
        if (chunkY >= 0 && chunkY < chunkMap.length && chunkX >= 0 && chunkX < chunkMap[chunkY].length) {
            return chunkMap[chunkY][chunkX];
        }
        return null;
    }

    private void refreshSelectedChunkDebug() {
        if (lblSelectedChunkDebug == null) return;

        if (selectedChunk == null) {
            lblSelectedChunkDebug.setText("No chunk selected. Click any empty tile or animal to inspect the chunk contents.");
            if (basicRenderer != null) basicRenderer.setSelectedChunk(null);
            return;
        }

        StringBuilder chunkSummary = new StringBuilder();
        chunkSummary.append("Chunk index: [").append(selectedChunkHashX()).append(", ").append(selectedChunkHashY()).append("]\n");
        chunkSummary.append("Distance to water: ").append(selectedChunk.getDistanceToWater()).append("\n");
        chunkSummary.append("Total entities: ").append(selectedChunk.getEntityList().size()).append("\n");

        lblSelectedChunkDebug.setText(chunkSummary.toString().trim());
        if (basicRenderer != null) basicRenderer.setSelectedChunk(selectedChunk);
    }

    private int selectedChunkHashX() {
        if (worldMap == null || selectedChunk == null) return -1;
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return -1;
        for (int cy = 0; cy < chunkMap.length; cy++) {
            for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                if (chunkMap[cy][cx] == selectedChunk) return cx;
            }
        }
        return -1;
    }

    private int selectedChunkHashY() {
        if (worldMap == null || selectedChunk == null) return -1;
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return -1;
        for (int cy = 0; cy < chunkMap.length; cy++) {
            for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                if (chunkMap[cy][cx] == selectedChunk) return cy;
            }
        }
        return -1;
    }

    private void drawMap(Canvas canvas) {
        basicRenderer.setSelectedAnimal(selectedAnimal);
        basicRenderer.setSelectedChunk(selectedChunk);
        
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        basicRenderer.renderTerrain(canvas, scale, offsetX, offsetY);
        basicRenderer.renderEntities(canvas, scale, offsetX, offsetY);
    }
}