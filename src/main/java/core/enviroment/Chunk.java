package core.enviroment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entities.base.Entity;
import entities.base.Position;

public class Chunk {
    // Use a synchronized list so callers can safely `synchronized(chunk.getEntityList())`
    private final List<Entity> entityList;
    private final List<Position> waterPositions;
    private int distanceToWater;

    public Chunk(int distanceToWater) {
        this.entityList = Collections.synchronizedList(new ArrayList<>());
        this.waterPositions = new ArrayList<>();
        this.distanceToWater = distanceToWater;
    }

    public void addWaterPosition(Position pos) {
        waterPositions.add(pos);
    }

    public List<Position> getWaterPositions() {
        return waterPositions;
    }

    public void setDistanceToWater(int distanceToWater) {
        this.distanceToWater = distanceToWater;
    }

    public int getDistanceToWater() {
        return distanceToWater;
    }

    /**
     * Return the internal list. This list is synchronized and callers may
     * include synchronized blocks on it as the codebase already does.
     */
    public List<Entity> getEntityList() {
        return entityList;
    }

    public void addEntity(Entity entity) {
        if (entity == null) return;
        if (!entityList.contains(entity)) entityList.add(entity);
    }

    public void removeEntity(Entity entity) {
        if (entity == null) return;
        entityList.remove(entity);
    }

    public boolean contains(Entity entity) { return entityList.contains(entity); }
}
