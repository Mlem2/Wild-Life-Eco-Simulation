package brain.strategy;

import java.util.List;

import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.base.Animals;
import entities.base.Position;


public class MatingStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());

        // Tìm bạn tình gần nhất trong tầm nhìn
        List<Animals> potentialMates = mapSystem.getPotentialMatesInChunks(visibleChunks, owner);
        if (potentialMates != null && !potentialMates.isEmpty()) {
            Animals closestMate = mapSystem.getClosestAnimal(owner.getPosition(), potentialMates);
            owner.lockTargetEntity(closestMate); // Khóa mục tiêu để ActionManager xử lý bám đuổi
            return closestMate.getPosition();
        }

        // Nếu không tìm thấy bạn tình, có thể đứng yên hoặc đi dạo ngẫu nhiên
        return owner.getPosition(); // Hoặc có thể sử dụng một chiến thuật khác như PassiveStrategy để đi dạo
    }    
}
