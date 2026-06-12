package entities;

import entities.base.Entity;

public class Rock extends Entity {
    public Rock(int x, int y) {
        super(x, y);
    }

    @Override
    public Boolean checkAlive() {
        return true; // Đá luôn luôn tồn tại, không bị chết
    }
}