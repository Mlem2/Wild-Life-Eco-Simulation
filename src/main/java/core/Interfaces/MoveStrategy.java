package Interfaces;

import Entities.Base.Entity;

import java.util.List;

public interface MoveStrategy {
    void move(Entity[][] toaDoSV, List<Entity> SV);
}
