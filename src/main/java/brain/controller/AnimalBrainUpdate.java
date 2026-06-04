package brain.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import brain.pathfinder.Pathfinder;
import entities.Bush;
import entities.Food;
import entities.Trees;
import entities.Water;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.EntityFactory;
import entities.base.Position;
public class AnimalBrainUpdate {
    private static final int CONSUME_RADIUS = 1;
    private static final double MIN_THIRST_TO_DRINK = 60.0; // Chỉ uống nước khi độ khát < 60
    private static final int MIN_TICKS_BETWEEN_DRINKS = 5; // Cooldown 5 ticks giữa các lần uống

    private final Animals owner;
    private final ChooseTarget targetSelector;
    private final Pathfinder pathFinder;
    private final ActionManager actionManager;
    private List<Position> currentPath = new ArrayList<>();
    private List<Point> rawPath = new ArrayList<>();
    private Position lastAnchorTarget = null;
    private String lastStrategyName = null;
    private int lastDrinkTick = Integer.MIN_VALUE; // Theo dõi tick cuối cùng uống nước
    private int updateTickCounter = 0; // Đếm số lần update() được gọi

    public AnimalBrainUpdate(Animals owner, ChooseTarget targetSelector, Pathfinder pathFinder, ActionManager actionManager) {
        this.owner = owner;
        this.targetSelector = targetSelector;
        this.pathFinder = pathFinder;
        this.actionManager = actionManager;
    }

    public List<Position> getCurrentPath() {
        return new ArrayList<>(currentPath);
    }

    public Position getCurrentAnchorTarget() {
        return targetSelector.getCurrentTargetPosition();
    }

    public String getCurrentStrategyName() {
        return targetSelector.getCurrentStrategyName();
    }

    public void update() {
        // Tăng tick counter mỗi lần update
        updateTickCounter++;

        // 1. Ưu tiên hành động ăn/đánh ngay lập tức nếu có mục tiêu gần, kể cả khi đang trong cooldown di chuyển.
        if (consumeNearbyTarget()) {
            currentPath.clear();
            return;
        }

        // 2. Kiểm tra Cooldown di chuyển/hành động từ ActionManager
        if (!actionManager.isAvailable()) return;

        // 2. Hỏi bộ não ChooseTarget xem mục tiêu (điểm neo) hiện tại là ở đâu
        Position anchorTarget = targetSelector.getOrUpdateTarget();
        String currentStrategyName = targetSelector.getCurrentStrategyName();

        boolean targetChanged = !Objects.equals(lastAnchorTarget, anchorTarget);
        boolean strategyChanged = !Objects.equals(lastStrategyName, currentStrategyName);

        if (anchorTarget == null) {
            currentPath.clear();
            lastAnchorTarget = null;
            lastStrategyName = currentStrategyName;
            return;
        }

        if (targetChanged || strategyChanged || owner.hasLockedTargetMoved()) {
            currentPath.clear();
        }

        // 3. Nếu đã đứng ngay cạnh mục tiêu lock-in (Thức ăn, Nước, Con mồi) -> Thực hiện Hành động
        if (owner.getPosition().equals(anchorTarget)) {
            Object targetEntity = owner.getLockedTargetEntity();
            if (targetEntity instanceof Position positionTarget) {
                Entity entityAtTarget = actionManager.getMapSystem().getEntityAt(positionTarget);
                if (entityAtTarget == owner) {
                    targetEntity = findConsumableAtCurrentTile();
                } else {
                    targetEntity = entityAtTarget;
                }
            }
            if (targetEntity == null) {
                targetEntity = findConsumableAtCurrentTile();
            }
            if (targetEntity instanceof Food food) {
                actionManager.eat(food);
            } else if (targetEntity instanceof Water) {
                actionManager.drink((Water) targetEntity);
            } else if (targetEntity instanceof Animals) {
                actionManager.eat((Animals) targetEntity); // Ăn mồi ngay lập tức, không dùng cơ chế HP/attack
            }
            currentPath.clear(); // Xóa đường đi cũ sau khi đã hành động xong
            return;
        }

        // 4. Nếu chưa đến đích -> Cập nhật đường đi và bắt ActionManager di chuyển
        // Nếu đường đi trống hoặc thực thể đích di chuyển (đối với con mồi chạy trốn)
        if (currentPath.isEmpty() || owner.hasLockedTargetMoved()) {
            pathFinder.calculatePath(new Point(owner.getPosition().getX(), owner.getPosition().getY()), new Point(anchorTarget.getX(), anchorTarget.getY()), rawPath);
            currentPath.clear();
            for (Point p : rawPath) currentPath.add(Position.of(p.x, p.y));
            // If path includes current position as first element, drop it so the animal advances
            if (!currentPath.isEmpty() && currentPath.get(0).equals(owner.getPosition())) {
                currentPath.remove(0);
            }
        }

        if (!currentPath.isEmpty()) {
            Position nextStep = currentPath.remove(0);
            // Thực hiện di chuyển 1 ô và set Cooldown di chuyển
            boolean moveSuccess = actionManager.move(nextStep);
            
            // Nếu di chuyển thất bại (terrain không phù hợp, v.v.), hãy xóa path và lấy mục tiêu mới
            if (!moveSuccess) {
                currentPath.clear();
                lastAnchorTarget = null; // Force getting a new target on next tick
            }
        }

        lastAnchorTarget = anchorTarget;
        lastStrategyName = currentStrategyName;
    }

    private Entity findConsumableAtCurrentTile() {
        MapSystem mapSystem = actionManager.getMapSystem();
        if (mapSystem == null) return null;

        List<Entity> currentTileEntities = mapSystem.getEntitiesWithinRadius(owner.getPosition(), 0);
        Entity waterTarget = null;
        Entity preyTarget = null;

        for (Entity entity : currentTileEntities) {
            if (entity == null || entity == owner) continue;
            
            // Carnivores: only eat animals (herbivores), never Bush/Trees
            if (owner instanceof entities.attributes.Carnivore) {
                if (entity instanceof Animals other && other != owner && other instanceof entities.attributes.Herbivore && !(other instanceof entities.attributes.Apex)) {
                    preyTarget = entity;
                }
            }
            
            // Herbivores: don't eat Bush/Trees (they eat grass from terrain instead)
            if (owner instanceof entities.attributes.Herbivore && owner instanceof entities.attributes.Apex == false) {
                if (entity instanceof Bush || entity instanceof Trees) {
                    continue; // Skip Bush/Trees, herbivores eat grass from terrain. Elephants tho, can eat Bush/Trees.
                }
            }

            // Elephants can eat Bush/Trees
                if (owner instanceof entities.attributes.Apex && owner instanceof entities.attributes.Carnivore && (entity instanceof Bush || entity instanceof Trees)) {
                    preyTarget = entity;
                }
            
            // Water can be consumed by any animal
            if (entity instanceof Water) {
                waterTarget = entity;
            }
        }

        // Carnivores prioritize prey
        if (owner instanceof entities.attributes.Carnivore && preyTarget != null) {
            return preyTarget;
        }
        
        // Water available for any animal
        if (waterTarget != null) return waterTarget;
        
        // Return prey target if found (for carnivores)
        return preyTarget;
    }

    private boolean consumeNearbyTarget() {
        MapSystem mapSystem = actionManager.getMapSystem();
        if (mapSystem == null) return false;

        List<Entity> nearbyEntities = mapSystem.getEntitiesWithinRadius(owner.getPosition(), CONSUME_RADIUS);

        // For aquatic herbivores: consume water to recover both hunger and thirst
        if (owner instanceof entities.attributes.AquaticHerbivore) {
            Entity waterTarget = findNearestTarget(nearbyEntities, entity -> entity instanceof Water, false);
            if (waterTarget instanceof Water water) {
                // Aquatic herbivores use water as both food and drink
                // Recover hunger when consuming water
                double currentThirst = owner.getThirst();
                double currentHunger = owner.getHunger();
                boolean needsConsumption = currentThirst < MIN_THIRST_TO_DRINK || currentHunger < MIN_THIRST_TO_DRINK;
                boolean enoughTimePassed = (updateTickCounter - lastDrinkTick) >= MIN_TICKS_BETWEEN_DRINKS;
                
                if (needsConsumption && enoughTimePassed) {
                    // Drink water to recover thirst
                    int thirstGain = owner.getThirstRecoveryAmount();
                    water.consume(thirstGain);
                    owner.increaseHydration(thirstGain);
                    // Also recover hunger as if eating food (aquatic herbivores get nutrition from water)
                    int hungerGain = owner.getHungerRecoveryAmount();
                    owner.increaseHunger(hungerGain);
                    owner.setCurrentMoveCooldown(5); // Consuming water also takes cooldown
                    lastDrinkTick = updateTickCounter;
                    return true;
                }
            }
        }

        // For regular herbivores: exclude Bush/Trees, only eat actual Food entities (not terrain-based food)
        if (owner instanceof entities.attributes.Herbivore && !(owner instanceof entities.attributes.Apex)) {
            Entity foodTarget = findNearestTarget(nearbyEntities, 
                entity -> entity instanceof Food 
                    && !(entity instanceof Water) 
                    && !(entity instanceof Bush) 
                    && !(entity instanceof Trees), 
                false);
            if (foodTarget instanceof Food food) {
                actionManager.eat(food);
                return true;
            }
        }

        // For Apex herbivores (Elephants): can eat Bush/Trees as food
        if (owner instanceof entities.attributes.Apex && owner instanceof entities.attributes.Herbivore) {
            Entity foodTarget = findNearestTarget(nearbyEntities, 
                entity -> entity instanceof Food 
                    && !(entity instanceof Water), 
                false);
            if (foodTarget instanceof Food food) {
                actionManager.eat(food);
                return true;
            }
        }

        Entity waterTarget = findNearestTarget(nearbyEntities, entity -> entity instanceof Water, false);
        if (waterTarget instanceof Water water) {
            // Chỉ uống nước nếu độ khát cao và cooldown đã xong (only for non-aquatic herbivores)
            if (!(owner instanceof entities.attributes.AquaticHerbivore) && shouldDrink()) {
                actionManager.drink(water);
                lastDrinkTick = updateTickCounter;
                return true;
            }
        }

        List<Position> nearbyWaterTiles = mapSystem.getWaterPositionsWithinRadius(owner.getPosition(), CONSUME_RADIUS);
        nearbyWaterTiles.removeIf(pos -> owner.getPosition().equals(pos));
        if (!nearbyWaterTiles.isEmpty()) {
            // Chỉ uống nước nếu độ khát cao và cooldown đã xong
            if (shouldDrink()) {
                actionManager.drink();
                lastDrinkTick = updateTickCounter;
                return true;
            }
        }

        // Carnivores: only eat herbivore animals, never Bush/Trees
        if (owner instanceof entities.attributes.Carnivore) {
            Entity preyTarget = findNearestTarget(nearbyEntities, 
                entity -> entity instanceof Animals other 
                    && other != owner 
                    && other instanceof entities.attributes.Herbivore
                    && !(other instanceof entities.attributes.Apex), 
                true);
            if (preyTarget instanceof Animals prey) {
                actionManager.eat(prey);
                return true;
            }
        }

        return false;
    }

    /**
     * Kiểm tra xem con vật có nên uống nước hay không.
     * Chỉ uống khi:
     * 1. Độ khát thấp hơn ngưỡng MIN_THIRST_TO_DRINK (60)
     * 2. Đã đủ thời gian tối thiểu giữa các lần uống (MIN_TICKS_BETWEEN_DRINKS)
     */
    private boolean shouldDrink() {
        double currentThirst = owner.getThirst();
        boolean thirstLowEnough = currentThirst < MIN_THIRST_TO_DRINK;
        boolean enoughTimePassed = (updateTickCounter - lastDrinkTick) >= MIN_TICKS_BETWEEN_DRINKS;
        
        return thirstLowEnough && enoughTimePassed;
    }

    private Entity findNearestTarget(List<Entity> entities, java.util.function.Predicate<Entity> predicate, boolean allowCurrentTile) {
        Entity nearest = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Entity entity : entities) {
            if (entity == null || !predicate.test(entity)) continue;

            int distance = Math.abs(owner.getX() - entity.getX()) + Math.abs(owner.getY() - entity.getY());
            if (!allowCurrentTile && distance == 0) continue;
            if (distance <= CONSUME_RADIUS && distance < bestDistance) {
                nearest = entity;
                bestDistance = distance;
            }
        }

        return nearest;
    }

    // Update sau khi Mating
    public boolean postMatingUpdate() {
        // Sau khi giao phối xong, sẽ có logic sinh con ở AnimalBrainUpdate dựa trên việc đã giao phối thành công hay chưa (có thể dựa vào một cờ hiệu hoặc kiểm tra xem bạn tình còn tồn tại không)
        // Nếu giao phối thành công, sẽ có con non xuất hiện ở vị trí gần bố mẹ nhất có thể (có thể là 1 trong 8 ô xung quanh nếu trống)
        // Sau khi sinh con, sẽ có cooldown cho cả bố và mẹ để nghỉ ngơi trước khi có thể thực hiện hành động khác (gọi ActionManager mating())
        if (owner.isMatable() && owner.getLockedTargetEntity() instanceof Animals partner && partner.isMatable()) {
            // Logic sinh con
            Position birthPosition = actionManager.getMapSystem().findNearbyFreePosition(owner.getPosition(), 1);
            if (birthPosition != null) {
                // Tạo con non mới (có thể dựa trên lớp của bố hoặc mẹ, hoặc một lớp con chung)
                Animals baby = EntityFactory.CreateEntity(
                    (EntityFactory.FakeConstructor<Animals, Integer, Integer>) (x, y) -> {
                        try {
                            return owner.getClass().getConstructor(int.class, int.class).newInstance(x, y);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }, 
                    birthPosition.getX(), 
                    birthPosition.getY()
                );
                if (baby != null) {
                    actionManager.getMapSystem().addEntity(baby);
                    // Set cooldown cho bố mẹ sau khi sinh con
                    actionManager.mating(owner);
                    actionManager.mating(partner);
                    return true;
                }
            }
        }
        return false;
    }
}