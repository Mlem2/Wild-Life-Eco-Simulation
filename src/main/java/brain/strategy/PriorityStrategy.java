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
        if (owner.getThirstPercentage() < 70) {
            // For aquatic animals, get all water; for terrestrial, get only shore water
            boolean isAquatic = owner instanceof entities.attributes.Aquatic;
            List<Position> waterSources = isAquatic 
                ? mapSystem.getWaterInChunks(visibleChunks)
                : mapSystem.getShoreWaterPositions(visibleChunks);
            
            // Nếu có nguồn nước nào trong tầm nhìn, ưu tiên di chuyển tới đó
            if (waterSources != null && !waterSources.isEmpty()) {
                // Filter cho phù hợp với loại động vật (aquatic/terrestrial)
                if (!(owner instanceof entities.attributes.Aquatic)) {
                    return mapSystem.getClosestShoreWater(owner.getPosition(), visibleChunks);
                }
                else{
                Position water = mapSystem.getClosestSuitablePosition(owner.getPosition(), waterSources, owner);
                if (water != null) {
                    owner.lockTargetEntity(water);
                    return water;
                }
            }
            }
            // If no water found, move towards the best chunk in heat map (only for aquatic)
            Chunk bestChunk = mapSystem.getBestWaterChunk(visibleChunks, owner);
            if (bestChunk != null && bestChunk.getDistanceToWater() < Integer.MAX_VALUE) {
                Position suitablePos = mapSystem.getRandomSuitablePosInChunk(bestChunk, owner);
                if (suitablePos != null) return suitablePos;
            }
        }

        // ƯU TIÊN 2: Đói bụng
        if (owner.getHungerPercentage() < owner.getThirstPercentage()) {
            // Check if this is an aquatic herbivore - they eat water instead of land food
            if (owner instanceof entities.attributes.AquaticHerbivore) {
                // Aquatic herbivores (Fish) recover hunger from water - seek any water
                List<Position> waterSources = mapSystem.getWaterInChunks(visibleChunks);
                if (waterSources != null && !waterSources.isEmpty()) {
                    // Filter to only water positions (extra safety)
                    Position water = mapSystem.getClosestSuitablePosition(owner.getPosition(), waterSources, owner);
                    if (water != null) {
                        owner.lockTargetEntity(water);
                        return water;
                    }
                }
            } else {
                // Regular terrestrial herbivores seek grass/food
                List<Position> foodSources = mapSystem.getFoodInChunks(visibleChunks);
                if (foodSources != null && !foodSources.isEmpty()) {
                    // Filter to only suitable terrain for this animal
                    Position food = mapSystem.getClosestSuitablePosition(owner.getPosition(), foodSources, owner);
                    if (food != null) {
                        owner.lockTargetEntity(food);
                        return food;
                    }
                }
            }
        }

        // Nếu không tìm thấy tài nguyên cụ thể nào dù đang có nhu cầu -> Đi lang thang tìm kiếm
        return mapSystem.getSafeRandomChunkPosition(visibleChunks, owner);
    }
}