package brain.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.enviroment.Chunk;
import core.enviroment.Terrain;
import core.enviroment.WorldMap;
import entities.Bush;
import entities.Food;
import entities.Trees;
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
                    if (dist <= 8) return true;
                }
            }
        }
        return false;
    }

    public boolean hasPreyNearby(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals && e != owner && e instanceof entities.attributes.Herbivore && !(e instanceof entities.attributes.Apex)) {
                    int dist = Math.abs(owner.getX() - e.getX()) + Math.abs(owner.getY() - e.getY());
                    if (dist <= 10) return true;
                }
            }
        }
        return false;
    }

    public boolean hasPreyAround(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals && e != owner && e instanceof entities.attributes.Herbivore && !(e instanceof entities.attributes.Apex)) return true;
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

        Chunk[][] chunkMap = worldMap.chunkMap;
        if (chunkMap == null) return out;

        int cx = pos.getX() / WorldMap.CHUNK_SIZE;
        int cy = pos.getY() / WorldMap.CHUNK_SIZE;
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
        Chunk[][] chunkMap = worldMap.chunkMap;
        int cx = pos.getX() / WorldMap.CHUNK_SIZE;
        int cy = pos.getY() / WorldMap.CHUNK_SIZE;
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
                    if (e instanceof Animals other && e != owner && other instanceof entities.attributes.Herbivore && !(other instanceof entities.attributes.Apex)) out.add(other);
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
                        out.add(Position.of(e.getX(), e.getY()));
                    }
                }
            }
        }
        return out;
    }

    /**
     * Get water positions that are on the shore (adjacent to land).
     * Useful for terrestrial animals to drink from the edge without entering water.
     */
    public List<Position> getShoreWaterPositions(List<Chunk> chunks) {
        List<Position> waterPositions = getWaterInChunks(chunks);
        List<Position> shorePositions = new ArrayList<>();
        
        if (waterPositions == null || waterPositions.isEmpty()) return shorePositions;
        
        // For each water position, check if it's adjacent to land
        for (Position waterPos : waterPositions) {
            if (isShorePosition(waterPos)) {
                shorePositions.add(waterPos);
            }
        }
        
        return shorePositions;
    }

    public Position getClosestShoreWater(Position from, List<Chunk> chunks) {
        List<Position> shoreWater = getShoreWaterPositions(chunks);
        return getClosestPosition(from, shoreWater);
    }
    /**
     * Check if a position is on the shore (water adjacent to land).
     */
    private boolean isShorePosition(Position pos) {
        if (pos == null || worldMap == null) return false;
        
        try {
            Terrain waterTerrain = worldMap.getTile(pos.getX(), pos.getY());
            if (waterTerrain == null || !waterTerrain.isWater()) return false;
            
            // Check all 8 adjacent positions for non-water land
            int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
            };
            
            for (int[] dir : directions) {
                int adjX = pos.getX() + dir[0];
                int adjY = pos.getY() + dir[1];
                
                // Check bounds
                if (adjX < 0 || adjX >= WorldMap.SIZE || adjY < 0 || adjY >= WorldMap.SIZE) continue;
                
                Terrain adjTerrain = worldMap.getTile(adjX, adjY);
                // If adjacent position is non-water and passable, this is shore
                if (adjTerrain != null && adjTerrain.isPassable() && !adjTerrain.isWater()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }

    public List<Position> getFoodInChunks(List<Chunk> chunks) {
        List<Position> out = new ArrayList<>();
        if (chunks == null) return out;
        for (Chunk c : chunks) {
            if (c == null) continue;
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    // Exclude Bush and Trees - they are not food for animals anymore
                    if ((e instanceof Food || e instanceof entities.base.ResourceEntity) 
                        && !(e instanceof Bush) 
                        && !(e instanceof Trees)) {
                        out.add(Position.of(e.getX(), e.getY()));
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
        Chunk chunk = getChunkAt(Position.of(entity.getX(), entity.getY()));
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
        if (chunks == null || chunks.isEmpty()) return null;
        if (owner == null) return null;

        boolean isAquatic = owner instanceof entities.attributes.Aquatic;

        // If thirsty, try to move towards water (only for aquatic animals)
        if (owner.getThirstPercentage() < 60 && isAquatic) {
            Chunk bestChunk = getBestWaterChunk(chunks, owner);
            if (bestChunk != null && bestChunk.getDistanceToWater() < Integer.MAX_VALUE) {
                Position suitablePos = getRandomSuitablePosInChunk(bestChunk, owner);
                if (suitablePos != null) return suitablePos;
            }
        }

        // Try multiple chunks to find suitable position
        List<Chunk> shuffledChunks = new ArrayList<>(chunks);
        java.util.Collections.shuffle(shuffledChunks);
        
        for (Chunk c : shuffledChunks) {
            if (c == null) continue;
            Position suitablePos = getRandomSuitablePosInChunk(c, owner);
            if (suitablePos != null) return suitablePos;
        }

        // No suitable position found - stay in place rather than risk wrong terrain
        return owner.getPosition();
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
        Chunk[][] chunkMap = worldMap.chunkMap;
        if (chunkMap == null) return out;
        
        int cx = center.getX() / WorldMap.CHUNK_SIZE;
        int cy = center.getY() / WorldMap.CHUNK_SIZE;
        
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
                int startX = cx * WorldMap.CHUNK_SIZE, startY = cy * WorldMap.CHUNK_SIZE;
                for (int attempt = 0; attempt < 50; attempt++) {
                    int rx = startX + rand.nextInt(WorldMap.CHUNK_SIZE);
                    int ry = startY + rand.nextInt(WorldMap.CHUNK_SIZE);
                    Terrain t = worldMap.getTile(rx, ry);
                    if (t != null && t.isPassable()) return Position.of(rx, ry);
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public boolean isWalkable(Position p) {
        if (worldMap == null || p == null) return true;
        try {
            if (p.getX() < 0 || p.getX() >= WorldMap.SIZE || p.getY() < 0 || p.getY() >= WorldMap.SIZE) return false;
            Terrain t = worldMap.getTile(p.getX(), p.getY());
            return t != null && t.isPassable();
        } catch (Exception e) { return false; }
    }

    /**
     * Check if terrain at position is suitable for the given animal.
     * Aquatic animals ONLY work in water; terrestrial animals avoid water.
     */
    public boolean isTerrainSuitableForAnimal(Position p, Animals owner) {
        if (worldMap == null || p == null || owner == null) return true;
        try {
            Terrain t = worldMap.getTile(p.getX(), p.getY());
            if (t == null || !t.isPassable()) return false;
            
            boolean isAquatic = owner instanceof entities.attributes.Aquatic;
            // Aquatic animals MUST be in water - grass/forest/mountain are all non-passable
            if (isAquatic) {
                return t.isWater();
            }
            // Terrestrial animals should NOT be in water
            return !t.isWater();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Filter positions suitable for an animal based on terrain and animal type.
     */
    public List<Position> filterSuitablePositions(List<Position> positions, Animals owner) {
        List<Position> suitable = new ArrayList<>();
        if (positions == null || owner == null) return suitable;
        
        for (Position p : positions) {
            if (isTerrainSuitableForAnimal(p, owner)) {
                suitable.add(p);
            }
        }
        return suitable;
    }

    /**
     * Get closest position that is suitable for the animal.
     */
    public Position getClosestSuitablePosition(Position from, List<Position> list, Animals owner) {
        List<Position> suitable = filterSuitablePositions(list, owner);
        return getClosestPosition(from, suitable);
    }

    /**
     * Get random walkable position in chunk that is suitable for the animal.
     */
    public Position getRandomSuitablePosInChunk(Chunk chunk, Animals owner) {
        if (chunk == null || worldMap == null || owner == null) return null;
        try {
            Field f = WorldMap.class.getDeclaredField("chunkMap");
            f.setAccessible(true);
            Chunk[][] chunkMap = (Chunk[][]) f.get(worldMap);
            boolean isAquatic = owner instanceof entities.attributes.Aquatic;
            
            for (int cy = 0; cy < chunkMap.length; cy++) {
                for (int cx = 0; cx < chunkMap[cy].length; cx++) {
                    if (chunkMap[cy][cx] == chunk) {
                        int startX = cx * WorldMap.CHUNK_SIZE;
                        int startY = cy * WorldMap.CHUNK_SIZE;
                        for (int attempt = 0; attempt < 100; attempt++) {
                            int rx = startX + rand.nextInt(WorldMap.CHUNK_SIZE);
                            int ry = startY + rand.nextInt(WorldMap.CHUNK_SIZE);
                            Terrain t = worldMap.getTile(rx, ry);
                            if (t != null && t.isPassable()) {
                                // Check terrain suitability
                                boolean isSuitable = isAquatic ? t.isWater() : !t.isWater();
                                if (isSuitable) {
                                    return Position.of(rx, ry);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }

    /**
     * Get best water chunk for an animal (suitable for aquatic animals).
     * Aquatic animals should seek water; terrestrial animals should not.
     */
    public Chunk getBestWaterChunk(List<Chunk> chunks, Animals owner) {
        if (chunks == null || chunks.isEmpty() || owner == null) return null;
        
        boolean isAquatic = owner instanceof entities.attributes.Aquatic;
        // Only aquatic animals should seek water
        if (!isAquatic) return null;
        
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

    // Trả về danh sách tiềm năng bạn đời gần vị trí nhất: cùng loài, trong tầm nhìn
    public List<Animals> getPotentialMatesInChunks(List<Chunk> chunks, Animals owner) {
        List<Animals> potentialMates = new ArrayList<>();
        if (chunks == null || owner == null) return potentialMates;

        for (Chunk c : chunks) {
            if (c == null) continue;
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    if (e instanceof Animals other && other != owner && other.getClass() == owner.getClass()) {
                        potentialMates.add(other);
                    }
                }
            }
        }
        return potentialMates;
    }
    // Trả về nơi trống xung quanh để tạo ra hậu duệ
    public Position findNearbyFreePosition(Position from, int radius) {
        if (worldMap == null || from == null) return null;
        for (int r = 1; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int nx = from.getX() + dx;
                    int ny = from.getY() + dy;
                    Position candidate = Position.of(nx, ny);
                    if (isWalkable(candidate) && getEntityAt(candidate) == null) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    public void addEntity(Entity entity) {
        if (worldMap == null || entity == null) return;
        Chunk chunk = getChunkAt(Position.of(entity.getX(), entity.getY()));
        if (chunk != null) chunk.addEntity(entity);
    }
}
