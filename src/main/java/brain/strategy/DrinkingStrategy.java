package brain.strategy;

import java.util.List;

import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.base.Animals;
import entities.base.Position;

public class DrinkingStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
        owner.setSpeedUp(false);

        List<Position> waterSources = mapSystem.getWaterInChunks(visibleChunks);
        if (waterSources != null && !waterSources.isEmpty()) {
            Position water = mapSystem.getClosestPosition(owner.getPosition(), waterSources);
            owner.lockTargetEntity(water);
            return water;
        } else {
            // If no water found in visible chunks, move towards the best chunk in heat map
            Chunk bestChunk = mapSystem.getBestWaterChunk(visibleChunks);
            if (bestChunk != null && bestChunk.getDistanceToWater() < Integer.MAX_VALUE) {
                return mapSystem.getRandomWalkablePosInChunk(bestChunk);
            }
        }

        // Nếu không tìm thấy tài nguyên cụ thể nào dù đang có nhu cầu -> Đi lang thang tìm kiếm
        Position safePos = mapSystem.getSafeRandomChunkPosition(visibleChunks, owner);
        if (owner instanceof entities.Fish) {
            core.enviroment.Terrain t = mapSystem.getTerrainAt(safePos);
            if (t != null && !t.isWater()) {
                return owner.getPosition();
            }
        }
        return safePos;
    }
}
