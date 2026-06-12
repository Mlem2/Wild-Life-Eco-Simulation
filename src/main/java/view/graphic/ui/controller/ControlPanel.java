package view.graphic.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent;

public class ControlPanel {

    @FXML
    private ChoiceBox<String> choiceSpeed;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnSeasonJump;
    @FXML
    private Label lblClock;
    @FXML
    private Label lblSeason;
    @FXML
    private Label lblPartOfDay;
    @FXML
    private ToggleGroup toolGroup;

    private MapController mapController;

    @FXML
    public void initialize() {
        if (choiceSpeed != null) {
            choiceSpeed.getItems().addAll("1x", "1.25x", "1.5x", "2x", "3x", "4x");
            choiceSpeed.setValue("1x");
        }
    }

    public void setMapController(MapController mapController) {
        this.mapController = mapController;
    }

    public void setSelectedInfo(String animalInfo, String chunkInfo) {
        // Selection info is now displayed only on the left-side inspector panel.
        // This method is kept as a no-op to avoid duplicate right-side information.
    }

    public void refreshTimeStatus() {
        if (lblClock == null || lblSeason == null || lblPartOfDay == null) return;
        lblClock.setText(String.format("Time: %02d:%02d", core.TimeSystem.hour, core.TimeSystem.minute));
        lblSeason.setText("Season: " + core.TimeSystem.season);
        lblPartOfDay.setText("Cycle: " + core.TimeSystem.partOfDay);
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
    void onSeasonJumpClicked(ActionEvent event) {
        try {
            core.TimeSystem.jumpToLastDayOfPreviousSeasonAt2330();
            refreshTimeStatus();
            System.out.println("🕒 Season jump executed: " + core.TimeSystem.season + " " + core.TimeSystem.day + "/" + core.TimeSystem.month + " " + core.TimeSystem.hour + ":" + core.TimeSystem.minute);
        } catch (Exception e) {
            System.err.println("Lỗi khi chuyển mùa: " + e.getMessage());
        }
    }

    // --- ĐỒNG BỘ CHUỖI CÔNG CỤ CHUẨN KHÍT VỚI MAPCONTROLLER ---
    @FXML void onToolNone(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "NONE"; }
    @FXML void onToolGrass(ActionEvent event)    { view.graphic.ui.controller.MapController.SELECTED_TOOL = "BUSH"; }     // Gieo bụi cỏ
    @FXML void onToolRabbit(ActionEvent event)   { view.graphic.ui.controller.MapController.SELECTED_TOOL = "RABBIT"; }   // Thêm thỏ
    @FXML void onToolWolf(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "WOLF"; }     // Thêm sói
    @FXML void onToolTiger(ActionEvent event)    { view.graphic.ui.controller.MapController.SELECTED_TOOL = "TIGER"; }    // Thêm hổ
    @FXML void onToolElephant(ActionEvent event) { view.graphic.ui.controller.MapController.SELECTED_TOOL = "ELEPHANT"; } // Thêm voi
    @FXML void onToolRock(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "ROCK"; }     // Đặt đá
    @FXML void onToolTree(ActionEvent event)     { view.graphic.ui.controller.MapController.SELECTED_TOOL = "TREE"; }     // Đặt cây to
}