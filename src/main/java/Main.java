import core.enviroment.ChunkMap;
import core.enviroment.WorldMap;
import entities.base.EntityMap;

private static final int SEED = 9403312;
private static final int SIZE = 500;

void main() {
    WorldMap worldMap = new WorldMap(SEED, SIZE);
    EntityMap entityMap = new EntityMap(worldMap, SIZE);
    ChunkMap chunkMap = new ChunkMap(worldMap, entityMap, SIZE);
}
