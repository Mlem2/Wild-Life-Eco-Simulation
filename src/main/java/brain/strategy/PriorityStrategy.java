package brain.strategy;

import java.util.List;

import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.base.Animals;
import entities.base.Position;

public class PriorityStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
        owner.setSpeedUp(false); // Ưu tiên sinh hoạt bình thường không tăng tốc

        // ƯU TIÊN 1: Khát nước nguy hiểm hơn đói (Ví dụ: Thể lực/Nước xuống dưới 60%)
        if (owner.getThirstPercentage() < 60) {
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
        }

        // ƯU TIÊN 2: Đói bụng
        if (owner.getHungerPercentage() < 60) {
            List<Position> foodSources = mapSystem.getFoodInChunks(visibleChunks);
            if (foodSources != null && !foodSources.isEmpty()) {
                Position food = mapSystem.getClosestPosition(owner.getPosition(), foodSources);
                owner.lockTargetEntity(food);
                return food;
            }
        }

        // Nếu không tìm thấy tài nguyên cụ thể nào dù đang có nhu cầu -> Đi lang thang tìm kiếm
        return mapSystem.getSafeRandomChunkPosition(visibleChunks, owner);
    }
}