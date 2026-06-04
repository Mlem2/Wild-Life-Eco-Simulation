package brain.strategy;

import java.util.Random;

import brain.controller.MapSystem;
import entities.base.Animals;
import entities.base.Position;

public class PassiveStrategy implements MoveStrategy {
    private final Random random = new Random();
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        // 70% đi dạo ngẫu nhiên trong chunk, 30% đứng yên
        if (random.nextDouble() < 0.7) {
            // Use animal-aware method to find suitable position
            Position suitablePos = mapSystem.getRandomSuitablePosInChunk(
                mapSystem.getChunkAt(owner.getPosition()), 
                owner
            );
            if (suitablePos != null) {
                return suitablePos;
            }
            // If no suitable position found, stay in place rather than going to random walkable (which might be wrong terrain)
            return owner.getPosition();
        }
        return owner.getPosition();
    }    
}