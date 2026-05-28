package view;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
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
import core.enviroment.*;
import core.TimeSystem;
import entities.base.Entity;
import entities.base.Animals;
import entities.Bush;
import entities.Trees;
import entities.Rabbit;
import entities.Tiger;
import entities.Wolf;
import entities.Elephant;
import entities.Fish;

import java.lang.reflect.Field;
import java.util.List;

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

    // --- DASHBOARD REALTIME HIỂN THỊ ĐẦY ĐỦ TỪNG LOÀI ---
    private Label lblTotalRabbits;
    private Label lblTotalTigers;
    private Label lblTotalWolves;
    private Label lblTotalElephants;
    private Label lblTotalFishes;
    private Label lblTotalBushes;
    private Label lblTotalTrees;
    private Label lblEcoStatus;

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo bản đồ thế giới ngầm với Seed cố định
        worldMap = new WorldMap(940320);
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

        // Sử dụng cấu trúc bố cục BorderPane chia cắt rõ ràng
        BorderPane root = new BorderPane();
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
                                targetChunk.addEntity(new entities.Rabbit("Manual Rabbit", gridX, gridY));
                            }
                            else if ("SPAWN_TIGER".equals(currentInteractionMode)) {
                                if (isWater || isStone) return;
                                targetChunk.addEntity(new entities.Tiger("Manual Tiger", gridX, gridY));
                            }
                            else if ("SPAWN_WOLF".equals(currentInteractionMode)) {
                                if (isWater || isStone) return;
                                targetChunk.addEntity(new entities.Wolf("Manual Wolf", gridX, gridY));
                            }
                            else if ("SPAWN_ELEPHANT".equals(currentInteractionMode)) {
                                if (isWater || isStone) return;
                                targetChunk.addEntity(new entities.Elephant("Manual Elephant", gridX, gridY));
                            }
                            else if ("SPAWN_FISH".equals(currentInteractionMode)) {
                                if (!isWater) return;
                                targetChunk.addEntity(new entities.Fish("Manual Fish", gridX, gridY));
                            }
                            else if ("SPAWN_BUSH".equals(currentInteractionMode)) {
                                if (isWater || isStone || isMud) return;
                                targetChunk.addEntity(new entities.Bush("Manual Bush", gridX, gridY));
                            }
                            else if ("SPAWN_TREE".equals(currentInteractionMode)) {
                                if (isWater || isStone || isMud) return;
                                targetChunk.addEntity(new entities.Trees("Manual Tree", gridX, gridY));
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Mouse interaction error: " + ex.getMessage());
                    }
                }
            }
        });

        // Gọi hàm kích hoạt rải đầy đủ muông thú ngẫu nhiên lúc vừa bật map
        injectTestEntities();

        Scene scene = new Scene(root, 1180, 820);
        primaryStage.setTitle("Ecosystem Monitor - Full Random Biome Mode");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Vòng lặp đồ họa và logic thời gian thực ngầm định
        AnimationTimer viewRefresher = new AnimationTimer() {
            private long lastTimeUpdate = 0;
            private long lastLogicUpdate = 0;

            @Override
            public void handle(long now) {
                // Tăng tiến thời gian hệ thống giả lập ngầm định
                if (now - lastTimeUpdate >= 200_000_000) {
                    try {
                        int m = core.TimeSystem.minute + 5;
                        if (m >= 60) {
                            m = 0;
                            core.TimeSystem.hour++;
                            if (core.TimeSystem.hour >= 24) {
                                core.TimeSystem.hour = 0;
                                core.TimeSystem.day++;
                            }
                        }
                        core.TimeSystem.minute = m;
                        core.TimeSystem.partOfDay = (core.TimeSystem.hour > 4 && core.TimeSystem.hour < 18) ? "Day" : "Night";
                    } catch (Exception e) {}
                    lastTimeUpdate = now;
                }

                // Cập nhật nhịp vòng lặp Logic của Backend định kỳ 1 giây một lần
                if (now - lastLogicUpdate >= 1_000_000_000) {
                    updateSimulationLogic();
                    lastLogicUpdate = now;
                }

                updateTimeInformation();
                drawMap(canvas);
            }
        };
        viewRefresher.start();
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
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Rabbit("Wild Rabbit " + countR, rx, ry));
                    countR++;
                }
            }
            // Thả Hổ
            for (int countT = 0; countT < spawnTigers; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Tiger("Apex Tiger " + countT, rx, ry));
                    countT++;
                }
            }
            // Thả Sói
            for (int countW = 0; countW < spawnWolves; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Wolf("Grey Wolf " + countW, rx, ry));
                    countW++;
                }
            }
            // Thả Voi
            for (int countE = 0; countE < spawnElephants; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Elephant("Wild Elephant " + countE, rx, ry));
                    countE++;
                }
            }
            // Thả Cá (Bắt buộc dưới nước)
            for (int countF = 0; countF < spawnFishes; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (name.contains("water") || name.contains("nuoc")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Fish("Aqua Fish " + countF, rx, ry));
                    countF++;
                }
            }
            // Gieo Bụi Cỏ (Tránh Nước, Đá, Bùn)
            for (int countB = 0; countB < spawnBushes; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da") && !name.contains("mud") && !name.contains("bun")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Bush("Grass Bush " + countB, rx, ry));
                    countB++;
                }
            }
            // Trồng Cây (Tránh Nước, Đá, Bùn)
            for (int countTree = 0; countTree < spawnTrees; ) {
                int rx = rand.nextInt(GRID_SIZE), ry = rand.nextInt(GRID_SIZE);
                String name = getTileType.apply(rx, ry);
                if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da") && !name.contains("mud") && !name.contains("bun")) {
                    chunkMap[ry / 50][rx / 50].addEntity(new entities.Trees("Ancient Tree " + countTree, rx, ry));
                    countTree++;
                }
            }
            System.out.println("🟢 Initial full ecosystem random setup successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawMap(Canvas canvas) {
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

            for (int cy = 0; cy < 10; cy++) {
                for (int cx = 0; cx < 10; cx++) {
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

            brain.controller.StateController stateController = new brain.controller.StateController();

            for (int cy = 0; cy < 10; cy++) {
                for (int cx = 0; cx < 10; cx++) {
                    Chunk chunk = chunkMap[cy][cx];
                    if (chunk == null) continue;

                    List<Entity> entityList = chunk.getEntityList();
                    synchronized (entityList) {
                        for (int i = entityList.size() - 1; i >= 0; i--) {
                            Entity entity = entityList.get(i);
                            if (entity == null || !entity.checkAlive()) continue;

                            if (entity instanceof Animals) {
                                Animals animal = (Animals) entity;
                                animal.updateMoveCooldown(animalCoordinates, allEntities);

                                Field fieldCooldown = Animals.class.getDeclaredField("currentMoveCooldown");
                                fieldCooldown.setAccessible(true);
                                int cooldown = (int) fieldCooldown.get(animal);

                                if (cooldown <= 0 && animal.checkAlive()) {
                                    stateController.updateState(animal, allEntities);

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