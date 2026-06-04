package brain.controller;

import core.enviroment.Chunk;
import entities.Food;
import entities.Water;
import entities.base.Animals;
import entities.base.Position;

public class ActionManager {
    private final Animals owner;
    private final MapSystem mapSystem;

    // Using per-animal default cooldowns instead of a global fixed cooldown

    public ActionManager(Animals owner, MapSystem mapSystem) {
        this.owner = owner;
        this.mapSystem = mapSystem;
    }

    public boolean isAvailable() {
        // Use the owner's shared `currentMoveCooldown` as the single source
        // of truth for move/action cooldown. The global simulation loop
        // already decrements that per tick via `updateMoveCooldown`.
        return owner.getCurrentMoveCooldown() <= 0;
    }

    /**
     * Thực hiện di chuyển sang ô kế tiếp
     * @return true nếu di chuyển thành công, false nếu bị từ chối
     */
    public boolean move(Position nextStep) {
        if (!mapSystem.isWalkable(nextStep)) return false;
        
        // Kiểm tra xem terrain có phù hợp với loại động vật không (cá phải ở dưới nước, v.v.)
        if (!mapSystem.isTerrainSuitableForAnimal(nextStep, owner)) return false;

        // Update chunk membership: compute old/new chunks, move the entity between them if needed
        Chunk oldChunk = null;
        Chunk newChunk = null;
        try {
            oldChunk = mapSystem.getChunkAt(owner.getPosition());
            newChunk = mapSystem.getChunkAt(nextStep);
        } catch (Exception ignored) {}

        owner.setPosition(nextStep);

        if (oldChunk != newChunk) {
            try {
                if (oldChunk != null) oldChunk.removeEntity(owner);
            } catch (Exception ignored) {}
            try {
                if (newChunk != null) newChunk.addEntity(owner);
            } catch (Exception ignored) {}
        }

        // ÁP DỤNG LOGIC COOLDOWN:
        if (owner.isSpeedUp()) {
            int cd = owner.getOwnMaxSpeedCooldown();
            owner.setCurrentMoveCooldown(cd);
        } else {
            owner.setCurrentMoveCooldown(owner.getDefaultMoveCooldown());
        }
        return true;
    }

    public MapSystem getMapSystem() {
        return mapSystem;
    }

    public void eat(Food food) {
        if (food == null) return;

        int hungerGain = food.getHungerRecoveryAmount();
        int thirstGain = food.getThirstRecoveryAmount();
        food.consume(hungerGain);
        mapSystem.removeEntity(food);
        owner.lockTargetEntity(null);
        owner.increaseHunger(hungerGain);
        owner.increaseHydration(thirstGain);
        owner.setCurrentMoveCooldown(10); // Ăn cũng tốn CD
    }

    public void eat(Animals prey) {
        if (prey == null) return;

        int hungerGain = prey.getHungerRecoveryAmount();
        int thirstGain = prey.getThirstRecoveryAmount();
        mapSystem.removeEntity(prey);
        owner.lockTargetEntity(null);
        owner.increaseHunger(hungerGain);
        owner.increaseHydration(thirstGain);
        owner.setCurrentMoveCooldown(15); // Ăn thịt lâu hơn chút
    }

    public void drink() {
        drink(null);
    }

    public static void setCooldown(Animals animal, int ticks) {
        animal.setCurrentMoveCooldown(ticks);
    }

    public void drink(Water water) {
        int gained = owner.getThirstRecoveryAmount();
        if (water != null) {
            water.consume(gained);
        }
        owner.increaseHydration(gained);
        owner.setCurrentMoveCooldown(10); // Uống nước cũng tốn CD nhưng nhanh hơn
    }

    public void attack(Animals prey) {
        eat(prey);
    }

    public void mating(Animals animal) {
        owner.setCurrentMoveCooldown(owner.getMatingTimeCost()); // Cooldown cho giao phối
        // Sau khi giao phối xong, sẽ có logic sinh con ở AnimalBrainUpdate dựa trên việc đã giao phối thành công hay chưa (có thể dựa vào một cờ hiệu hoặc kiểm tra xem bạn tình còn tồn tại không)
        owner.recordMating();
        owner.setCurrentMoveCooldown(owner.getDefaultMoveCooldown()); // Nghỉ tý :D
    }
}