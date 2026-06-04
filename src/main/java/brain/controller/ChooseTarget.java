package brain.controller;

import brain.strategy.AggressiveStrategy;
import brain.strategy.HunterStrategy;
import brain.strategy.MoveStrategy;
import brain.strategy.PassiveStrategy;
import brain.strategy.PriorityStrategy;
import brain.strategy.ScaredStrategy;
import entities.base.Animals;
import entities.base.Position;

public class ChooseTarget {
    private final Animals owner;
    private final MapSystem mapSystem;

    private final MoveStrategy scaredStrategy = new ScaredStrategy();
    private final MoveStrategy aggressiveStrategy = new AggressiveStrategy();
    private final MoveStrategy priorityStrategy = new PriorityStrategy();
    private final MoveStrategy passiveStrategy = new PassiveStrategy();
    private final MoveStrategy hunterStrategy = new HunterStrategy();

    private MoveStrategy currentStrategy;
    private Position currentTargetPos = null;
    private long targetSetTime = 0;
    private final long TARGET_TIMEOUT_MS = 15000; // 15 giây không cập nhật lại mục tiêu sẽ ép tính toán lại
    
    public ChooseTarget(Animals owner, MapSystem mapSystem) {
        this.owner = owner;
        this.mapSystem = mapSystem;
        this.currentStrategy = passiveStrategy;
    }

    // Được gọi bởi AnimalBrainUpdate mỗi tick để lấy vị trí mục tiêu hiện tại. Nếu cần thiết sẽ tự động cập nhật lại mục tiêu dựa trên chiến thuật hiện tại.
    public Position getOrUpdateTarget() {
        long currentTime = System.currentTimeMillis();

        evaluateStrategy();

        if (currentTargetPos == null ||
            owner.getPosition().equals(currentTargetPos) ||
            (currentTime - targetSetTime > TARGET_TIMEOUT_MS)) {

            currentTargetPos = currentStrategy.getTarget(owner, mapSystem);
            targetSetTime = currentTime;
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
        // 1. Kiểm tra thiên địch xung quanh toàn bộ tầm nhìn (3x3 chunks)
        if ((owner instanceof entities.attributes.Herbivore
            && !(owner instanceof entities.attributes.Apex)) &&
            mapSystem.hasEnemyAround(owner)) {
            // Ngẫu nhiên có phát hiện kẻ địch hay không (80%)
            int chance = (int)(Math.random() * 100);
            if (mapSystem.hasEnemyNearby(owner) == true) {
                chance = 100; // Nếu kẻ địch quá gần thì chắc chắn sẽ sợ hãi
            }
            if (chance >= 75) {
                changeStrategy(scaredStrategy);  
            }
            else changeStrategy(passiveStrategy);
            return;
        }

        // 2. Nếu an toàn và là thú săn mồi đang đói + thấy con mồi -> Kích hoạt Hunter/Aggressive
        if ((owner instanceof entities.attributes.Carnivore)
            && owner.getHungerPercentage() < 70
            && mapSystem.hasPreyNearby(owner)) {
            changeStrategy(aggressiveStrategy); 
            return;
        }
        
        if ((owner instanceof entities.attributes.Carnivore)
                && owner.getHungerPercentage() < 70
                && mapSystem.hasPreyAround(owner)) {
            changeStrategy(hunterStrategy);
            return;
        }
        // 2. Nếu đói hoặc khát nhưng không có mục tiêu săn đuổi ngay lập tức -> Dùng Priority để mò đồ ăn/nước
        if (owner instanceof entities.attributes.Carnivore && owner.getThirstPercentage() < 50 && owner.getHungerPercentage() > owner.getThirstPercentage()) {
            changeStrategy(priorityStrategy);
            return;
        }

        if ((owner.getHungerPercentage() < 70 || owner.getThirstPercentage() < 70) && !(owner instanceof entities.attributes.Carnivore)) {
            changeStrategy(priorityStrategy);
            return;
        }

        // 4. Mọi thứ ổn định -> Thư giãn
        changeStrategy(passiveStrategy);
    }

    private void changeStrategy(MoveStrategy newStrategy) {
        if (this.currentStrategy != newStrategy) {
            this.currentStrategy = newStrategy;
            this.currentTargetPos = null; // Ép tính toán lại mục tiêu theo chiến thuật mới ngay lập tức
            owner.setSpeedUp(false);      // Reset trạng thái speed về mặc định trước khi strategy mới tính toán
        }
    }
}