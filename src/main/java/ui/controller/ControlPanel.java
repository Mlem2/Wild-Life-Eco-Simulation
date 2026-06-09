package ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;

public class ControlPanel {
	
	@FXML
	private Button btnPause;
	
    @FXML
    private Label labelSpeed;
    
    @FXML
    private Slider sliderSpeed;
    
    @FXML
    private ToggleGroup toolGroup;

    @FXML
    void onPauseClicked()
    {
    	System.out.println("Bấm tạm dừng");
    }
    
    @FXML
    void onResetZoomClicked()
    {
    	System.out.println("Reset Zoom");
    }
    
    @FXML
    void onToolNone()
    {
    	
    }
    
    @FXML
    void onToolGrass()
    {
    	
    }
    
    @FXML void onToolRabbit()
    {
    	
    }
    
    @FXML void onToolDeer()
    {
    	
    }
    
    @FXML void onToolWolf()
    {
    	
    }
    
    @FXML void onToolTiger()
    {
    	
    }
    
    @FXML void onToolElephant()
    {
    	
    }
    
    @FXML void onToolRock()
    {
    	
    }

}
