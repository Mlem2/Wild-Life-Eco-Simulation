package brain.strategy;

import java.util.Random;

import brain.controller.MapSystem;
import entities.base.Animals;
import entities.base.Position;

public class PassiveStrategy implements MoveStrategy {
    private final Random random = new Random();
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        // 70% đi dạo ngẫu nhiên trong chunk hoặc chunk khác, 30% đứng yên
        if (random.nextDouble() < 0.7) {
            Position pos;
            if (owner instanceof entities.attributes.Herbivore) {
                // Herbivores prefer safer chunks even when passive
                java.util.List<core.enviroment.Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
                java.util.List<core.enviroment.Chunk> safestChunks = new java.util.ArrayList<>();
                
                core.enviroment.Chunk currentChunk = mapSystem.getChunkAt(owner.getPosition());
                int minPredators = mapSystem.getEnemiesInChunk(currentChunk, owner).size();
                safestChunks.add(currentChunk);

                for (core.enviroment.Chunk chunk : visibleChunks) {
                    if (chunk == currentChunk) continue;
                    int predatorCount = mapSystem.getEnemiesInChunk(chunk, owner).size();
                    if (predatorCount < minPredators) {
                        minPredators = predatorCount;
                        safestChunks.clear();
                        safestChunks.add(chunk);
                    } else if (predatorCount == minPredators) {
                        safestChunks.add(chunk);
                    }
                }
                
                core.enviroment.Chunk chosenChunk = safestChunks.get(random.nextInt(safestChunks.size()));
                if (chosenChunk != currentChunk) {
                    pos = mapSystem.getRandomWalkablePosInChunk(chosenChunk);
                } else {
                    pos = mapSystem.getRandomWalkablePosInVisibleChunk(owner.getPosition());
                }
            } else if (owner instanceof entities.attributes.Carnivore) {
                java.util.List<core.enviroment.Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
                java.util.List<core.enviroment.Chunk> bestChunks = new java.util.ArrayList<>();
                
                core.enviroment.Chunk currentChunk = mapSystem.getChunkAt(owner.getPosition());
                int maxHerbivoreCount = currentChunk.getEntitiesCountByType(entities.attributes.Herbivore.class);
                bestChunks.add(currentChunk);

                for (core.enviroment.Chunk chunk : visibleChunks) {
                    if (chunk == currentChunk) continue;
                    int herbivoreCount = chunk.getEntitiesCountByType(entities.attributes.Herbivore.class);
                    if (herbivoreCount > maxHerbivoreCount) {
                        maxHerbivoreCount = herbivoreCount;
                        bestChunks.clear();
                        bestChunks.add(chunk);
                    } else if (herbivoreCount == maxHerbivoreCount) {
                        bestChunks.add(chunk);
                    }
                }
                core.enviroment.Chunk chosenChunk = bestChunks.get(random.nextInt(bestChunks.size()));
                pos = mapSystem.getRandomWalkablePosInChunk(chosenChunk);
            } else {
                pos = mapSystem.getRandomWalkablePosInVisibleChunk(owner.getPosition());
            }
            
            // Nếu là động vật trên cạn (không phải Fish), không đi vào nước khi ở Passive strategy
            if (!(owner instanceof entities.Fish)) {
                core.enviroment.Terrain terrain = mapSystem.getTerrainAt(pos);
                if (terrain != null && terrain.isWater()) {
                    return owner.getPosition(); // Đứng yên nếu mục tiêu ngẫu nhiên là nước
                }
            } else {
                // Nếu là Fish, không đi lên cạn
                core.enviroment.Terrain terrain = mapSystem.getTerrainAt(pos);
                if (terrain != null && !terrain.isWater()) {
                    return owner.getPosition();
                }
            }
            
            return pos;
        }
        return owner.getPosition();
    }    
}