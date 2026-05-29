package brain.strategy;

import java.util.List;

import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.attributes.Carnivore;
import entities.base.Animals;
import entities.base.Position;

public class AggressiveStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        // Chỉ thú săn mồi (Carnivore) mới sử dụng chiến thuật này
        if (!(owner instanceof Carnivore)) return null;

        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
        List<Animals> preys = mapSystem.getPreysInChunks(visibleChunks, owner);

        if (preys == null || preys.isEmpty()) return null;

        // Khóa con mồi gần nhất và đưa con vật vào trạng thái Speed up
        Animals closestPrey = mapSystem.getClosestAnimal(owner.getPosition(), preys);
        owner.lockTargetEntity(closestPrey);
        owner.setSpeedUp(true); // Bật trạng thái tăng tốc tối đa

        return closestPrey.getPosition();
    }
}