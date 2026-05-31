package brain.strategy;

import java.util.List;

import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.base.Animals;
import entities.base.Position;

/*
 * Hunter Strategy
 *
 * Dành cho thú săn mồi.
 *
 * Hành vi:
 * - Tìm prey gần nhất
 * - Di chuyển tới prey
 */
public class HunterStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());

        // Nếu đói, ưu tiên tìm thức ăn/con mồi gần nhất
        List<Position> foodSources = mapSystem.getFoodInChunks(visibleChunks);
        if (foodSources != null && !foodSources.isEmpty()) {
            return mapSystem.getClosestPosition(owner.getPosition(), foodSources);
        }

        List<Animals> preys = mapSystem.getPreysInChunks(visibleChunks, owner);
        if (preys != null && !preys.isEmpty()) {
            Animals closestPrey = mapSystem.getClosestAnimal(owner.getPosition(), preys);
            owner.lockTargetEntity(closestPrey); // Khóa mục tiêu để ActionManager xử lý bám đuổi
            return closestPrey.getPosition();
        }

        // Đang đói mà xung quanh không có gì -> Di chuyển sang một chunk an toàn ngẫu nhiên để tìm tiếp
        return mapSystem.getSafeRandomChunkPosition(visibleChunks, owner);
    }
}