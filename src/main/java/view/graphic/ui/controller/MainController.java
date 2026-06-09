package view.graphic.ui.controller;

import javafx.fxml.FXML;
import brain.controller.SimulationManager;
import core.enviroment.WorldMap;

public class MainController {

    @FXML
    private MapController mapController; // Đại diện cho fx:id="map" trong MainLayout.fxml

    @FXML
    private ControlPanel controlPanelController; // Biến nhúng bảng điều khiển bên phải

    @FXML
    public void initialize() {
        System.out.println("MainLayout.fxml loaded. Waiting for MapViewer to pass world context...");
    }

    /**
     * Hàm xử lý khi bấm nút quay về Menu chính
     */
    @FXML
    void onBackToMenuClicked(javafx.event.ActionEvent event) {
        System.out.println("Back to Menu button clicked. Cleaning up...");

        if (mapController != null) {
            try {
                mapController.shutdownTimeline(); // Tắt bộ vẽ Canvas ngầm an toàn để nhả luồng
            } catch (Exception e) {
                System.err.println("Không thể tắt AnimationTimer đồ họa: " + e.getMessage());
            }
        }

        // Quay xe về Menu chính thông qua instance của MapViewer
        if (view.MapViewer.instance != null) {
            view.MapViewer.instance.showMainMenu();
        } else {
            System.err.println("❌ LỖI: MapViewer.instance đang bị null, không thể quay về Menu!");
        }
    }

    /**
     * Hàm nhận dữ liệu bản đồ dùng chung từ MapViewer truyền sang để chạy mô phỏng đồ họa
     */
    public brain.controller.SimulationManager setWorldMapContext(WorldMap realWorldMap, int gridSize) {
        System.out.println("World context received in MainController. Syncing graphics...");

        // SỬA ĐỔI HOÀN HẢO: Đọc luồng đang chạy duy nhất từ MapViewer sang, không tự ý new bừa bãi làm đơ core
        SimulationManager simManager = view.MapViewer.instance.getSharedSimulationManager();

        // Kích hoạt đẩy dữ liệu bản đồ và cấu hình Zoom mượt mà sang cho Canvas vẽ
        if (mapController != null) {
            mapController.setWorldContext(realWorldMap, gridSize);
        } else {
            System.err.println("CẢNH BÁO: mapController chưa được inject từ FXML!");
        }

        return simManager;
    }
}