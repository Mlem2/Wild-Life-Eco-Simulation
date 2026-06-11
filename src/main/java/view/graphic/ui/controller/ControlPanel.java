package view.graphic.ui.controller; // 1. Đã cập nhật đúng package mới của cậu

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent; // 2. Thêm import ActionEvent để đồng bộ với sự kiện từ file FXML

public class ControlPanel {

    @FXML
    private javafx.scene.control.ChoiceBox<String> choiceSpeed; // Thay cho Slider sliderSpeed cũ
    @FXML
    private Button btnPause; //

    @FXML
    private Label labelSpeed; //

    @FXML
    private ToggleGroup toolGroup; //

    // 3. Sửa đổi: Đã thêm tham số (ActionEvent event) vào tất cả các hàm xử lý nút bấm để tránh lỗi đứt gãy liên kết UI

    @FXML
    public void initialize() {
        if (choiceSpeed != null) {
            // Nạp các mốc tốc độ theo đúng yêu cầu của cậu vào ô chọn
            choiceSpeed.getItems().addAll("1x", "1.25x", "1.5x", "2x", "3x", "4x");
            // Chọn sẵn mốc mặc định ban đầu là 1x
            choiceSpeed.setValue("1x");
        }
    }

    @FXML
    void onSpeedChanged(ActionEvent event) {
        if (choiceSpeed == null || view.MapViewer.instance == null) return;

        // Lấy chuỗi ký tự người dùng vừa chọn (ví dụ: "1.25x")
        String selectedSpeed = choiceSpeed.getValue();
        if (selectedSpeed == null) return;

        // Chuyển chuỗi "1.25x" thành số thực 1.25
        try {
            String numericPart = selectedSpeed.replace("x", "");
            double multiplier = Double.parseDouble(numericPart);

            // Gọi sang backend core để thay đổi tốc độ chạy ngầm
            brain.controller.SimulationManager manager = view.MapViewer.instance.getSharedSimulationManager();
            if (manager != null) {
                manager.setSpeedMultiplier(multiplier);
            }
        } catch (Exception e) {
            System.err.println("Lỗi chuyển đổi mốc tốc độ: " + e.getMessage());
        }
    }
    @FXML
    void onPauseClicked(ActionEvent event) {
        // Lấy luồng mô phỏng dùng chung toàn cục đang chạy từ MapViewer
        if (view.MapViewer.instance != null) {
            brain.controller.SimulationManager manager = view.MapViewer.instance.getSharedSimulationManager();

            if (manager != null) {
                if (manager.isSimulationRunning()) {
                    // Nếu đang chạy -> Ra lệnh tạm dừng backend
                    manager.pauseSimulation();
                    btnPause.setText("▶ Resume");
                    btnPause.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;"); // Đổi sang màu xanh lá báo hiệu có thể chạy tiếp
                } else {
                    // Nếu đang dừng -> Kích hoạt chạy tiếp
                    manager.resumeSimulation();
                    btnPause.setText("⏸ Pause");
                    btnPause.setStyle(""); // Trả về style mặc định (Màu đỏ/xanh cũ của cậu)
                }
            } else {
                System.err.println("❌ Không tìm thấy luồng SimulationManager để điều khiển tạm dừng!");
            }
        }
    }

    @FXML
    void onResetZoomClicked(ActionEvent event)
    {
        System.out.println("Reset Zoom"); //
    }

    // --- ĐỒNG BỘ TOÀN BỘ CÔNG CỤ CHỌN THỰC THỂ SANG MAPCONTROLLER ---
    @FXML
    void onToolNone(ActionEvent event) {
        view.graphic.ui.controller.MapController.SELECTED_TOOL = "NONE";
    }

    @FXML
    void onToolGrass(ActionEvent event) {
        view.graphic.ui.controller.MapController.SELECTED_TOOL = "PLANT_GRASS";
    }

    @FXML
    void onToolRabbit(ActionEvent event) {
        view.graphic.ui.controller.MapController.SELECTED_TOOL = "ANIMAL_RABBIT";
    }


    @FXML
    void onToolWolf(ActionEvent event) {
        view.graphic.ui.controller.MapController.SELECTED_TOOL = "ANIMAL_WOLF";
    }

    @FXML
    void onToolTiger(ActionEvent event) {
        view.graphic.ui.controller.MapController.SELECTED_TOOL = "ANIMAL_TIGER";
    }

    @FXML
    void onToolElephant(ActionEvent event) {
        view.graphic.ui.controller.MapController.SELECTED_TOOL = "ANIMAL_ELEPHANT";
    }

    @FXML
    void onToolRock(ActionEvent event) {
        view.graphic.ui.controller.MapController.SELECTED_TOOL = "OBSTACLE_ROCK";
    }
}