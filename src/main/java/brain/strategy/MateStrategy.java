package brain.strategy;

import java.util.List;
import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.base.Animals;
import entities.base.Position;

public class MateStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
        List<Animals> potentialMates = mapSystem.getMatesInChunks(visibleChunks, owner);
        
        if (!potentialMates.isEmpty()) {
            Animals closestMate = mapSystem.getClosestAnimal(owner.getPosition(), potentialMates);
            owner.lockTargetEntity(closestMate);
            return closestMate.getPosition();
        }
        
        // No mate found, wander around to find one
        return mapSystem.getSafeRandomChunkPosition(visibleChunks, owner);
    }
}
