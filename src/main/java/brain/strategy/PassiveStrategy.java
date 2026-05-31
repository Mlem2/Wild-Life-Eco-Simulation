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
            return mapSystem.getRandomWalkablePosInChunk(owner.getPosition());
        }
        return owner.getPosition();
    }    
}