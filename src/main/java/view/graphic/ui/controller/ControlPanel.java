package view.graphic.ui.controller; // 1. Đã cập nhật đúng package mới của cậu

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent; // 2. Thêm import ActionEvent để đồng bộ với sự kiện từ file FXML

public class ControlPanel {

    @FXML
    private Button btnPause; //

    @FXML
    private Label labelSpeed; //

    @FXML
    private Slider sliderSpeed; //

    @FXML
    private ToggleGroup toolGroup; //

    // 3. Sửa đổi: Đã thêm tham số (ActionEvent event) vào tất cả các hàm xử lý nút bấm để tránh lỗi đứt gãy liên kết UI
    @FXML
    
    void onPauseClicked(ActionEvent event)
    {
        System.out.println("Bấm tạm dừng"); //
    }

    @FXML
    void onResetZoomClicked(ActionEvent event)
    {
        System.out.println("Reset Zoom"); //
    }

    public String activeTool = "NONE";
    
    public String getActiveTool() {
        return this.activeTool;
    }
    
    @FXML
    void onToolNone(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Không chọn");
    }

    @FXML
    void onToolGrass(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Gieo cỏ");
    }

    @FXML
    void onToolRabbit(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Thêm Thỏ");
        activeTool = "ANIMAL_RABBIT";
    }

    @FXML
    void onToolDeer(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Thêm Hươu");
    }

    @FXML
    void onToolWolf(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Thêm Sói");
    }

    @FXML
    void onToolTiger(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Thêm Hổ");
    }

    @FXML
    void onToolElephant(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Thêm Voi");
    }

    @FXML
    void onToolRock(ActionEvent event)
    {
        System.out.println("Chọn công cụ: Đặt Đá");
    }
}