package brain.strategy;

import entities.base.Animal;

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
     */
    void move(Animal animal);
}