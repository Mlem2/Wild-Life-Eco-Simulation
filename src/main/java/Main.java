import brain.controller.SimulationManager;
import core.enviroment.WorldMap;
import entities.base.EntityMap;
import javafx.application.Application;
import view.MapViewer;

public class Main {
    private static final int SEED = 94033112;
    private static final int SIZE = 500;

    public static void main(String[] args) {
        entities.base.Position.initializeGrid(SIZE);
        WorldMap worldMap = new WorldMap(SEED, SIZE);
        EntityMap entityMap = new EntityMap(worldMap, SIZE);
        worldMap.initializeChunks(entityMap);

        SimulationManager simulationManager = new SimulationManager(worldMap, SIZE);
        simulationManager.start();

        MapViewer.setSharedWorldMap(worldMap);
        MapViewer.setSharedSimulationManager(simulationManager);

        Application.launch(MapViewer.class, args);
    }
}
