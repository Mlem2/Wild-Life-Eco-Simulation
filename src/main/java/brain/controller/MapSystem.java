package brain.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.enviroment.Chunk;
import core.enviroment.Terrain;
import core.enviroment.WorldMap;
import entities.base.*;

/**
 * Lightweight MapSystem facade used by brain strategies.
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

    public boolean hasPreyAround(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals other && isPrey(owner, other)) return true;
            }
        }
        return false;
    }

    public boolean hasMateAround(Animals owner) {
        List<Chunk> chunks = getVisibleChunks(owner.getPosition());
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals other && isPotentialMate(owner, other)) return true;
            }
        }
        return false;
    }

    public List<Animals> getMatesInChunks(List<Chunk> chunks, Animals owner) {
        List<Animals> res = new ArrayList<>();
        for (Chunk c : chunks) {
            if (c == null) continue;
            for (Entity e : c.getEntityList()) {
                if (e instanceof Animals other && isPotentialMate(owner, other)) res.add(other);
            }
        }
        return res;
    }

    public boolean isPotentialMate(Animals owner, Animals other) {
        if (other == owner || !other.checkAlive()) return false;
        if (other.getClass() != owner.getClass()) return false;
        return other.isReadyToMate();
    }

    public boolean isThreateningEnemy(Animals owner, Animals other) {
        if (other == owner || !other.checkAlive()) return false;
        if (other.getState() == allEnum.State.HIDING) return false;
        if (!(other instanceof entities.attributes.Carnivore)) return false;
        if (owner instanceof entities.attributes.Carnivore) {
            return other.getSize().ordinal() > owner.getSize().ordinal();
        }


        // Elephants are not threatened by anyone in this simulation context
        return !(owner instanceof entities.Elephant);
    }

    public boolean isPrey(Animals owner, Animals other) {
        if (other == owner || !other.checkAlive()) return false;
        if (other.getState() == allEnum.State.HIDING) return false;
        if (!(owner instanceof entities.attributes.Carnivore)) return false;
        if (other instanceof entities.Elephant) return false;

        // Carnivore only eat entities that extend herbivore and smaller (in SIZE) carnivores except for elephants
        if (other instanceof entities.attributes.Herbivore) return true;
        if (other instanceof entities.attributes.Carnivore) {
            // Check if smaller in SIZE. Enum Size: SMALL(1), MEDIUM(2), LARGE(5).
            // ordinal() can be used: SMALL is 0, MEDIUM is 1, LARGE is 2.
            return other.getSize().ordinal() < owner.getSize().ordinal();
        }

        return false;
    }

    public List<Chunk> getVisibleChunks(Position pos) {
        List<Chunk> out = new ArrayList<>();
        if (worldMap == null || pos == null) return out;

        Chunk[][] chunkMap = worldMap.chunkMap;

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
                    if (e instanceof Animals other && isPrey(owner, other)) out.add(other);
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
        }
        return out;
    }

    public List<Position> getPlantsInChunks(List<Chunk> chunks) {
        List<Position> out = new ArrayList<>();
        if (chunks == null) return out;
        for (Chunk c : chunks) {
            if (c == null) continue;
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    if (e instanceof Plant) {
                        out.add(Position.of(e.getX(), e.getY()));
                    }
                }
            }
        }
        return out;
    }

    public boolean isBushOccupied(Position pos) {
        Chunk chunk = getChunkAt(pos);
        if (chunk == null) return false;
        synchronized (chunk.getEntityList()) {
            for (Entity e : chunk.getEntityList()) {
                if (e instanceof Animals animal) {
                    if (animal.getPosition().equals(pos) && animal.getState() == allEnum.State.HIDING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Position> getAvailableBushesInChunks(List<Chunk> chunks) {
        List<Position> out = new ArrayList<>();
        if (chunks == null) return out;
        for (Chunk c : chunks) {
            if (c == null) continue;
            synchronized (c.getEntityList()) {
                for (Entity e : c.getEntityList()) {
                    if (e instanceof entities.Bush) {
                        Position pos = Position.of(e.getX(), e.getY());
                        if (!isBushOccupied(pos)) {
                            out.add(pos);
                        }
                    }
                }
            }
        }
        return out;
    }

    public List<Position> getGrassInChunks(List<Chunk> chunks) {
        List<Position> out = new ArrayList<>();
        if (chunks == null || worldMap == null) return out;
        
        Chunk[][] chunkMap = worldMap.chunkMap;
        if (chunkMap == null) return out;

        for (Chunk c : chunks) {
            if (c == null) continue;
            
            // Find coordinates of this chunk
            int cx = -1, cy = -1;
            for (int y = 0; y < chunkMap.length; y++) {
                for (int x = 0; x < chunkMap[0].length; x++) {
                    if (chunkMap[y][x] == c) {
                        cx = x; cy = y;
                        break;
                    }
                }
                if (cx != -1) break;
            }

            if (cx != -1) {
                int startX = cx * WorldMap.CHUNK_SIZE;
                int startY = cy * WorldMap.CHUNK_SIZE;
                for (int y = startY; y < startY + WorldMap.CHUNK_SIZE; y++) {
                    for (int x = startX; x < startX + WorldMap.CHUNK_SIZE; x++) {
                        if (x >= 0 && x < WorldMap.SIZE && y >= 0 && y < WorldMap.SIZE) {
                            Terrain t = worldMap.getTile(x, y);
                            if (t != null && t.isGrass()) {
                                out.add(Position.of(x, y));
                            }
                        }
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

    public void addEntity(Entity entity) {
        if (entity == null) return;
        Chunk chunk = getChunkAt(Position.of(entity.getX(), entity.getY()));
        if (chunk != null) chunk.addEntity(entity);
    }

    public Terrain getTerrainAt(Position pos) {
        if (worldMap == null || pos == null) return null;
        try {
            return worldMap.getTile(pos.getX(), pos.getY());
        } catch (Exception e) {
            return null;
        }
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

    public Position getRandomWalkablePosInVisibleChunk(Animals owner) {
        Position pos = owner.getPosition();
        List<Chunk> chunks = getVisibleChunks(pos);
        if (chunks.isEmpty()) return pos;
        Chunk c = chunks.get(rand.nextInt(chunks.size()));
        Position out = getRandomWalkablePosInChunk(c);
        return out != null ? out : pos;
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

    private int distance(Position center, Entity entity) {
        if (center == null || entity == null) return Integer.MAX_VALUE;
        return Math.abs(center.getX() - entity.getX()) + Math.abs(center.getY() - entity.getY());
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
}
