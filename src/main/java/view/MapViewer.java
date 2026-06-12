package view;

import core.enviroment.WorldMap;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MapViewer extends Application {
    public static MapViewer instance;
    private Stage primaryStage;
    private WorldMap worldMap;

    private static WorldMap sharedWorldMap;

    // Trạng thái zoom/offset dùng chung giữa hai chế độ hiển thị
    private double viewScale = 1.0;
    private double viewOffsetX = 0.0;
    private double viewOffsetY = 0.0;
    private double viewCenterX = WorldMap.SIZE / 2.0;
    private double viewCenterY = WorldMap.SIZE / 2.0;
    private double basicViewScale = 1.0;
    private double graphicViewScale = 2.5;

    // Luồng mô phỏng duy nhất, dùng chung toàn cục xuyên suốt cả BasicMode lẫn GraphicMode
    private brain.controller.SimulationManager currentSimulationManager;

    // Thuộc tính static cũ để giữ tương thích ngược với Main.java hoặc BasicModeView
    private static brain.controller.SimulationManager sharedSimulationManager;

    public static void setSharedWorldMap(WorldMap worldMap) {
        sharedWorldMap = worldMap;
    }

    // KHÔI PHỤC HÀM CŨ: Để file Main.java không bị lỗi dòng 21
    public static void setSharedSimulationManager(brain.controller.SimulationManager simulationManager) {
        sharedSimulationManager = simulationManager;
        if (instance != null && simulationManager != null) {
            instance.currentSimulationManager = simulationManager;
        }
    }

    // KHÔI PHỤC HÀM CŨ: Để file BasicModeView.java lấy được bản đồ dữ liệu ở dòng 59
    public WorldMap getWorldMap() {
        return this.worldMap;
    }

    // KHÔI PHỤC HÀM CŨ: Để file BasicModeView.java lấy được luồng mô phỏng dùng chung
    public brain.controller.SimulationManager getSharedSimulationManager() {
        if (currentSimulationManager != null) {
            return currentSimulationManager;
        }
        return sharedSimulationManager;
    }

    public void setViewTransform(double scale, double offsetX, double offsetY) {
        this.viewScale = scale;
        this.viewOffsetX = offsetX;
        this.viewOffsetY = offsetY;
    }

    public double getViewScale() {
        return viewScale;
    }

    public double getViewOffsetX() {
        return viewOffsetX;
    }

    public double getViewOffsetY() {
        return viewOffsetY;
    }

    public void setViewCenter(double centerX, double centerY) {
        this.viewCenterX = centerX;
        this.viewCenterY = centerY;
    }

    public double getViewCenterX() {
        return viewCenterX;
    }

    public double getViewCenterY() {
        return viewCenterY;
    }

    public void setBasicViewScale(double scale) {
        this.basicViewScale = scale;
    }

    public double getBasicViewScale() {
        return basicViewScale;
    }

    public void setGraphicViewScale(double scale) {
        this.graphicViewScale = scale;
    }

    public double getGraphicViewScale() {
        return graphicViewScale;
    }

    /**
     * Hàm dọn dẹp: CHỈ GỌI hàm này khi thoát hẳn game quay về Menu chính!
     */
    private void stopCurrentSimulationIfRunning() {
        if (currentSimulationManager != null) {
            try {
                currentSimulationManager.stopSimulation();
                System.out.println("🧹 [MapViewer] Đã dừng luồng mô phỏng động an toàn.");
            } catch (Exception e) {
                System.err.println("Không thể dừng luồng mô phỏng động: " + e.getMessage());
            }
            currentSimulationManager = null;
        }

        if (sharedSimulationManager != null) {
            try {
                sharedSimulationManager.stopSimulation();
                System.out.println("🧹 [MapViewer] Đã dừng luồng mô phỏng tĩnh an toàn.");
            } catch (Exception e) {
                System.err.println("Không thể dừng luồng mô phỏng tĩnh: " + e.getMessage());
            }
            sharedSimulationManager = null;
        }
    }

    /**
     * Hàm hỗ trợ kiểm tra và kích hoạt luồng Giả lập nếu nó chưa chạy (Trái tim của hệ thống)
     */
    private void ensureSimulationIsRunning() {
        // Nếu chưa có luồng nào chạy, lập tức khởi tạo luồng duy nhất cho toàn bộ vòng đời mô phỏng
        if (this.currentSimulationManager == null && sharedSimulationManager == null) {
            System.out.println("⏳ [MapViewer] Khởi tạo luồng Backend Core mới chạy xuyên suốt...");
            brain.controller.SimulationManager manager = new brain.controller.SimulationManager(this.worldMap, WorldMap.SIZE);
            manager.start(); // Bật luồng chạy vị trí con vật nhảy nhót

            this.currentSimulationManager = manager;
            sharedSimulationManager = manager;
        } else {
            System.out.println("🔄 [MapViewer] Luồng mô phỏng cũ đang sống tốt, tiếp tục duy trì di chuyển!");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreen(false);
        setStageFullScreen(primaryStage);

        // Khởi tạo hoặc nhận diện bản đồ dữ liệu ngầm dùng chung
        if (sharedWorldMap != null) {
            this.worldMap = sharedWorldMap;
        } else {
            worldMap = new WorldMap(94033111, WorldMap.SIZE);
        }

        showMainMenu();
        primaryStage.show();
    }

    public void showMainMenu() {
        // Khi quay về Menu chính -> Tắt hẳn luồng giả lập cũ để giải phóng RAM/CPU hoàn toàn
        stopCurrentSimulationIfRunning();

        primaryStage.setTitle("Wild Ecosystems App");
        MainMenuView menuView = new MainMenuView(this);
        primaryStage.setScene(menuView.getScene());
        setStageFullScreen(primaryStage);
    }

    private void setStageFullScreen(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setFullScreen(false);
        stage.setMaximized(true);
    }

    public void showBasicMode() {
        // TUYỆT ĐỐI KHÔNG dừng luồng ở đây! Ngược lại, phải đảm bảo luồng đang chạy mượt mà
        ensureSimulationIsRunning();

        primaryStage.setTitle("Ecosystem Monitor - Full Random Biome Mode");
        BasicModeView basicModeView = new BasicModeView(this);
        primaryStage.setScene(basicModeView.getScene());
        setStageFullScreen(primaryStage);

        System.out.println("✅ Chuyển sang Basic Mode thành công! Luồng con vật vẫn chạy phăng phăng.");
    }

    public void showGraphicMode() {
        // TUYỆT ĐỐI KHÔNG dừng luồng ở đây! Bảo vệ luồng di chuyển của con vật sống sót
        ensureSimulationIsRunning();

        try {
            System.out.println("Switching to Graphic Mode... Loading FXML layout.");

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/MainLayout.fxml")
            );

            javafx.scene.layout.BorderPane root = loader.load();
            view.graphic.ui.controller.MainController mainController = loader.getController();

            // Truyền map vào cho Canvas vẽ, đồng thời đồng bộ hóa luồng cũ sang MainController (đã fix ở bước trước)
            brain.controller.SimulationManager manager = mainController.setWorldMapContext(this.worldMap, WorldMap.SIZE);

            // Cập nhật lại tham chiếu đồng bộ
            this.currentSimulationManager = manager;
            sharedSimulationManager = manager;

            Scene graphicScene = new Scene(root);
            primaryStage.setTitle("🌿 Wild Ecosystem - Graphic Simulation Mode");
            primaryStage.setScene(graphicScene);
            setStageFullScreen(primaryStage);

            System.out.println("Graphic Mode loaded successfully!");

        } catch (Exception e) {
            System.err.println("LỖI NGHIÊM TRỌNG: Không thể nạp giao diện đồ họa Graphic Mode!");
            e.printStackTrace();
        }
    }
}