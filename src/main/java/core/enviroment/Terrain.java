package core.enviroment;

import java.awt.Color;

public enum Terrain {
    FOREST("Forest", 0.7f, true, true, false, new Color(0, 100, 0)),
    GRASSLAND("Grassland", 1.0f, true, true, false, new Color(50, 200, 50)),
    MOUNTAIN("Rock", 0.0f, false, false, false, new Color(100, 100, 100)),
    MUD("Mud", 0.5f, true, false, false, new Color(112, 59, 59)),
    WATER("Water", 0.2f, true, false, true, new Color(0, 100, 255));

    private final String name;
    private final float speedMultiplier;
    private final boolean isPassable;
    private final boolean isGrass;
    private final boolean isWater;
    private final Color terrainColor;

    Terrain(String name, float speedMultiplier, boolean isPassable, boolean isGrass, boolean isWater, Color terrainColor) {
        this.name = name;
        this.speedMultiplier = speedMultiplier;
        this.isPassable = isPassable;
        this.isGrass = isGrass;
        this.isWater = isWater;
        this.terrainColor = terrainColor;
    }

    public String getName() {
        return name;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public boolean isWater() {
        return isWater;
    }

    public boolean isGrass() {
        return isGrass;
    }

    public boolean isPassable() {
        return isPassable;
    }

    public Color getTerrainColor() {
        return terrainColor;
    }
}
