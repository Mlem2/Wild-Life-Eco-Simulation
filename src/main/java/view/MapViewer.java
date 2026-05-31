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
    private static final int GRID_SIZE = 500;
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
            worldMap = new WorldMap(94033111, 500);
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

            double minScaleX = canvas.getWidth() / (GRID_SIZE * baseTileSize);
            double minScaleY = canvas.getHeight() / (GRID_SIZE * baseTileSize);
            double minScale = Math.max(minScaleX, minScaleY);
            double maxScale = 50.0;

            newScale = Math.max(minScale, Math.min(maxScale, newScale));
            if (newScale == oldScale) return;

            scale = newScale;
            offsetX = event.getX() - (event.getX() - offsetX) * (scale / oldScale);
            offsetY = event.getY() - (event.getY() - offsetY) * (scale / oldScale);

            double mapWidth = GRID_SIZE * (baseTileSize * scale);
            double mapHeight = GRID_SIZE * (baseTileSize * scale);
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
            double mapWidth = GRID_SIZE * (baseTileSize * scale);
            double mapHeight = GRID_SIZE * (baseTileSize * scale);

            offsetX = Math.min(0, Math.max(canvas.getWidth() - mapWidth, offsetX + dx));
            offsetY = Math.min(0, Math.max(canvas.getHeight() - mapHeight, offsetY + dy));

            lastMouseX = e.getX();
            lastMouseY = e.getY();
            drawMap(canvas);
        });

        canvas.setOnMouseMoved(e -> {
            int gridX = (int) ((e.getX() - offsetX) / scale);
            int gridY = (int) ((e.getY() - offsetY) / scale);
            if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
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

                if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
                    Entity clickedEntity = findEntityAt(gridX, gridY);
                    if ("VIEW_MAP".equals(currentInteractionMode)) {
                        handleSelectionClick(gridX, gridY, clickedEntity);
                        refreshSelectedAnimalDebug();
                        refreshSelectedChunkDebug();
                        drawMap((Canvas) e.getSource());
                        return;
                    }

                    int chunkX = gridX / 50;
                    int chunkY = gridY / 50;

                    try {
                        Field fieldChunk = WorldMap.class.getDeclaredField("chunkMap");
                        fieldChunk.setAccessible(true);
                        Chunk[][] chunkMap = (Chunk[][]) fieldChunk.get(worldMap);

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
                    } catch (Exception ex) {
                        System.err.println("Mouse interaction error: " + ex.getMessage());
                    }
                }
            }
        });

        // Gọi hàm kích hoạt rải đầy đủ muông thú ngẫu nhiên lúc vừa bật map
        if (sharedWorldMap == null) {
            injectTestEntities();
        }

        // Đăng ký bộ não cho tất cả các động vật đã spawn
        if (sharedSimulationManager == null) {
            try {
                Field field = WorldMap.class.getDeclaredField("chunkMap");
                field.setAccessible(true);
                Chunk[][] chunkMap = (Chunk[][]) field.get(worldMap);
                registerAllBrains(chunkMap);
            } catch (Exception e) {
            }
        }

        Scene scene = new Scene(root, 1180, 820);
        primaryStage.setTitle("Ecosystem Monitor - Full Random Biome Mode");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Vòng lặp đồ họa và logic thời gian thực ngầm định
        AnimationTimer viewRefresher = new AnimationTimer() {
            private long lastLogicUpdate = 0;

            @Override
            public void handle(long now) {
                // Nếu SimulationManager không chạy (chạy từ MapViewer trực tiếp), ta tự update logic
                if (sharedSimulationManager == null) {
                    // Cập nhật nhịp vòng lặp Logic của Backend định kỳ 1 giây một lần
                    if (now - lastLogicUpdate >= 1_000_000_000) {
                        updateSimulationLogic();
                        lastLogicUpdate = now;
                    }
                }

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
        try {
            int year = (int) TimeSystem.class.getDeclaredField("year").get(null);
            int month = (int) TimeSystem.class.getDeclaredField("month").get(null);
            int day = (int) TimeSystem.class.getDeclaredField("day").get(null);
            int hour = (int) TimeSystem.class.getDeclaredField("hour").get(null);
            int minute = (int) TimeSystem.class.getDeclaredField("minute").get(null);
            String season = (String) TimeSystem.class.getDeclaredField("season").get(null);
            String partOfDay = (String) TimeSystem.class.getDeclaredField("partOfDay").get(null);

            lblClock.setText(String.format("Time: %02d:%02d (Day %02d/%02d/%d)", hour, minute, day, month, year));
            lblSeason.setText("Current Season: " + season);
            lblPartOfDay.setText("Light Cycle: " + partOfDay);
        } catch (Exception e) {
            lblClock.setText("Time: Sync Error...");
        }
    }

    // --- KHỞI TẠO NGẪU NHIÊN ĐẦY ĐỦ TẤT CẢ CÁC LOÀI ĐÚNG BIOME KHI MỞ GAME ---
    private void injectTestEntities() {
        try {
            Field field = WorldMap.class.getDeclaredField("chunkMap");
            field.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) field.get(worldMap);
            if (chunkMap == null) return;

            java.util.Random rand = new java.util.Random();

            // Cấu hình số lượng sinh ban đầu cho từng loài độc lập
            int spawnRabbits = 150;
            int spawnTigers = 20;
            int spawnWolves = 35;
            int spawnElephants = 15;
            int spawnFishes = 80;
            int spawnBushes = 400;
            int spawnTrees = 300;

            java.util.function.BiFunction<Integer, Integer, String> getTileType = (x, y) -> {
                try { return worldMap.getTile(x, y).getName().toLowerCase(); } catch (Exception e) { return ""; }
            };

            // Thả Thỏ
            for (int countR = 0; countR < spawnRabbits; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Rabbit(rx, ry));
                    countR++;
                }
            }
            // Thả Hổ
            for (int countT = 0; countT < spawnTigers; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Tiger(rx, ry));
                    countT++;
                }
            }
            // Thả Sói
            for (int countW = 0; countW < spawnWolves; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Wolf(rx, ry));
                    countW++;
                }
            }
            // Thả Voi
            for (int countE = 0; countE < spawnElephants; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Elephant(rx, ry));
                    countE++;
                }
            }
            // Thả Cá (Bắt buộc dưới nước)
            for (int countF = 0; countF < spawnFishes; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (name.contains("water") || name.contains("nuoc")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Fish(rx, ry));
                    countF++;
                }
            }
            // Gieo Bụi Cỏ (Tránh Nước, Đá, Bùn)
            for (int countB = 0; countB < spawnBushes; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da") && !name.contains("mud") && !name.contains("bun")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Bush(rx, ry));
                    countB++;
                }
            }
            // Trồng Cây (Tránh Nước, Đá, Bùn)
            for (int countTree = 0; countTree < spawnTrees; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da") && !name.contains("mud") && !name.contains("bun")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Trees(rx, ry));
                    countTree++;
                }
            }
            System.out.println("🟢 Initial full ecosystem random setup successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Entity findEntityAt(int gridX, int gridY) {
        try {
            Field fieldChunk = WorldMap.class.getDeclaredField("chunkMap");
            fieldChunk.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) fieldChunk.get(worldMap);
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
        } catch (Exception ex) {
            System.err.println("findEntityAt error: " + ex.getMessage());
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
        content.append("Chunk: [").append(selectedAnimal.getX() / 50).append(", ").append(selectedAnimal.getY() / 50).append("]\n");
        content.append("Hunger: ").append(String.format("%.1f", selectedAnimal.getHunger())).append(" / Thirst: ").append(String.format("%.1f", selectedAnimal.getThirst())).append("\n");
        content.append("Cooldown: ").append(selectedAnimal.getCurrentMoveCooldown()).append("\n");
        content.append("Strategy: ").append(strategyName).append("\n");
        content.append("Current target: ").append(target == null ? "None" : "(" + target.getX() + ", " + target.getY() + ")").append("\n");
        content.append("Path remaining: ").append(path == null ? "0" : path.size()).append("\n");
        if (path != null && !path.isEmpty()) {
            Position nextStep = path.get(0);
            content.append("Next step: (").append(nextStep.getX()).append(", ").append(nextStep.getY()).append(")\n");
        } else {
            content.append("Next step: None\n");
        }
        content.append(buildVisibleChunkSummary(selectedAnimal));

        lblSelectedAnimalDebug.setText(content.toString());
        if (basicRenderer != null) basicRenderer.setSelectedAnimal(selectedAnimal);
    }

    private String buildVisibleChunkSummary(Animals animal) {
        try {
            brain.controller.MapSystem ms = new brain.controller.MapSystem(worldMap);
            java.util.List<Chunk> visibleChunks = ms.getVisibleChunks(animal.getPosition());
            int totalEntities = 0;
            int rabbits = 0, tigers = 0, wolves = 0, elephants = 0, fishes = 0, bushes = 0, trees = 0;
            for (Chunk chunk : visibleChunks) {
                if (chunk == null) continue;
                synchronized (chunk.getEntityList()) {
                    for (Entity entity : chunk.getEntityList()) {
                        if (entity == null || !entity.checkAlive()) continue;
                        totalEntities++;
                        if (entity instanceof entities.Rabbit) rabbits++;
                        else if (entity instanceof entities.Tiger) tigers++;
                        else if (entity instanceof entities.Wolf) wolves++;
                        else if (entity instanceof entities.Elephant) elephants++;
                        else if (entity instanceof entities.Fish) fishes++;
                        else if (entity instanceof Bush) bushes++;
                        else if (entity instanceof Trees) trees++;
                    }
                }
            }
            return String.format("Visible chunks: %d | Entities: %d | Rabbits: %d, Tigers: %d, Wolves: %d, Elephants: %d, Fish: %d, Bushes: %d, Trees: %d",
                    visibleChunks.size(), totalEntities, rabbits, tigers, wolves, elephants, fishes, bushes, trees);
        } catch (Exception e) {
            return "Visible chunks: unavailable";
        }
    }

    private Chunk findChunkAt(int gridX, int gridY) {
        try {
            Field fieldChunk = WorldMap.class.getDeclaredField("chunkMap");
            fieldChunk.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) fieldChunk.get(worldMap);
            if (chunkMap == null) return null;
            int chunkX = gridX / 50;
            int chunkY = gridY / 50;
            if (chunkY >= 0 && chunkY < chunkMap.length && chunkX >= 0 && chunkX < chunkMap[chunkY].length) {
                return chunkMap[chunkY][chunkX];
            }
        } catch (Exception ex) {
            System.err.println("findChunkAt error: " + ex.getMessage());
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

        java.util.Map<String, Integer> typeCounts = new java.util.LinkedHashMap<>();
        java.util.List<String> sampleEntities = new java.util.ArrayList<>();

        synchronized (selectedChunk.getEntityList()) {
            for (Entity entity : selectedChunk.getEntityList()) {
                if (entity == null || !entity.checkAlive()) continue;
                String typeName = entity.getClass().getSimpleName();
                typeCounts.put(typeName, typeCounts.getOrDefault(typeName, 0) + 1);
                if (sampleEntities.size() < 12) {
                    sampleEntities.add(entity.getClass().getSimpleName());
                }
            }
        }

        StringBuilder chunkSummary = new StringBuilder();
        chunkSummary.append("Chunk index: [").append(selectedChunkHashX()).append(", ").append(selectedChunkHashY()).append("]\n");
        chunkSummary.append("Distance to water: ").append(selectedChunk.getDistanceToWater()).append("\n");
        chunkSummary.append("Total entities: ").append(selectedChunk.getEntityList().size()).append("\n");
        chunkSummary.append("Types: ");
        if (typeCounts.isEmpty()) {
            chunkSummary.append("None\n");
        } else {
            java.util.StringJoiner joiner = new java.util.StringJoiner(", ");
            for (java.util.Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                joiner.add(entry.getKey() + "=" + entry.getValue());
            }
            chunkSummary.append(joiner.toString()).append("\n");
        }
        chunkSummary.append("Sample entities:\n");
        if (sampleEntities.isEmpty()) {
            chunkSummary.append("- none");
        } else {
            for (String sample : sampleEntities) {
                chunkSummary.append("- ").append(sample).append("\n");
            }
        }

        lblSelectedChunkDebug.setText(chunkSummary.toString().trim());
        if (basicRenderer != null) basicRenderer.setSelectedChunk(selectedChunk);
    }

    private int selectedChunkHashX() {
        try {
            Field fieldChunk = WorldMap.class.getDeclaredField("chunkMap");
            fieldChunk.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) fieldChunk.get(worldMap);
            if (chunkMap == null || selectedChunk == null) return -1;
            for (int cy = 0; cy < chunkMap.length; cy++) {
                for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                    if (chunkMap[cy][cx] == selectedChunk) return cx;
                }
            }
        } catch (Exception ex) {}
        return -1;
    }

    private int selectedChunkHashY() {
        try {
            Field fieldChunk = WorldMap.class.getDeclaredField("chunkMap");
            fieldChunk.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) fieldChunk.get(worldMap);
            if (chunkMap == null || selectedChunk == null) return -1;
            for (int cy = 0; cy < chunkMap.length; cy++) {
                for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                    if (chunkMap[cy][cx] == selectedChunk) return cy;
                }
            }
        } catch (Exception ex) {}
        return -1;
    }

    private void drawMap(Canvas canvas) {
        basicRenderer.setSelectedAnimal(selectedAnimal);
        basicRenderer.setSelectedChunk(selectedChunk);
        basicRenderer.render(canvas, scale, offsetX, offsetY);
    }

    // --- HÀM CẬP NHẬT LOGIC: ĐÃ MỞ RỘNG ĐẾM CHI TIẾT VÀ KIỂM DUYỆT ĐỊA HÌNH KHI DI CHUYỂN ---
    private void updateSimulationLogic() {
        try {
            Field fieldChunk = WorldMap.class.getDeclaredField("chunkMap");
            fieldChunk.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) fieldChunk.get(worldMap);
            if (chunkMap == null) return;

            Entity[][] animalCoordinates = new Entity[GRID_SIZE][GRID_SIZE];
            List<Entity> allEntities = new java.util.ArrayList<>();

            // Biến đếm đầy đủ cho từng loài cụ thể để cập nhật lên Dashboard
            int rCount = 0, tCount = 0, wCount = 0, eCount = 0, fCount = 0, bushCount = 0, treeCount = 0;

            for (int cy = 0; cy < chunkMap.length; cy++) {
                for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                    Chunk chunk = chunkMap[cy][cx];
                    if (chunk == null) continue;
                    synchronized (chunk.getEntityList()) {
                        for (Entity entity : chunk.getEntityList()) {
                            if (entity != null && entity.checkAlive()) {
                                allEntities.add(entity);
                                if (entity instanceof Animals) {
                                    animalCoordinates[entity.getX()][entity.getY()] = entity;
                                    if (entity instanceof entities.Rabbit) rCount++;
                                    else if (entity instanceof entities.Tiger) tCount++;
                                    else if (entity instanceof entities.Wolf) wCount++;
                                    else if (entity instanceof entities.Elephant) eCount++;
                                    else if (entity instanceof entities.Fish) fCount++;
                                } else if (entity instanceof Bush) {
                                    bushCount++;
                                } else if (entity instanceof Trees) {
                                    treeCount++;
                                }
                            }
                        }
                    }
                }
            }

            // Đồng bộ đẩy toàn bộ số lượng thực tế lên Dashboard JavaFX bằng tiếng Anh
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

            // StateController not available here; skip external state update

            for (int cy = 0; cy < chunkMap.length; cy++) {
                for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                    Chunk chunk = chunkMap[cy][cx];
                    if (chunk == null) continue;

                    List<Entity> entityList = chunk.getEntityList();
                    synchronized (entityList) {
                        for (int i = entityList.size() - 1; i >= 0; i--) {
                            Entity entity = entityList.get(i);
                            if (entity == null || !entity.checkAlive()) continue;

                            if (entity instanceof entities.base.Tree tree) {
                                tree.checkCD(animalCoordinates, allEntities);
                            }

                            if (entity instanceof entities.base.ResourceEntity resource) {
                                resource.updateResourceState();
                            }

                            if (entity instanceof Animals) {
                                Animals animal = (Animals) entity;
                                animal.updateMoveCooldown(animalCoordinates, allEntities);

                                Field fieldCooldown = Animals.class.getDeclaredField("currentMoveCooldown");
                                fieldCooldown.setAccessible(true);
                                int cooldown = (int) fieldCooldown.get(animal);

                                if (cooldown <= 0 && animal.checkAlive()) {
                                    // Prefer brain-driven movement if a brain is registered for this animal
                                    brain.controller.AnimalBrainUpdate brain = brainMap.get(animal);
                                    if (brain != null) {
                                        brain.update();
                                    } else {
                                        Field fieldStrategy = Animals.class.getDeclaredField("moveStrategy");
                                        fieldStrategy.setAccessible(true);
                                        brain.strategy.MoveStrategy strategy = (brain.strategy.MoveStrategy) fieldStrategy.get(animal);

                                        if (strategy != null) {
                                            allEnum.Direction dir = strategy.move(animal, allEntities);
                                            if (dir != null && dir != allEnum.Direction.CENTER) {
                                                Field fieldX = Entity.class.getDeclaredField("x");
                                                Field fieldY = Entity.class.getDeclaredField("y");
                                                fieldX.setAccessible(true);
                                                fieldY.setAccessible(true);

                                                int curX = (int) fieldX.get(animal);
                                                int curY = (int) fieldY.get(animal);
                                                int nextX = curX, nextY = curY;

                                                switch (dir) {
                                                    case NORTH:     nextY--; break;
                                                    case SOUTH:     nextY++; break;
                                                    case EAST:      nextX++; break;
                                                    case WEST:      nextX--; break;
                                                    case NORTHEAST: nextX++; nextY--; break;
                                                    case NORTHWEST: nextX--; nextY--; break;
                                                    case SOUTHEAST: nextX++; nextY++; break;
                                                    case SOUTHWEST: nextX--; nextY++; break;
                                                    default: break;
                                                }

                                                nextX = Math.max(0, Math.min(499, nextX));
                                                nextY = Math.max(0, Math.min(499, nextY));

                                                // CHẶN DI CHUYỂN QUA LẠI SAI BIOME ĐỊA HÌNH
                                                try {
                                                    var nextTile = worldMap.getTile(nextX, nextY);
                                                    String tName = (nextTile != null && nextTile.getName() != null) ? nextTile.getName().toLowerCase() : "";
                                                    boolean water = tName.contains("water") || tName.contains("nuoc");
                                                    boolean stone = tName.contains("stone") || tName.contains("da") || tName.contains("mountain");

                                                    if (!(animal instanceof entities.Fish)) {
                                                        if (water || stone) { nextX = curX; nextY = curY; }
                                                    } else {
                                                        if (!water) { nextX = curX; nextY = curY; }
                                                    }
                                                } catch (Exception ex) {}

                                                fieldX.set(animal, nextX);
                                                fieldY.set(animal, nextY);
                                            }
                                        }
                                        Field fieldDefault = Animals.class.getDeclaredField("defaultMoveCooldown");
                                        fieldDefault.setAccessible(true);
                                        int defaultCooldown = (int) fieldDefault.get(animal);
                                        fieldCooldown.set(animal, defaultCooldown > 0 ? defaultCooldown : 3);
                                    }
                                }

                                // Logic ăn cỏ giải đói
                                synchronized (entityList) {
                                    for (int j = entityList.size() - 1; j >= 0; j--) {
                                        Entity target = entityList.get(j);
                                        if (target != null && target != animal && target.getX() == animal.getX() && target.getY() == animal.getY()) {
                                            if (animal instanceof entities.attributes.Herbivore && (target instanceof Bush || target instanceof Trees)) {
                                                Field fieldHunger = Animals.class.getDeclaredField("hunger");
                                                Field fieldThirst = Animals.class.getDeclaredField("thirst");
                                                fieldHunger.setAccessible(true);
                                                fieldThirst.setAccessible(true);

                                                fieldHunger.set(animal, Math.min(100.0, (double) fieldHunger.get(animal) + 40.0));
                                                fieldThirst.set(animal, Math.min(100.0, (double) fieldThirst.get(animal) + 20.0));

                                                Field fieldAlive = Entity.class.getDeclaredField("isAlive");
                                                fieldAlive.setAccessible(true);
                                                fieldAlive.set(target, false);
                                                entityList.remove(j);
                                            }
                                        }
                                    }
                                }

                                // Hồi khát khi đi qua sông lạch
                                try {
                                    String tileName = worldMap.getTile(animal.getX(), animal.getY()).getName().toLowerCase();
                                    if (tileName.contains("water") || tileName.contains("nuoc")) {
                                        Field fieldThirst = Animals.class.getDeclaredField("thirst");
                                        fieldThirst.setAccessible(true);
                                        fieldThirst.set(animal, 100.0);
                                    }
                                } catch (Exception ex) {}

                                // Chuyển vùng Chunk quản lý
                                int newChunkX = entity.getX() / 50;
                                int newChunkY = entity.getY() / 50;
                                if (newChunkX != cx || newChunkY != cy) {
                                    if (newChunkX >= 0 && newChunkX < 10 && newChunkY >= 0 && newChunkY < 10) {
                                        Chunk newChunk = chunkMap[newChunkY][newChunkX];
                                        if (newChunk != null) {
                                            newChunk.addEntity(entity);
                                            entityList.remove(i);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Simulation loop error: " + e.getMessage());
        }
    }
}