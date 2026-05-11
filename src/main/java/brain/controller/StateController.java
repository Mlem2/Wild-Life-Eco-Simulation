package brain.controller;

import AllEnum.State;
import Entities.Attributes.Carnivore;
import Entities.Attributes.Herbivore;
import Entities.Base.Animals;
import Entities.Base.Entity;
import Entities.Base.Tree;
import brain.scanner.TargetScanner;
import brain.strategy.AggressiveStrategy;
import brain.strategy.HunterStrategy;
import brain.strategy.MateStrategy;
import brain.strategy.MoveStrategy;
import brain.strategy.PassiveStrategy;
import brain.strategy.PriorityStrategy;
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

        if (shouldMate(animal, visibleEntities)) {
            setStrategy(animal, State.MATE, new MateStrategy());
            return;
        }

        if (shouldPrioritize(animal, visibleEntities)) {
            setStrategy(animal, State.PRIORITIZE, new PriorityStrategy());
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

        setStrategy(animal, State.PASSIVE, new PassiveStrategy());
    }
    // Set strategy dựa trên state mới. Core sẽ gọi hàm move() của strategy này trong vòng lặp chính để lấy hướng di chuyển.
    private void setStrategy(Animals animal, State newState, MoveStrategy strategy) {
        animal.setState(newState);
        animal.setMoveStrategy(strategy);
    }
    
    private boolean shouldPrioritize(Animals animal, List<Entity> visibleEntities) {
        if (!(animal instanceof Herbivore)) {
            return false;
        }
        if (visibleEntities == null) {
            return false;
        }

        return TargetScanner.findNearest(animal, visibleEntities, 50, entity -> entity instanceof Tree) != null
                && (animal.getHunger() < 70 || animal.getThirst() < 70);
    }

    // Logic để quyết định khi nào nên săn mồi hoặc bỏ chạy. Có thể mở rộng thêm các yếu tố như sức khỏe, tuổi tác, v.v.
    private boolean shouldHunt(Animals animal, List<Entity> visibleEntities) {
        if (!(animal instanceof Carnivore)) {
            return false;
        }
        if (visibleEntities == null) {
            return false;
        }

        return TargetScanner.findNearest(animal, visibleEntities, 40, entity -> entity instanceof Herbivore) != null
                && animal.getHunger() < 80;
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

        return TargetScanner.findNearest(animal, visibleEntities, 50, entity -> entity instanceof Herbivore) != null
                && animal.getHunger() < 90;
    }

    private boolean shouldMate(Animals animal, List<Entity> visibleEntities) {
        if (visibleEntities == null) {
            return false;
        }
        if (!animal.canMate()) {
            return false;
        }

        return TargetScanner.findNearest(animal, visibleEntities, 40, entity -> {
            if (!(entity instanceof Animals)) {
                return false;
            }
            Animals other = (Animals) entity;
            return other.getClass() == animal.getClass() && other != animal && other.canMate();
        }) != null;
    }
}