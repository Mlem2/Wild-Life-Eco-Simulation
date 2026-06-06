package view;

import core.enviroment.WorldMap;
import javafx.application.Application;
import javafx.stage.Stage;

public class MapViewer extends Application {
    private Stage primaryStage;
    private WorldMap worldMap;

    private static WorldMap sharedWorldMap;
    private static brain.controller.SimulationManager sharedSimulationManager;

    public static void setSharedWorldMap(WorldMap worldMap) {
        sharedWorldMap = worldMap;
    }

    public static void setSharedSimulationManager(brain.controller.SimulationManager simulationManager) {
        sharedSimulationManager = simulationManager;
    }

    public WorldMap getWorldMap() {
        return this.worldMap;
    }

    public brain.controller.SimulationManager getSharedSimulationManager() {
        return sharedSimulationManager;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Khởi tạo bản đồ dữ liệu ngầm dùng chung không đổi qua các màn hình
        if (sharedWorldMap != null) {
            this.worldMap = sharedWorldMap;
        } else {
            worldMap = new WorldMap(94033111, WorldMap.SIZE);
        }

        // Vừa bật ứng dụng lên, hiển thị ngay màn hình Menu chính
        showMainMenu();
        primaryStage.show();
    }

    public void showMainMenu() {
        primaryStage.setTitle("Wild Ecosystems App");
        MainMenuView menuView = new MainMenuView(this);
        primaryStage.setScene(menuView.getScene());
    }

    public void showBasicMode() {
        primaryStage.setTitle("Ecosystem Monitor - Full Random Biome Mode");
        BasicModeView basicModeView = new BasicModeView(this);
        primaryStage.setScene(basicModeView.getScene());
    }

    public void showGraphicMode() {
        System.out.println("Switching to Graphic Mode... (Ready for Stardew Valley view integration)");
    }
}