package ui.controller;

import javafx.fxml.FXML;
import core.enviroment.WorldMap;
import brain.controller.SimulationManager;

public class MainController {
	@FXML
	private MapController mapController; 
	
    @FXML
    private ControlPanel controlPanel; 

    @FXML
    public void initialize() {
        int gridSize = 100;
        worldMap realWorldMap = new worldMap(); 

        SimulationManager simManager = new SimulationManager(realWorldMap, gridSize);
        
        if (mapController != null) {
            mapController.setWorldContext(realWorldMap, gridSize);
        }
        simManager.start();
    }
}
