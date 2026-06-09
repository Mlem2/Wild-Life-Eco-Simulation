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
            pos = mapSystem.getRandomWalkablePosInVisibleChunk(owner.getPosition());

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