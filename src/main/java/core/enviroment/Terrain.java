package core.enviroment;

import java.awt.Color;

abstract class Terrain {
    protected String name;
    protected float speedMultiplier;
    protected final boolean isPassable;
    protected final boolean isGrass;
    protected final boolean isWater;
    protected Color terrainColor;

    public Terrain(String name, float speedMultiplier, boolean isPassable, boolean isGrass, boolean isWater, Color terrainColor) {
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
