package brain.controller;

import brain.pathfinder.Pathfinder;
import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Bush;
import entities.Trees;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.ResourceEntity;
import entities.base.Tree;
import entities.base.Position;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            int m = core.TimeSystem.minute + 5;
            if (m >= 60) {
                m = 0;
                core.TimeSystem.hour++;
                if (core.TimeSystem.hour >= 24) {
                    core.TimeSystem.hour = 0;
                    core.TimeSystem.day++;
                }
            }
            core.TimeSystem.minute = m;
            core.TimeSystem.partOfDay = (core.TimeSystem.hour > 4 && core.TimeSystem.hour < 18) ? "Day" : "Night";
        } catch (Exception ignored) {}
    }

    private void updateSimulationLogic() {
        try {
            Field fieldChunk = WorldMap.class.getDeclaredField("chunkMap");
            fieldChunk.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) fieldChunk.get(worldMap);
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
                    synchronized (entityList) {
                        for (int i = entityList.size() - 1; i >= 0; i--) {
                            Entity entity = entityList.get(i);
                            if (entity == null || !entity.checkAlive()) continue;

                            if (entity instanceof Tree tree) {
                                tree.checkCD(animalCoordinates, allEntities);
                            }

                            if (entity instanceof ResourceEntity resource) {
                                resource.updateResourceState();
                            }

                            if (entity instanceof Animals animal) {
                                animal.updateMoveCooldown(animalCoordinates, allEntities);
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

                                                    try {
                                                        var nextTile = worldMap.getTile(nextX, nextY);
                                                        String tName = (nextTile != null && nextTile.getName() != null) ? nextTile.getName().toLowerCase() : "";
                                                        boolean water = tName.contains("water") || tName.contains("nuoc");
                                                        boolean stone = tName.contains("stone") || tName.contains("da") || tName.contains("mountain");

                                                        if (!(animal instanceof entities.Fish)) {
                                                            if (water || stone) { nextX = curX; nextY = curY; }
                                                        } else {
                                                            if (!water) { nextX = curX; nextY = curY; }
                                                        }
                                                    } catch (Exception ignored) {}

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

                                // Herbivore logic
                                synchronized (entityList) {
                                    for (int j = entityList.size() - 1; j >= 0; j--) {
                                        Entity target = entityList.get(j);
                                        if (target != null && target != animal && target.getX() == animal.getX() && target.getY() == animal.getY()) {
                                            if (animal instanceof entities.attributes.Herbivore && (target instanceof Bush || target instanceof Trees)) {
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
                                                    entityList.remove(j);
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                    }
                                }

                                // Thirst logic
                                try {
                                    String tileName = worldMap.getTile(animal.getX(), animal.getY()).getName().toLowerCase();
                                    if (tileName.contains("water") || tileName.contains("nuoc")) {
                                        Field fieldThirst = Animals.class.getDeclaredField("thirst");
                                        fieldThirst.setAccessible(true);
                                        fieldThirst.set(animal, 100.0);
                                    }
                                } catch (Exception ignored) {}

                                // Chunk management
                                int newChunkX = entity.getX() / 50;
                                int newChunkY = entity.getY() / 50;
                                if (newChunkX != cx || newChunkY != cy) {
                                    if (newChunkX >= 0 && newChunkX < chunkMap[0].length && newChunkY >= 0 && newChunkY < chunkMap.length) {
                                        Chunk newChunk = chunkMap[newChunkY][newChunkX];
                                        if (newChunk != null) {
                                            newChunk.addEntity(entity);
                                            entityList.remove(i);
                                        }
                                    }
                                }
                            }
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
            Field field = WorldMap.class.getDeclaredField("chunkMap");
            field.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) field.get(worldMap);
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
