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
     */
    public void move(Position nextStep) {
        if (!mapSystem.isWalkable(nextStep)) return;

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

        // ÁP DỤNG LOGIC COOLDOWN THEO ĐỀ BÀI:
        if (owner.isSpeedUp()) {
            int cd = owner.getOwnMaxSpeedCooldown();
            owner.setCurrentMoveCooldown(cd);
        } else {
            owner.setCurrentMoveCooldown(owner.getDefaultMoveCooldown());
        }
        // `owner.setCurrentMoveCooldown` already updated above.
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
        owner.setCurrentMoveCooldown(1); // Ăn uống cũng tốn CD nhưng rất nhanh để khuyến khích tiêu thụ tài nguyên khi đã tiếp cận được
    }

    public void eat(Animals prey) {
        if (prey == null) return;

        int hungerGain = prey.getHungerRecoveryAmount();
        int thirstGain = prey.getThirstRecoveryAmount();
        mapSystem.removeEntity(prey);
        owner.lockTargetEntity(null);
        owner.increaseHunger(hungerGain);
        owner.increaseHydration(thirstGain);
        owner.setCurrentMoveCooldown(1); // Ăn thỏ cũng instant kill, không dùng cơ chế HP/attack
    }

    public void drink() {
        drink(null);
    }

    public void drink(Water water) {
        int gained = owner.getThirstRecoveryAmount();
        if (water != null) {
            water.consume(gained);
        }
        owner.increaseHydration(gained);
        owner.setCurrentMoveCooldown(1); // Uống nước cũng tốn CD nhưng rất nhanh để khuyến khích tiêu thụ tài nguyên khi đã tiếp cận được
    }

    public void attack(Animals prey) {
        eat(prey);
    }
}