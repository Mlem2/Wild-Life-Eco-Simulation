package brain.pathfinder;

import allEnum.Direction;
import core.enviroment.WorldMap;
import java.awt.Point;
import java.util.*;

public class Pathfinder {
    private static final int SIZE = 500;
    private final WorldMap worldMap;
    int[][] visitedWithRunId = new int[SIZE][SIZE];
    Node[][] nodeMap = new Node[SIZE][SIZE];
    int currentRunID = 0;

    // Precomputed movement costs for cleaner code
    private static final double CARDINAL_COST = 1.0;
    private static final double DIAGONAL_COST = 1.4142;

    public Pathfinder(WorldMap worldMap) {
        this.worldMap = worldMap; // No heavy graph allocation at all!
    }

    private static class Node implements Comparable<Node> {
        int x, y;
        double gCost; // Cost from start
        double hCost; // Heuristic cost to end
        double fCost; // Total cost (g + h)
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.fCost, o.fCost);
        }
    }

    public List<Point> calculatePath(Point start, Point end) {
        if (start == null || end == null) return Collections.emptyList();

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        // Tracking states via plain primitive arrays instead of complex Map lookups
        currentRunID++;

        Node startNode = new Node(start.x, start.y);
        startNode.gCost = 0;
        startNode.hCost = getHeuristic(start.x, start.y, end.x, end.y);
        startNode.fCost = startNode.hCost;

        openSet.add(startNode);
        nodeMap[start.y][start.x] = startNode;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == end.x && current.y == end.y) {
                return retracePath(current);
            }

            visitedWithRunId[current.y][current.x] = currentRunID;

            for (Direction dir : Direction.values()) {
                if (dir == Direction.CENTER) continue;

                int nextX = current.x + dir.x;
                int nextY = current.y + dir.y;

                // Boundary & Passable Check
                if (nextX < 0 || nextX >= SIZE || nextY < 0 || nextY >= SIZE) continue;
                if (!worldMap.getTile(nextX, nextY).isPassable()) continue;
                if (visitedWithRunId[nextY][nextX] == currentRunID) continue;

                double moveCost = (dir.x != 0 && dir.y != 0) ? DIAGONAL_COST : CARDINAL_COST;
                double edgeWeight = moveCost / worldMap.getTile(nextX, nextY).getSpeedMultiplier();
                double tentativeGCost = current.gCost + edgeWeight;

                Node neighbor = nodeMap[nextY][nextX];
                if (neighbor == null) {
                    neighbor = new Node(nextX, nextY);
                    nodeMap[nextY][nextX] = neighbor;
                }

                if (tentativeGCost < neighbor.gCost || !openSet.contains(neighbor)) {
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = getHeuristic(nextX, nextY, end.x, end.y);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    neighbor.parent = current;

                    // Force priority queue update
                    if (openSet.contains(neighbor)) {
                        openSet.remove(neighbor);
                    }
                    openSet.add(neighbor);
                }
            }
        }
        return Collections.emptyList(); // No path found
    }

    private double getHeuristic(int x, int y, int targetX, int targetY) {
        // Diagonal distance heuristic matching standard 8-way grid movement
        int dx = Math.abs(x - targetX);
        int dy = Math.abs(y - targetY);
        return (dx > dy) ? (DIAGONAL_COST * dy + CARDINAL_COST * (dx - dy))
                : (DIAGONAL_COST * dx + CARDINAL_COST * (dy - dx));
    }

    private List<Point> retracePath(Node endNode) {
        List<Point> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            path.add(new Point(current.x, current.y));
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}