package brain.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.enviroment.Chunk;
import core.enviroment.Terrain;
import core.enviroment.WorldMap;
import entities.Food;
import entities.Water;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.Position;

/**
 * Lightweight MapSystem facade used by brain strategies.
 *
 * NOTE: This is a minimal, local implementation to provide the methods
 * the brain package expects. It intentionally returns safe defaults
 * when a full world/registry isn't provided yet.
 */
public class MapSystem {
    private final WorldMap worldMap;
    private final Random rand = new Random();

    public MapSystem() { this.worldMap = null; }

    public MapSystem(WorldMap worldMap) { this.worldMap = worldMap; }

    public boolean hasEnemyAround(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals other && isThreateningEnemy(owner, other)) return true;
            }
        }
        return false;
    }

    public boolean hasEnemyNearby(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals other && isThreateningEnemy(owner, other)) {
                    int dist = Math.abs(owner.getX() - e.getX()) + Math.abs(owner.getY() - e.getY());
                    if (dist <= 5) return true;
                }
            }
        }
        return false;
    }

    public boolean hasFoodAround(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Food) return true;
            }
        }
        return false;
    }

    public boolean hasPreyAround(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals && e != owner && e instanceof entities.attributes.Herbivore) return true;
            }
        }
        return false;
    }

    public boolean hasWaterAround(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Water) return true;
            }
        }
        return false;
    }

    private boolean isThreateningEnemy(Animals owner, Animals other) {
        if (other == owner) return false;
        if (!(other instanceof entities.attributes.Carnivore)) return false;

        if (owner instanceof entities.attributes.Carnivore) {
            return other.getAttackDamage() > owner.getAttackDamage();
        }

        return true;
    }

    public List<Chunk> getVisibleChunks(Position pos) {
        List<Chunk> out = new ArrayList<>();
        if (worldMap == null || pos == null) return out;

        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return out;

        int cx = pos.getX() / 50;
        int cy = pos.getY() / 50;
        int radius = 1; // 3x3 area
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int nx = cx + dx, ny = cy + dy;
                if (nx >= 0 && nx < chunkMap[0].length && ny >= 0 && ny < chunkMap.length) {
                    out.add(chunkMap[ny][nx]);
                }
            }
        }
        return out;
    }

    public Chunk getChunkAt(Position pos) {
        if (worldMap == null || pos == null) return null;
        Chunk[][] chunkMap = worldMap.getChunkMap();
        int cx = pos.getX() / 50;
        int cy = pos.getY() / 50;
        if (cx >= 0 && cx < chunkMap[0].length && cy >= 0 && cy < chunkMap.length) return chunkMap[cy][cx];
        return null;
    }

    public List<Animals> getPreysInChunks(List<Chunk> chunks, Animals owner) {
        List<Animals> out = new ArrayList<>();
        if (chunks == null) return out;
        for (Chunk c : chunks) {
            if (c == null) continue;
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    if (e instanceof Animals other && e != owner && other instanceof entities.attributes.Herbivore) out.add(other);
                }
            }
        }
        return out;
    }

    public List<Animals> getEnemiesInChunks(List<Chunk> chunks, Animals owner) {
        List<Animals> out = new ArrayList<>();
        if (chunks == null) return out;
        for (Chunk c : chunks) {
            if (c == null) continue;
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    if (e instanceof Animals other && isThreateningEnemy(owner, other)) out.add(other);
                }
            }
        }
        return out;
    }

    public List<Animals> getEnemiesInChunk(Chunk chunk, Animals owner) {
        List<Animals> out = new ArrayList<>();
        if (chunk == null) return out;
        synchronized (chunk.getEntityList()) {
            for (Entity e : chunk.getEntityList()) {
                if (e instanceof Animals other && isThreateningEnemy(owner, other)) out.add(other);
            }
        }
        return out;
    }

    public Animals getClosestAnimal(Position from, List<Animals> list) {
        if (list == null || list.isEmpty()) return null;
        Animals best = null; int bestDist = Integer.MAX_VALUE;
        for (Animals a : list) {
            int d = Math.abs(a.getX() - from.getX()) + Math.abs(a.getY() - from.getY());
            if (d < bestDist) { best = a; bestDist = d; }
        }
        return best;
    }

    public List<Position> getWaterInChunks(List<Chunk> chunks) {
        List<Position> out = new ArrayList<>();
        if (chunks == null || worldMap == null) return out;

        for (Chunk c : chunks) {
            if (c == null) continue;
            // Use pre-calculated water positions
            out.addAll(c.getWaterPositions());
            
            // Also check for Water entities (e.g., spawned ones if any, though usually it's terrain)
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    if (e instanceof Water) {
                        out.add(new Position(e.getX(), e.getY()));
                    }
                }
            }
        }
        return out;
    }

    public List<Position> getFoodInChunks(List<Chunk> chunks) {
        List<Position> out = new ArrayList<>();
        if (chunks == null) return out;
        for (Chunk c : chunks) {
            if (c == null) continue;
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    if (e instanceof Food || e instanceof entities.base.ResourceEntity) {
                        out.add(new Position(e.getX(), e.getY()));
                    }
                }
            }
        }
        return out;
    }

    public Entity getEntityAt(Position pos) {
        if (pos == null) return null;
        Chunk chunk = getChunkAt(pos);
        if (chunk == null) return null;
        synchronized (chunk.getEntityList()) {
            for (Entity entity : chunk.getEntityList()) {
                if (entity == null) continue;
                if (entity.getX() == pos.getX() && entity.getY() == pos.getY()) return entity;
            }
        }
        return null;
    }

    public void removeEntity(Entity entity) {
        if (entity == null) return;
        Chunk chunk = getChunkAt(new Position(entity.getX(), entity.getY()));
        if (chunk != null) chunk.removeEntity(entity);
    }

    public Position getClosestPosition(Position from, List<Position> list) {
        if (list == null || list.isEmpty()) return null;
        Position best = null; int bestDist = Integer.MAX_VALUE;
        for (Position p : list) {
            int d = Math.abs(p.getX() - from.getX()) + Math.abs(p.getY() - from.getY());
            if (d < bestDist) { best = p; bestDist = d; }
        }
        return best;
    }

    public Position getSafeRandomChunkPosition(List<Chunk> chunks, Animals owner) {
        if (chunks == null || chunks.isEmpty()) return owner.getPosition();

        // If thirsty, try to move towards water using heat map
        if (owner.getThirstPercentage() < 60) {
            Chunk bestChunk = getBestWaterChunk(chunks);
            if (bestChunk != null && bestChunk.getDistanceToWater() < Integer.MAX_VALUE) {
                return getRandomWalkablePosInChunk(bestChunk);
            }
        }

        Chunk c = chunks.get(rand.nextInt(chunks.size()));
        if (c == null) return owner.getPosition();
        return getRandomWalkablePosInChunk(c);
    }

    public Chunk getBestWaterChunk(List<Chunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return null;
        Chunk bestChunk = null;
        int minDistance = Integer.MAX_VALUE;
        for (Chunk c : chunks) {
            if (c != null && c.getDistanceToWater() < minDistance) {
                minDistance = c.getDistanceToWater();
                bestChunk = c;
            }
        }
        return bestChunk;
    }

    public Position getRandomWalkablePosInChunk(Position pos) {
        if (worldMap == null || pos == null) return pos;
        Chunk c = getChunkAt(pos);
        if (c == null) return pos;
        Position out = getRandomWalkablePosInChunk(c);
        return out != null ? out : pos;
    }


    /**
     * Return all chunks within a Manhattan radius (in chunks) around a center position.
     */
    public List<Chunk> getNearbyChunks(Position center, int radiusChunks) {
        List<Chunk> out = new ArrayList<>();
        if (worldMap == null || center == null) return out;
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return out;
        
        int cx = center.getX() / 50;
        int cy = center.getY() / 50;
        
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dy = -radiusChunks; dy <= radiusChunks; dy++) {
                int nx = cx + dx, ny = cy + dy;
                if (nx >= 0 && nx < chunkMap[0].length && ny >= 0 && ny < chunkMap.length) {
                    out.add(chunkMap[ny][nx]);
                }
            }
        }
        return out;
    }

    public List<Entity> getEntitiesInNearbyChunks(Position center, int radiusChunks) {
        List<Entity> out = new ArrayList<>();
        List<Chunk> chunks = getNearbyChunks(center, radiusChunks);
        if (chunks == null) return out;
        for (Chunk c : chunks) {
            if (c == null) continue;
            List<Entity> list = c.getEntityList();
            if (list == null) continue;
            out.addAll(list);
        }
        return out;
    }

    public <T> List<T> getEntitiesOfTypeInNearbyChunks(Position center, int radiusChunks, Class<T> cls) {
        List<T> out = new ArrayList<>();
        for (Entity e : getEntitiesInNearbyChunks(center, radiusChunks)) {
            if (e == null) continue;
            if (cls.isInstance(e)) out.add(cls.cast(e));
        }
        return out;
    }

    public List<Entity> getEntitiesWithinRadius(Position center, int radius) {
        List<Entity> out = new ArrayList<>();
        if (center == null) return out;

        for (Chunk chunk : getVisibleChunks(center)) {
            if (chunk == null) continue;
            synchronized (chunk.getEntityList()) {
                for (Entity e : chunk.getEntityList()) {
                    if (e == null) continue;
                    if (distance(center, e) <= radius) out.add(e);
                }
            }
        }
        return out;
    }

    public List<Position> getWaterPositionsWithinRadius(Position center, int radius) {
        List<Position> out = new ArrayList<>();
        if (center == null) return out;

        for (Position pos : getWaterInChunks(getVisibleChunks(center))) {
            if (distance(center, pos) <= radius) out.add(pos);
        }
        return out;
    }

    public List<Position> getNearbyFoodPositions(Position center, int radiusChunks) {
        List<Position> out = new ArrayList<>();
        for (Entity e : getEntitiesInNearbyChunks(center, radiusChunks)) {
            if (e instanceof Food) out.add(new Position(e.getX(), e.getY()));
        }
        return out;
    }

    public List<Animals> getNearbyEnemyAnimals(Position center, int radiusChunks, Animals owner) {
        List<Animals> out = new ArrayList<>();
        for (Entity e : getEntitiesInNearbyChunks(center, radiusChunks)) {
            if (e instanceof Animals) {
                Animals a = (Animals) e;
                if (owner == null || a != owner) out.add(a);
            }
        }
        return out;
    }

    private int distance(Position center, Entity entity) {
        if (center == null || entity == null) return Integer.MAX_VALUE;
        return Math.abs(center.getX() - entity.getX()) + Math.abs(center.getY() - entity.getY());
    }

    private int distance(Position center, Position target) {
        if (center == null || target == null) return Integer.MAX_VALUE;
        return Math.abs(center.getX() - target.getX()) + Math.abs(center.getY() - target.getY());
    }
    public Position getRandomWalkablePosInChunk(Chunk chunk) {
        if (chunk == null || worldMap == null) return null;
        try {
            Field f = WorldMap.class.getDeclaredField("chunkMap");
            f.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) f.get(worldMap);
            for (int cy = 0; cy < chunkMap.length; cy++) for (int cx = 0; cx < chunkMap[cy].length; cx++) if (chunkMap[cy][cx] == chunk) {
                int startX = cx * 50, startY = cy * 50;
                for (int attempt = 0; attempt < 50; attempt++) {
                    int rx = startX + rand.nextInt(50);
                    int ry = startY + rand.nextInt(50);
                    Terrain t = worldMap.getTile(rx, ry);
                    if (t != null && t.isPassable()) return new Position(rx, ry);
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public boolean isWalkable(Position p) {
        if (worldMap == null || p == null) return true;
        try {
            if (p.getX() < 0 || p.getX() >= 500 || p.getY() < 0 || p.getY() >= 500) return false;
            Terrain t = worldMap.getTile(p.getX(), p.getY());
            return t != null && t.isPassable();
        } catch (Exception e) { return false; }
    }
}
