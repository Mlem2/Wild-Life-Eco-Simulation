package view.graphic.ui.controller;

import javafx.fxml.FXML;
import brain.controller.SimulationManager;
import core.enviroment.WorldMap;
import core.enviroment.Terrain;
import core.enviroment.Chunk;
import entities.base.Entity;
import java.util.Random;

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

        // Đọc luồng đang chạy duy nhất từ MapViewer sang để không làm đơ core
        SimulationManager simManager = view.MapViewer.instance.getSharedSimulationManager();

        // 🚀 KÍCH HOẠT SINH ĐỘNG VẬT ĐÔNG ĐÚC - PHÂN CHIA SINH THÁI CHUẨN ĐƯỜNG DẪN PACKAGE
        if (realWorldMap != null) {
            try {
                Random rand = new Random();
                int spawned = 0;
                int totalAnimals = 200; // Số lượng sinh vật cậu muốn thả vào game
                int attempts = 0;

                System.out.println("⏳ [MainController] Đang tiến hành thả " + totalAnimals + " sinh vật vào đúng môi trường...");

                while (spawned < totalAnimals && attempts < totalAnimals * 15) {
                    attempts++;
                    int rx = rand.nextInt(gridSize);
                    int ry = rand.nextInt(gridSize);

                    Terrain tile = realWorldMap.getTile(rx, ry);
                    int dice = rand.nextInt(100);

                    Entity newEntity = null;

                    // 🌊 NẾU LÀ Ô NƯỚC: Chỉ tạo Cá (Fish) đưa xuống nước
                    if (tile == Terrain.WATER) {
                        newEntity = new entities.Fish(rx, ry); // Định nghĩa chính xác package entities.Fish
                    }
                    // 🌾 NẾU LÀ Ô CỎ: Sinh ngẫu nhiên các loài thú chạy trên cạn
                    else if (tile == Terrain.GRASSLAND) {
                        if (dice < 35) {
                            newEntity = new entities.Rabbit(rx, ry);
                        } else if (dice < 60) {
                            newEntity = new entities.Wolf(rx, ry);
                        } else if (dice < 80) {
                            newEntity = new entities.Tiger(rx, ry);
                        } else {
                            newEntity = new entities.Elephant(rx, ry);
                        }
                    }

                    // Nếu tạo thực thể thành công, nạp thẳng nó vào lưới Chunk quản lý đồ họa
                    if (newEntity != null) {
                        int yChunk = ry / WorldMap.CHUNK_SIZE;
                        int xChunk = rx / WorldMap.CHUNK_SIZE;

                        if (yChunk >= 0 && yChunk < realWorldMap.chunkMap.length &&
                                xChunk >= 0 && xChunk < realWorldMap.chunkMap[0].length) {

                            Chunk targetChunk = realWorldMap.chunkMap[yChunk][xChunk];
                            if (targetChunk != null) {
                                targetChunk.addEntity(newEntity);
                                spawned++;
                            }
                        }
                    }
                }
                System.out.println("🐾 [MainController] Hệ sinh thái đồng bộ hoàn tất! Đã kích hoạt " + spawned + " sinh vật.");

            } catch (Exception e) {
                System.err.println("⚠️ Cảnh báo lỗi khởi tạo sinh thái: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Kích hoạt đẩy dữ liệu bản đồ và cấu hình Zoom mượt mà sang cho Canvas vẽ
        if (mapController != null) {
            mapController.setWorldContext(realWorldMap, gridSize);
        } else {
            System.err.println("CẢNH BÁO: mapController chưa được inject từ FXML!");
        }

        return simManager;
    }
}