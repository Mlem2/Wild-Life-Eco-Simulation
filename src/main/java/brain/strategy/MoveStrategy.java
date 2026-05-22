package brain.strategy;

import entities.base.Animals;
import entities.base.Entity;
import allEnum.Direction;
import java.util.List;

/*
 * Interface Strategy Pattern
 * Mỗi loài sẽ có cách di chuyển khác nhau.
 * Các strategy sẽ implement interface này.
 * Ví dụ:
 * - PassiveStrategy → đi lang thang
 * - HunterStrategy → săn mồi
 * - ScaredStrategy → bỏ chạy
 */
public interface MoveStrategy {

    /*
     * Hàm xử lý logic di chuyển cho animal.
     * @param animal Con vật đang sử dụng strategy này
     * @param visibleEntities Danh sách thực thể xung quanh hoặc toàn bộ entity để scan
     * @return Hướng di chuyển
     */
    Direction move(Animals animal, List<Entity> visibleEntities);
}