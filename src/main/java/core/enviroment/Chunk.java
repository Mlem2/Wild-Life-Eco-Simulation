package core.enviroment;

import entities.base.Entity;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    private List<Entity> entityList;
    private int distanceToWater;

    public Chunk(int distanceToWater) {
        entityList = new ArrayList<Entity>();
        this.distanceToWater = distanceToWater;
    }

    public void setDistanceToWater(int distanceToWater) {
        this.distanceToWater = distanceToWater;
    }

    public int getDistanceToWater() {
        return distanceToWater;
    }

    public List<Entity> getEntityList() {
        return entityList;
    }

    public void addEntity(Entity entity) {
        entityList.add(entity);
    }

    public void removeEntity(Entity entity) {
        entityList.remove(entity);
    }
}
