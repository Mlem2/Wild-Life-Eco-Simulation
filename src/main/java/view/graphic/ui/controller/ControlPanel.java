package view.graphic.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent;

public class ControlPanel {

    @FXML
    private javafx.scene.control.ChoiceBox<String> choiceSpeed;
    @FXML
    private Button btnPause;
    @FXML
    private Label labelSpeed;
    @FXML
    private ToggleGroup toolGroup;

    @FXML
    public void initialize() {
        if (choiceSpeed != null) {
            choiceSpeed.getItems().addAll("1x", "1.25x", "1.5x", "2x", "3x", "4x");
            choiceSpeed.setValue("1x");
        }
    }

    @FXML
    void onSpeedChanged(ActionEvent event) {
        if (choiceSpeed == null || view.MapViewer.instance == null) return;
        String selectedSpeed = choiceSpeed.getValue();
        if (selectedSpeed == null) return;
        try {
            String numericPart = selectedSpeed.replace("x", "");
            double multiplier = Double.parseDouble(numericPart);
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
        if (view.MapViewer.instance != null) {
            brain.controller.SimulationManager manager = view.MapViewer.instance.getSharedSimulationManager();
            if (manager != null) {
                if (manager.isSimulationRunning()) {
                    manager.pauseSimulation();
                    btnPause.setText("▶ Resume");
                    btnPause.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                } else {
                    manager.resumeSimulation();
                    btnPause.setText("⏸ Pause");
                    btnPause.setStyle("");
                }
            }
        }
    }

    @FXML
    void onResetZoomClicked(ActionEvent event) {
        System.out.println("Reset Zoom");
    }

    // --- ĐỒNG BỘ CHUỖI CÔNG CỤ CHUẨN KHÍT VỚI MAPCONTROLLER ---
    @FXML void onToolNone(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "NONE"; }
    @FXML void onToolBush(ActionEvent event)    { view.graphic.ui.controller.MapController.SELECTED_TOOL = "BUSH"; }     // Gieo bụi cỏ
    @FXML void onToolRabbit(ActionEvent event)   { view.graphic.ui.controller.MapController.SELECTED_TOOL = "RABBIT"; }   // Thêm thỏ
    @FXML void onToolWolf(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "WOLF"; }     // Thêm sói
    @FXML void onToolTiger(ActionEvent event)    { view.graphic.ui.controller.MapController.SELECTED_TOOL = "TIGER"; }    // Thêm hổ
    @FXML void onToolElephant(ActionEvent event) { view.graphic.ui.controller.MapController.SELECTED_TOOL = "ELEPHANT"; } // Thêm voi
    @FXML void onToolRock(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "ROCK"; }     // Đặt đá
    @FXML void onToolTree(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "TREE"; }     // Đặt cây to
}