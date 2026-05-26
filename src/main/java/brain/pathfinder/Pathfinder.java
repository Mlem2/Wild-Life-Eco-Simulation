package brain.pathfinder;

import allEnum.Direction;
import core.enviroment.WorldMap;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.awt.*;
import java.util.List;

public class Pathfinder {
    private static final int SIZE = 500;
    Graph<Point, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    AStarShortestPath<Point, DefaultWeightedEdge> aStar;
    Point[][] tiles = new Point[SIZE][SIZE];

    public Pathfinder(WorldMap worldMap) {
        addWalkableVertices(worldMap);
        connectVertices(worldMap);

        double minSpeedMultiplier = 0.2;
        aStar = new AStarShortestPath<>(
                graph,
                (node, target) -> minSpeedMultiplier * Math.max(Math.abs(node.x - target.x), Math.abs(node.y - target.y))
        );
    }

    public List<Point> calculatePath(Point start, Point end) {
        if (start == null || end == null) {
            return java.util.Collections.emptyList();
        }

        org.jgrapht.GraphPath<Point, DefaultWeightedEdge> path = aStar.getPath(start, end);

        if (path == null) {
            return java.util.Collections.emptyList();
        }

        return path.getVertexList();
    }

    private void addWalkableVertices(WorldMap worldMap) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (worldMap.getTile(x, y).isPassable()) {
                    tiles[y][x] = new Point(x, y);
                    graph.addVertex(tiles[y][x]);
                }
            }
        }
    }

    private void connectVertices(WorldMap worldMap) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (!worldMap.getTile(x, y).isPassable()) continue;

                Point current = tiles[y][x];
                for (Direction direction : Direction.values()) {
                    if (direction == Direction.CENTER) continue;

                    int newX = x + direction.x;
                    int newY = y + direction.y;

                    if (!(newX > SIZE - 1 || newY > SIZE - 1 || newX < 0 || newY < 0)) {
                        Point neighbor = tiles[newY][newX];

                        if (neighbor != null && !graph.containsEdge(current, neighbor)) {
                            DefaultWeightedEdge edge = graph.addEdge(current, neighbor);

                            if (edge != null) {
                                double weight = (direction.x != 0 && direction.y != 0) ? 1.4 : 1.0;
                                weight = weight / worldMap.getTile(newX, newY).getSpeedMultiplier();

                                graph.setEdgeWeight(edge, weight);
                            }
                        }
                    }
                }
            }
        }
    }
}
