package brain.controller;

import allEnum.State;
import brain.strategy.AggressiveStrategy;
import brain.strategy.DrinkingStrategy;
import brain.strategy.EatingStrategy;
import brain.strategy.HunterStrategy;
import brain.strategy.MateStrategy;
import brain.strategy.MoveStrategy;
import brain.strategy.PassiveStrategy;
import brain.strategy.ScaredStrategy;
import entities.base.Animals;
import entities.base.Position;

public class ChooseTarget {
    private final Animals owner;
    private final MapSystem mapSystem;

    private final MoveStrategy scaredStrategy = new ScaredStrategy();
    private final MoveStrategy aggressiveStrategy = new AggressiveStrategy();
    private final MoveStrategy drinkingStrategy = new DrinkingStrategy();
    private final MoveStrategy eatingStrategy = new EatingStrategy();
    private final MoveStrategy passiveStrategy = new PassiveStrategy();
    private final MoveStrategy hunterStrategy = new HunterStrategy();
    private final MoveStrategy mateStrategy = new MateStrategy();

    private MoveStrategy currentStrategy;
    private Position currentTargetPos = null;
    private long targetSetTime = 0;

    public ChooseTarget(Animals owner, MapSystem mapSystem) {
        this.owner = owner;
        this.mapSystem = mapSystem;
        this.currentStrategy = passiveStrategy;
        this.owner.setState(State.PASSIVE);
    }

    // Được gọi bởi AnimalBrainUpdate mỗi tick để lấy vị trí mục tiêu hiện tại. Nếu cần thiết sẽ tự động cập nhật lại mục tiêu dựa trên chiến thuật hiện tại.
    public Position getOrUpdateTarget() {
        if (owner.getState() == allEnum.State.HIDING) {
            if (owner.getHungerPercentage() < 30) {
                owner.setState(State.EATING);
            } else if (owner.getThirstPercentage() < 30) {
                owner.setState(State.DRINKING);
            } else if (mapSystem.hasEnemyAround(owner)) {
                return owner.getPosition(); // Stay hiding
            } else {
                owner.setState(State.PASSIVE); // Safe now
            }
        }

        long currentTime = System.currentTimeMillis();

        evaluateStrategy();

        // Always call getTarget to ensure speedUp state is updated by the strategy if it depends on the strategy being active
        // However, we only WANT to change the target position if needed.
        // 10 giây không cập nhật lại mục tiêu sẽ ép tính toán lại
        long TARGET_TIMEOUT_MS = 10000;
        if (currentTargetPos == null ||
            owner.getPosition().equals(currentTargetPos) ||
            (currentTime - targetSetTime > TARGET_TIMEOUT_MS)) {

            currentTargetPos = currentStrategy.getTarget(owner, mapSystem);
            targetSetTime = currentTime;
        } else {
            // Even if we don't update the target position, we MUST ensure the strategy-specific side effects (like speedUp) are applied
            // Since some strategies set speedUp(true) inside getTarget, we call it even if we ignore the returned position.
            currentStrategy.getTarget(owner, mapSystem); 
        }

        return currentTargetPos;
    }

// Dành cho AnimalBrainUpdate gọi để lấy thông tin hiển thị/debug
    public String getCurrentStrategyName() {
        if (currentStrategy == null) return "None";
        return currentStrategy.getClass().getSimpleName();
    }

    public Position getCurrentTargetPosition() {
        return currentTargetPos;
    }

    private void evaluateStrategy() {
        // 1. Kiểm tra trạng thái thèm ăn/thèm uống
        if ((owner.getHungerPercentage() < 50 || owner.getThirstPercentage() < 50) && owner instanceof entities.attributes.Herbivore) {
            changeStrategy(aggressiveStrategy);
            return;
        }

        // 2. Kiểm tra thiên địch xung quanh toàn bộ tầm nhìn (3x3 chunks)
        // ScaredStrategy has the highest priority for herbivores
        if (!(owner instanceof entities.attributes.Apex) && !(owner instanceof entities.attributes.Aquatic) &&
            mapSystem.hasEnemyAround(owner)
        ) {
            // Ngẫu nhiên có phát hiện kẻ địch hay không (80%)
            if (getCurrentStrategyName().equals("ScaredStrategy")) {
                return;
            } else if (Math.random() < 0.2) {
                changeStrategy(scaredStrategy);
            }
            return;
        }

        // 3. Mating strategy
        if (owner.isReadyToMate() && mapSystem.hasMateAround(owner)) {
            changeStrategy(mateStrategy);
            return;
        }

        // 4. Nếu đói hoặc khát nhưng không có mục tiêu săn đuổi ngay lập tức -> Dùng Priority để mò đồ ăn/nước
        if (owner.getThirstPercentage() < 50) {
            changeStrategy(drinkingStrategy);
            return;
        }

        if ((owner instanceof entities.attributes.Carnivore)
                && owner.getHungerPercentage() < 60
                && mapSystem.hasPreyAround(owner)) {
            changeStrategy(hunterStrategy);
            return;
        }

        if (owner.getHungerPercentage() < 60 && owner instanceof entities.attributes.Herbivore) {
            changeStrategy(eatingStrategy);
            return;
        }

        // 5. Mọi thứ ổn định -> Thư giãn
        changeStrategy(passiveStrategy);
    }

    private void changeStrategy(MoveStrategy newStrategy) {
        if (this.currentStrategy != newStrategy) {
            this.currentStrategy = newStrategy;
            owner.setSpeedUp(false);      // Reset trạng thái speed về mặc định trước khi strategy mới tính toán
            
            if (newStrategy == scaredStrategy) owner.setState(allEnum.State.SCARED);
            else if (newStrategy == aggressiveStrategy) owner.setState(allEnum.State.AGGRESSIVE);
            else if (newStrategy == drinkingStrategy) owner.setState(allEnum.State.DRINKING);
            else if (newStrategy == eatingStrategy) owner.setState(allEnum.State.EATING);
            else if (newStrategy == hunterStrategy) owner.setState(allEnum.State.HUNT);
            else if (newStrategy == mateStrategy) owner.setState(allEnum.State.MATE);
            else if (newStrategy == passiveStrategy) owner.setState(allEnum.State.PASSIVE);

            this.currentTargetPos = newStrategy.getTarget(owner, mapSystem); 
            targetSetTime = System.currentTimeMillis();
        }
    }
}