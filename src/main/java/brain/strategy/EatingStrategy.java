package brain.strategy;

import java.util.List;

import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.base.Animals;
import entities.base.Position;

public class EatingStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
        owner.setSpeedUp(false);

        if (owner instanceof entities.attributes.Herbivore) {
            if (owner instanceof entities.Elephant) {
                List<Position> plants = mapSystem.getPlantsInChunks(visibleChunks);
                if (!plants.isEmpty()) {
                    Position bush = mapSystem.getClosestPosition(owner.getPosition(), plants);
                    owner.lockTargetEntity(bush);
                    return bush;
                }
            }
            // Herbivores seek grass if no entities are found
            List<Position> grassTiles = mapSystem.getGrassInChunks(visibleChunks);
            if (!grassTiles.isEmpty()) {
                Position grass = mapSystem.getClosestPosition(owner.getPosition(), grassTiles);
                owner.lockTargetEntity(grass);
                return grass;
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
