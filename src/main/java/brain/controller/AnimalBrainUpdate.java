package brain.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import brain.pathfinder.Pathfinder;
import entities.Food;
import entities.Water;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.Position;

public class AnimalBrainUpdate {
    private static final int CONSUME_RADIUS = 1;

    private final Animals owner;
    private final ChooseTarget targetSelector;
    private final Pathfinder pathFinder;
    private final ActionManager actionManager;
    private List<Position> currentPath = new ArrayList<>();
    private Position lastAnchorTarget = null;
    private String lastStrategyName = null;

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

        if (currentPath.isEmpty() || targetChanged || strategyChanged || owner.hasLockedTargetMoved()) {
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
            List<Point> raw = pathFinder.calculatePath(new Point(owner.getPosition().getX(), owner.getPosition().getY()), new Point(anchorTarget.getX(), anchorTarget.getY()));
            currentPath.clear();
            for (Point p : raw) currentPath.add(new Position(p.x, p.y));
            // If path includes current position as first element, drop it so the animal advances
            if (!currentPath.isEmpty() && currentPath.get(0).equals(owner.getPosition())) {
                currentPath.remove(0);
            }
        }

        if (!currentPath.isEmpty()) {
            Position nextStep = currentPath.remove(0);
            actionManager.move(nextStep); // Thực hiện di chuyển 1 ô và set Cooldown di chuyển
        }

        lastAnchorTarget = anchorTarget;
        lastStrategyName = currentStrategyName;
    }

    private Entity findConsumableAtCurrentTile() {
        MapSystem mapSystem = actionManager.getMapSystem();
        if (mapSystem == null) return null;

        List<Entity> currentTileEntities = mapSystem.getEntitiesWithinRadius(owner.getPosition(), 0);
        Entity foodTarget = null;
        Entity waterTarget = null;
        Entity preyTarget = null;

        for (Entity entity : currentTileEntities) {
            if (entity == null || entity == owner) continue;
            if (entity instanceof Food && !(entity instanceof Water)) {
                foodTarget = entity;
                break;
            }
            if (entity instanceof Water) {
                waterTarget = entity;
            }
            if (entity instanceof Animals && entity != owner) {
                preyTarget = entity;
            }
        }

        if (foodTarget != null) return foodTarget;
        if (waterTarget != null) return waterTarget;
        return preyTarget;
    }

    private boolean consumeNearbyTarget() {
        MapSystem mapSystem = actionManager.getMapSystem();
        if (mapSystem == null) return false;

        List<Entity> nearbyEntities = mapSystem.getEntitiesWithinRadius(owner.getPosition(), CONSUME_RADIUS);

        Entity foodTarget = findNearestTarget(nearbyEntities, entity -> entity instanceof Food && !(entity instanceof Water), false);
        if (foodTarget instanceof Food food) {
            actionManager.eat(food);
            return true;
        }

        Entity waterTarget = findNearestTarget(nearbyEntities, entity -> entity instanceof Water, false);
        if (waterTarget instanceof Water water) {
            actionManager.drink(water);
            return true;
        }

        List<Position> nearbyWaterTiles = mapSystem.getWaterPositionsWithinRadius(owner.getPosition(), CONSUME_RADIUS);
        nearbyWaterTiles.removeIf(pos -> owner.getPosition().equals(pos));
        if (!nearbyWaterTiles.isEmpty()) {
            actionManager.drink();
            return true;
        }

        if (owner instanceof entities.attributes.Carnivore) {
            Entity preyTarget = findNearestTarget(nearbyEntities, entity -> entity instanceof Animals other && other != owner && other instanceof entities.attributes.Herbivore, true);
            if (preyTarget instanceof Animals prey) {
                actionManager.eat(prey);
                return true;
            }
        }

        return false;
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

}