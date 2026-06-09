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
    private final Animals owner;
    private final ChooseTarget targetSelector;
    private final Pathfinder pathFinder;
    private final ActionManager actionManager;
    private List<Position> currentPath = new ArrayList<>();
    private List<Point> rawPath = new ArrayList<>();
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

        // 1. Kiểm tra Cooldown di chuyển/hành động từ ActionManager
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
            // Check if small animal reached a bush while being scared and if it's not occupied
            if (owner.getSize() == allEnum.Size.SMALL && "ScaredStrategy".equals(currentStrategyName)) {
                List<Entity> entitiesAtPos = actionManager.getMapSystem().getEntitiesWithinRadius(owner.getPosition(), 0);
                boolean inBush = false;
                for (Entity e : entitiesAtPos) {
                    if (e instanceof entities.Bush) {
                        inBush = true;
                        break;
                    }
                }
                if (inBush && !actionManager.getMapSystem().isBushOccupied(owner.getPosition())) {
                    owner.setState(allEnum.State.HIDING);
                    currentPath.clear();
                    return;
                }
            }

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
            } else if (targetEntity instanceof Animals other) {
                if (actionManager.getMapSystem().isPotentialMate(owner, other)) {
                    actionManager.mate(other);
                } else {
                    actionManager.attack(other); // CẬP NHẬT: Sử dụng attack() thay vì eat() để có tỉ lệ thành công
                }
            } else {
                // Check if it's grass terrain
                core.enviroment.Terrain terrain = actionManager.getMapSystem().getTerrainAt(owner.getPosition());
                if (terrain != null && terrain.isGrass() && owner instanceof entities.attributes.Herbivore) {
                    actionManager.eatGrass();
                }
            }
            currentPath.clear(); // Xóa đường đi cũ sau khi đã hành động xong
            return;
        }

        // 4. Nếu chưa đến đích -> Cập nhật đường đi và bắt ActionManager di chuyển
        // Nếu đường đi trống hoặc thực thể đích di chuyển (đối với con mồi chạy trốn)
        if (currentPath.isEmpty() || owner.hasLockedTargetMoved()) {
            pathFinder.calculatePath(new Point(owner.getPosition().getX(), owner.getPosition().getY()), new Point(anchorTarget.getX(), anchorTarget.getY()), rawPath, owner);
            currentPath.clear();
            for (Point p : rawPath) currentPath.add(Position.of(p.x, p.y));
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
        Entity mateTarget = null;

        for (Entity entity : currentTileEntities) {
            if (entity == null || entity == owner) continue;
            if (entity instanceof Food && !(entity instanceof Water)) {
                // Only elephants eat trees and bushes
                if (entity instanceof entities.Trees || entity instanceof entities.Bush) {
                    if (owner instanceof entities.Elephant) {
                        foodTarget = entity;
                        break;
                    }
                    continue;
                }
                foodTarget = entity;
                break;
            }
            if (entity instanceof Water) {
                waterTarget = entity;
            }
            if (entity instanceof Animals && entity != owner) {
                if (mapSystem.isPotentialMate(owner, (Animals) entity)) {
                    mateTarget = entity;
                } else if (mapSystem.isPrey(owner, (Animals) entity)) {
                    preyTarget = entity;
                }
            }
        }

        if (mateTarget != null) return mateTarget;
        if (foodTarget != null) return foodTarget;
        if (waterTarget != null) return waterTarget;
        return preyTarget;
    }

}