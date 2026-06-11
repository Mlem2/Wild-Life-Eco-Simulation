package brain.controller;

import brain.pathfinder.Pathfinder;
import core.TimeSystem;
import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Bush;
import entities.Trees;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.Plant;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationManager {
    private final WorldMap worldMap;
    private final int gridSize;
    private final Map<Animals, AnimalBrainUpdate> brainMap = new HashMap<>();
    private final Pathfinder pathfinder;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean running = false;

    public SimulationManager(WorldMap worldMap, int gridSize) {
        this.worldMap = worldMap;
        this.gridSize = gridSize;
        this.pathfinder = new Pathfinder(worldMap);
    }

    public void start() {
        if (running) return;
        running = true;
        
        // Initialize brains for existing entities
        registerAllBrains();
        
        // 25 ticks per second = 1000ms / 25 = 40ms per tick
        scheduler.scheduleAtFixedRate(this::tick, 0, 40, TimeUnit.MILLISECONDS);
    }

    private int tickCount = 0;

    private void tick() {
        if (!running) return;
        
        updateSimulationLogic();
        
        tickCount++;
        // Update time system once per second (every 25 ticks) or at some other frequency
        // For now, let's keep it consistent with the user's request
        if (tickCount % 25 == 0) {
            updateTimeSystem();
        }
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
    }

    private void updateTimeSystem() {
        try {
            TimeSystem.updateHours();
        } catch (Exception ignored) {}
    }

    private void updateSimulationLogic() {
        try {
            Chunk[][] chunkMap = worldMap.chunkMap;
            if (chunkMap == null) return;

            Entity[][] animalCoordinates = new Entity[gridSize][gridSize];
            List<Entity> allEntities = new ArrayList<>();

            for (int cy = 0; cy < chunkMap.length; cy++) {
                for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                    Chunk chunk = chunkMap[cy][cx];
                    if (chunk == null) continue;
                    synchronized (chunk.getEntityList()) {
                        for (Entity entity : chunk.getEntityList()) {
                            if (entity != null && entity.checkAlive()) {
                                allEntities.add(entity);
                                if (entity instanceof Animals) {
                                    animalCoordinates[entity.getX()][entity.getY()] = entity;
                                }
                            }
                        }
                    }
                }
            }

            for (int cy = 0; cy < chunkMap.length; cy++) {
                for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                    Chunk chunk = chunkMap[cy][cx];
                    if (chunk == null) continue;

                    List<Entity> entityList = chunk.getEntityList();
                    Set<Entity> entitiesToRemove = new HashSet<>();

                    synchronized (entityList) {
                        for (int i = entityList.size() - 1; i >= 0; i--) {
                            Entity entity = entityList.get(i);
                            if (entity == null || !entity.checkAlive() || entitiesToRemove.contains(entity)) continue;

                            if (entity instanceof Plant plant) {
                                plant.checkCD(animalCoordinates, allEntities);
                            }

                            if (entity instanceof Animals animal) {
                                animal.updateMoveCooldown();
                                if (tickCount % 25 == 0) {
                                    try {
                                        Field fieldAge = Entity.class.getDeclaredField("age");
                                        fieldAge.setAccessible(true);
                                        int age = (int) fieldAge.get(animal);
                                        if (age > 0) fieldAge.set(animal, age - 1);
                                    } catch (Exception ignored) {}
                                }

                                try {
                                    Field fieldCooldown = Animals.class.getDeclaredField("currentMoveCooldown");
                                    fieldCooldown.setAccessible(true);
                                    int cooldown = (int) fieldCooldown.get(animal);

                                    if (cooldown <= 0 && animal.checkAlive()) {
                                        AnimalBrainUpdate brain = brainMap.get(animal);
                                        if (brain == null) {
                                            registerBrainForEntity(animal);
                                            brain = brainMap.get(animal);
                                        }

                                        if (brain != null) {
                                            brain.update();
                                        } else {
                                            Field fieldStrategy = Animals.class.getDeclaredField("moveStrategy");
                                            fieldStrategy.setAccessible(true);
                                            brain.strategy.MoveStrategy strategy = (brain.strategy.MoveStrategy) fieldStrategy.get(animal);

                                            if (strategy != null) {
                                                allEnum.Direction dir = strategy.move(animal, allEntities);
                                                if (dir != null && dir != allEnum.Direction.CENTER) {
                                                    Field fieldX = Entity.class.getDeclaredField("x");
                                                    Field fieldY = Entity.class.getDeclaredField("y");
                                                    fieldX.setAccessible(true);
                                                    fieldY.setAccessible(true);

                                                    int curX = (int) fieldX.get(animal);
                                                    int curY = (int) fieldY.get(animal);
                                                    int nextX = curX, nextY = curY;

                                                    switch (dir) {
                                                        case NORTH:     nextY--; break;
                                                        case SOUTH:     nextY++; break;
                                                        case EAST:      nextX++; break;
                                                        case WEST:      nextX--; break;
                                                        case NORTHEAST: nextX++; nextY--; break;
                                                        case NORTHWEST: nextX--; nextY--; break;
                                                        case SOUTHEAST: nextX++; nextY++; break;
                                                        case SOUTHWEST: nextX--; nextY++; break;
                                                        default: break;
                                                    }

                                                    nextX = Math.max(0, Math.min(gridSize - 1, nextX));
                                                    nextY = Math.max(0, Math.min(gridSize - 1, nextY));

                                                    fieldX.set(animal, nextX);
                                                    fieldY.set(animal, nextY);
                                                }
                                            }
                                            Field fieldDefault = Animals.class.getDeclaredField("defaultMoveCooldown");
                                            fieldDefault.setAccessible(true);
                                            int defaultCooldown = (int) fieldDefault.get(animal);
                                            fieldCooldown.set(animal, defaultCooldown > 0 ? defaultCooldown : 3);
                                        }
                                    }
                                } catch (Exception ignored) {}

                                // Eating logic (Moved mostly to Brain, keeping minimal here for non-brain entities if any)
                                for (int j = entityList.size() - 1; j >= 0; j--) {
                                    Entity target = entityList.get(j);
                                    if (target != null && target != animal && target.getX() == animal.getX() && target.getY() == animal.getY()) {
                                        boolean canEat = false;
                                        if (animal instanceof entities.Elephant) {
                                            if (target instanceof Bush || target instanceof Trees) {
                                                canEat = true;
                                            }
                                        } else if (animal instanceof entities.attributes.Herbivore) {
                                            // Non-elephant herbivores no longer eat trees and bushes
                                            if (target instanceof Plant && !(target instanceof Bush || target instanceof Trees)) {
                                                canEat = true;
                                            }
                                        }

                                        if (canEat) {
                                            try {
                                                Field fieldHunger = Animals.class.getDeclaredField("hunger");
                                                Field fieldThirst = Animals.class.getDeclaredField("thirst");
                                                fieldHunger.setAccessible(true);
                                                fieldThirst.setAccessible(true);

                                                fieldHunger.set(animal, Math.min(100.0, (double) fieldHunger.get(animal) + 40.0));
                                                fieldThirst.set(animal, Math.min(100.0, (double) fieldThirst.get(animal) + 20.0));

                                                Field fieldAlive = Entity.class.getDeclaredField("isAlive");
                                                fieldAlive.setAccessible(true);
                                                fieldAlive.set(target, false);
                                                entitiesToRemove.add(target);
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }

                                // Herbivore grass eating logic (only from terrain, not from entities like Bush/Trees), also need to in Scared Strategy.
                                // Elephants doesn't tend to eat grass, they eat Bush/Trees instead, so they are not affected by this logic.
                                if ((animal instanceof entities.attributes.Herbivore && !(animal instanceof entities.attributes.Apex)) && (animal.getHungerPercentage() < 80.0) && (animal.getCurrentMoveCooldown() <= 1) && (animal.isSpeedUp() == false)) {
                                    try {
                                        var currentTile = worldMap.getTile(animal.getX(), animal.getY());
                                        if (currentTile != null && currentTile.isGrass()) {
                                            Field fieldHunger = Animals.class.getDeclaredField("hunger");
                                            Field fieldThirst = Animals.class.getDeclaredField("thirst");
                                            fieldHunger.setAccessible(true);
                                            fieldThirst.setAccessible(true);

                                            fieldHunger.set(animal, Math.min(100.0, (double) fieldHunger.get(animal) + 30.0));
                                            fieldThirst.set(animal, Math.min(100.0, (double) fieldThirst.get(animal) + 10.0));

                                            ActionManager.setCooldown(animal, 20);
                                        }
                                    } catch (Exception ignored) {}
                                }


                                // Thirst logic, also nearby-water count as well
                                try {
                                    boolean nearWater = false;
                                    for (int dx = -1; dx <= 1; dx++) {
                                        for (int dy = -1; dy <= 1; dy++) {
                                            int checkX = animal.getX() + dx;
                                            int checkY = animal.getY() + dy;
                                            if (checkX >= 0 && checkX < gridSize && checkY >= 0 && checkY < gridSize) {
                                                try {
                                                    String nearbyTileName = worldMap.getTile(checkX, checkY).getName().toLowerCase();
                                                    if (nearbyTileName.contains("water") || nearbyTileName.contains("nuoc")) {
                                                        nearWater = true;
                                                        break;
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        }}
                                        if (nearWater && animal.getThirstPercentage() < 90 && animal.getCurrentMoveCooldown() <= 1) {
                                        Field fieldThirst = Animals.class.getDeclaredField("thirst");
                                        fieldThirst.setAccessible(true);
                                        fieldThirst.set(animal, 100.0);
                                        if (animal instanceof entities.attributes.Aquatic) {
                                            Field fieldHunger = Animals.class.getDeclaredField("hunger");
                                            fieldHunger.setAccessible(true);
                                            fieldHunger.set(animal, 100.0);
                                        }   
                                        ActionManager.setCooldown(animal, 10);
                                    }
                            } catch (Exception ignored) {}

                                // Chunk management
                                int newChunkX = entity.getX() / WorldMap.CHUNK_SIZE;
                                int newChunkY = entity.getY() / WorldMap.CHUNK_SIZE;
                                if (newChunkX != cx || newChunkY != cy) {
                                    if (newChunkX >= 0 && newChunkX < chunkMap[0].length && newChunkY >= 0 && newChunkY < chunkMap.length) {
                                        Chunk newChunk = chunkMap[newChunkY][newChunkX];
                                        if (newChunk != null) {
                                            newChunk.addEntity(entity);
                                            entitiesToRemove.add(entity);
                                        }
                                    }
                                }
                            }
                        }
                        synchronized (entityList) {
                            entityList.removeAll(entitiesToRemove);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerAllBrains() {
        try {
            Chunk[][] chunkMap = worldMap.chunkMap;
            if (chunkMap == null) return;
            for (Chunk[] row : chunkMap) {
                for (Chunk chunk : row) {
                    if (chunk == null) continue;
                    for (Entity e : chunk.getEntityList()) {
                        registerBrainForEntity(e);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public void registerBrainForEntity(Entity e) {
        if (e == null || !(e instanceof Animals a)) return;
        if (brainMap.containsKey(a)) return;

        MapSystem ms = new MapSystem(worldMap);
        ChooseTarget ct = new ChooseTarget(a, ms);
        ActionManager am = new ActionManager(a, ms);
        AnimalBrainUpdate abu = new AnimalBrainUpdate(a, ct, pathfinder, am);
        brainMap.put(a, abu);
    }

    public Map<Animals, AnimalBrainUpdate> getBrainMap() {
        return brainMap;
    }
}
