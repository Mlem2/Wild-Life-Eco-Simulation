package brain.strategy;

import java.util.List;

import brain.controller.MapSystem;
import core.enviroment.Chunk;
import entities.base.Animals;
import entities.base.Position;

public class ScaredStrategy implements MoveStrategy {
    @Override
    public Position getTarget(Animals owner, MapSystem mapSystem) {
        Chunk currentChunk = mapSystem.getChunkAt(owner.getPosition());
        List<Animals> enemiesInMyChunk = mapSystem.getEnemiesInChunk(currentChunk, owner);

        // NGUY HIỂM CAO: Kẻ địch ở cùng chunk -> Kích hoạt Speed up để chạy trốn
        if (enemiesInMyChunk != null && !enemiesInMyChunk.isEmpty()) {
            owner.setSpeedUp(true);

            // Tìm trong 5 chunk xung quanh xem chunk nào an toàn nhất để chạy vào
            List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
            Chunk safestChunk = currentChunk;
            int minEnemyCount = enemiesInMyChunk.size();

            for (Chunk chunk : visibleChunks) {
                int enemyCount = mapSystem.getEnemiesInChunk(chunk, owner).size();
                if (enemyCount < minEnemyCount) {
                    minEnemyCount = enemyCount;
                    safestChunk = chunk;
                }
            }
            return mapSystem.getRandomWalkablePosInChunk(safestChunk);
        }

        // Nếu kẻ địch chỉ ở chunk lân cận (chưa vào cùng chunk) -> Tắt Speed up (Chạy vừa phải)
        owner.setSpeedUp(false);
        List<Chunk> visibleChunks = mapSystem.getVisibleChunks(owner.getPosition());
        List<Animals> allVisibleEnemies = mapSystem.getEnemiesInChunks(visibleChunks, owner);

        if (allVisibleEnemies == null || allVisibleEnemies.isEmpty()) return null;

        return mapSystem.getSafeRandomChunkPosition(visibleChunks, owner);
    }
}