package brain.controller;

import core.enviroment.Chunk;
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

        // Fish constraint: only water. Others: everything but water (mostly)
        // Actually, land animals CAN go to water if they want to (Hunter/Priority drink),
        // but the previous requirement was specifically for PassiveStrategy to avoid water.
        // However, THIS requirement says "fishes can't go on land". This sounds like a hard constraint.
        core.enviroment.Terrain targetTerrain = mapSystem.getTerrainAt(nextStep);
        if (owner instanceof entities.Fish) {
            if (targetTerrain != null && !targetTerrain.isWater()) return;
        } else {
            // If we want to strictly prevent land animals from entering water AT ALL (not just passive), we'd put it here.
            // But the previous task only asked for Passive strategy. 
            // The current task is "fishes can't go on land", which I'll enforce here.
        }

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
        double multiplier = 1.0;
        try {
            multiplier = mapSystem.getTerrainAt(nextStep).getSpeedMultiplier();
        } catch (Exception ignored) {}

        if (owner.isSpeedUp()) {
            int cd = owner.getOwnMaxSpeedCooldown();
            owner.setCurrentMoveCooldown((int) Math.round(cd / multiplier));
        } else {
            owner.setCurrentMoveCooldown((int) Math.round(owner.getDefaultMoveCooldown() / multiplier));
        }
        // `owner.setCurrentMoveCooldown` already updated above.
    }

    public MapSystem getMapSystem() {
        return mapSystem;
    }

    public static void setCooldown(Animals animal, int cooldown) {
        animal.setCurrentMoveCooldown(cooldown);
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

    public void eatGrass() {
        int hungerGain = 10;
        int thirstGain = 3;

        try {
            Position pos = owner.getPosition();
            if (pos != null) {
                Chunk currentChunk = mapSystem.getChunkAt(pos);
                if (currentChunk != null) {
                    int herbivoresInChunk = currentChunk.getEntitiesCountByType(entities.attributes.Herbivore.class);
                    // Each herbivore in the chunk reduces the grass nutrition available
                    // Formula: gain = base / (1 + (herbivores-1) * 0.1)
                    // We use (herbivores-1) because the owner is also a herbivore and should be counted,
                    // but 1 herbivore should get full nutrition.
                    if (herbivoresInChunk > 1) {
                        double population = (herbivoresInChunk - 1) * 0.5;
                        hungerGain = (int) Math.round(hungerGain - population);
                        thirstGain = (int) Math.round(thirstGain - population);
                    }
                }
            }
        } catch (Exception ignored) {}

        owner.increaseHunger(hungerGain);
        owner.increaseHydration(thirstGain);
        owner.setCurrentMoveCooldown(1);
    }

    public void attack(Animals prey) {
        if (prey == null) return;

        // Implement hunting success chance: 50%
        if (new java.util.Random().nextBoolean()) {
            // Success: act normally (eat the prey)
            owner.setMatingCooldown(owner.getMatingCooldown() - 500);
            eat(prey);
        } else {
            // Failure: prey slips away, predator is stunned for 40 ticks
            owner.lockTargetEntity(null);
            owner.setCurrentMoveCooldown(20);
        }
    }

    public void mate(Animals partner) {
        if (partner == null) return;
        // Basic check, in case someone else already mated with them or they are not ready anymore
        if (!owner.isReadyToMate() || !partner.isReadyToMate()) return;

        try {
            // New animal is born at owner's position
            Animals offspring = owner.getClass().getConstructor(int.class, int.class)
                    .newInstance(owner.getX(), owner.getY());
            mapSystem.addEntity(offspring);

            // Reset mating urge and set cooldown for both parents
            owner.setMatingCooldown(owner.getDefaultMatingCooldown());
            partner.setMatingCooldown(partner.getDefaultMatingCooldown());

            owner.lockTargetEntity(null);
            owner.setCurrentMoveCooldown(10); // Mating takes some time
        } catch (Exception e) {
            // Error during mating
        }
    }
}