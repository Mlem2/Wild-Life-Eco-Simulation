package brain.controller;

import allEnum.State;
import entities.attributes.Carnivore;
import entities.attributes.Herbivore;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.Tree;
import brain.scanner.TargetScanner;
import brain.strategy.AggressiveStrategy;
import brain.strategy.DrinkingStrategy;
import brain.strategy.EatingStrategy;
import brain.strategy.HunterStrategy;
import brain.strategy.MateStrategy;
import brain.strategy.MoveStrategy;
import brain.strategy.PassiveStrategy;
import brain.strategy.ScaredStrategy;

import java.util.List;

/// StateController
// Chịu trách nhiệm cập nhật state và strategy cho animal dựa trên môi trường xung quanh.
public class StateController {

    public void updateState(Animals animal, List<Entity> visibleEntities) {
        if (animal == null) {
            return;
        }

        if (shouldFlee(animal, visibleEntities)) {
            setStrategy(animal, State.SCARED, new ScaredStrategy());
            return;
        }

        // Herbivores prioritize Scared over Priority
        if (shouldDrink(animal, visibleEntities)) {
            setStrategy(animal, State.DRINKING, new DrinkingStrategy());
            return;
        }

        if (shouldEat(animal, visibleEntities)) {
            setStrategy(animal, State.EATING, new EatingStrategy());
            return;
        }

        if (shouldHunt(animal, visibleEntities)) {
            setStrategy(animal, State.HUNT, new HunterStrategy());
            return;
        }

        if (shouldAggressive(animal, visibleEntities)) {
            setStrategy(animal, State.AGGRESSIVE, new AggressiveStrategy());
            return;
        }

        if (shouldMate(animal, visibleEntities)) {
            setStrategy(animal, State.MATE, new MateStrategy());
            return;
        }

        setStrategy(animal, State.PASSIVE, new PassiveStrategy());
    }
    // Set strategy dựa trên state mới. Core sẽ gọi hàm move() của strategy này trong vòng lặp chính để lấy hướng di chuyển.
    private void setStrategy(Animals animal, State newState, MoveStrategy strategy) {
        animal.setState(newState);
        animal.setMoveStrategy(strategy);
    }
    
    private boolean shouldDrink(Animals animal, List<Entity> visibleEntities) {
        if (!(animal instanceof Herbivore)) {
            return false;
        }
        if (visibleEntities == null) {
            return false;
        }

        boolean hasWaterNearby = TargetScanner.findNearest(animal, visibleEntities, 50, entity -> entity instanceof entities.Water) != null;

        return (hasWaterNearby) && (animal.getThirst() < 70);
    }

    private boolean shouldEat(Animals animal, List<Entity> visibleEntities) {
        if (!(animal instanceof Herbivore)) {
            return false;
        }
        if (visibleEntities == null) {
            return false;
        }

        boolean hasFoodNearby = TargetScanner.findNearest(animal, visibleEntities, 50, entity -> {
            if (entity instanceof Tree) {
                return animal instanceof entities.Elephant;
            }
            return (entity instanceof entities.Food && !(entity instanceof entities.Water));
        }) != null;

        return (hasFoodNearby) && (animal.getHunger() < 70);
    }

    // Logic để quyết định khi nào nên săn mồi hoặc bỏ chạy. Có thể mở rộng thêm các yếu tố như sức khỏe, tuổi tác, v.v.
    private boolean shouldHunt(Animals animal, List<Entity> visibleEntities) {
        if (!(animal instanceof Carnivore)) {
            return false;
        }
        if (visibleEntities == null) {
            return false;
        }

        // We need to check if any of visibleEntities is a prey according to new rules.
        // Since we don't have MapSystem here, we'll do a simplified check matching MapSystem.isPrey
        boolean hasPreyNearby = TargetScanner.findNearest(animal, visibleEntities, 40, entity -> {
            if (entity instanceof Animals other) {
                if (other == animal || !other.checkAlive()) return false;
                if (other instanceof Herbivore) return true;
                if (other instanceof Carnivore) {
                    if (other instanceof entities.Elephant) return false;
                    return other.getSize().ordinal() < animal.getSize().ordinal();
                }
            }
            return false;
        }) != null;

        return hasPreyNearby && animal.getHunger() < 80;
    }
    private boolean shouldFlee(Animals animal, List<Entity> visibleEntities) {
        if (!(animal instanceof Herbivore)) {
            return false;
        }
        if (visibleEntities == null) {
            return false;
        }

        return TargetScanner.findNearest(animal, visibleEntities, 30, entity -> entity instanceof Carnivore) != null;
    }

    private boolean shouldAggressive(Animals animal, List<Entity> visibleEntities) {
        if (!(animal instanceof Carnivore)) {
            return false;
        }
        if (visibleEntities == null) {
            return false;
        }

        boolean hasPreyNearby = TargetScanner.findNearest(animal, visibleEntities, 50, entity -> {
            if (entity instanceof Animals other) {
                if (other == animal || !other.checkAlive()) return false;
                if (other instanceof Herbivore) return true;
                if (other instanceof Carnivore) {
                    if (other instanceof entities.Elephant) return false;
                    return other.getSize().ordinal() < animal.getSize().ordinal();
                }
            }
            return false;
        }) != null;

        return hasPreyNearby && animal.getHunger() < 90;
    }

    private boolean shouldMate(Animals animal, List<Entity> visibleEntities) {
        if (!animal.isReadyToMate()) return false;
        if (visibleEntities == null) return false;

        return TargetScanner.findNearest(animal, visibleEntities, 50, entity -> {
            if (entity instanceof Animals other) {
                return other.getClass() == animal.getClass() && other.isReadyToMate();
            }
            return false;
        }) != null;
    }
}