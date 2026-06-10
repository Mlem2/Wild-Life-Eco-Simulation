package view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenuView {
    private final MapViewer manager;
    private final Scene scene;

    public MainMenuView(MapViewer manager) {
        this.manager = manager;

        // --- 1. TIÊU ĐỀ KHU VỰC MENU CHÍNH ---
        Label lblTitle = new Label("WILD ECOSYSTEMS APP");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.BLACK);

        Label lblSubTitle = new Label("Select an environment mode to begin simulation");
        lblSubTitle.setFont(Font.font("Segoe UI", 14));
        lblSubTitle.setTextFill(Color.RED);

        // --- 2. CÁC NÚT BẤM CHUYỂN CHẾ ĐỘ & THOÁT ---
        Button btnBasicMode = new Button("Launch Basic Mode");
        btnBasicMode.setPrefWidth(240);
        btnBasicMode.setPrefHeight(42);
        btnBasicMode.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnBasicMode.setOnAction(e -> manager.showBasicMode());

        Button btnGraphicMode = new Button("Launch Graphic Mode");
        btnGraphicMode.setPrefWidth(240);
        btnGraphicMode.setPrefHeight(42);
        btnGraphicMode.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnGraphicMode.setOnAction(e -> manager.showGraphicMode());

        Button btnExit = new Button("❌ Exit Program");
        btnExit.setPrefWidth(240);
        btnExit.setPrefHeight(42);
        btnExit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff4d4d; -fx-cursor: hand;");
        btnExit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        // --- 3. THIẾT LẬP LAYOUT VÀ HÌNH NỀN TỪ THƯ MỤC RESOURCES/IMAGE ---
        VBox layout = new VBox(22);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        // Nạp file ảnh từ resources/image/menu_bg.png
        // (Lưu ý: Nếu ảnh của cậu là đuôi .jpg, hãy sửa "menu_bg.png" thành "menu_bg.jpg")
        java.net.URL imgUrl = getClass().getResource("/image/forest-background.jpg");

        if (imgUrl != null) {
            String externalForm = imgUrl.toExternalForm();
            layout.setStyle(
                    "-fx-background-image: url('" + externalForm + "'); " +
                            "-fx-background-repeat: no-repeat; " +
                            "-fx-background-size: cover; " +
                            "-fx-background-position: center;"
            );
        } else {
            // Phương án dự phòng nếu ghi sai tên file hoặc sai đường dẫn hệ thống
            System.err.println("Không tìm thấy file ảnh nền trong thư mục resources/image/ !");
            layout.setStyle("-fx-background-color: #1e1e1e;");
        }

        layout.getChildren().addAll(lblTitle, lblSubTitle, btnBasicMode, btnGraphicMode, btnExit);

        this.scene = new Scene(layout, 1180, 820);
    }

    public Scene getScene() {
        return this.scene;
    }
}