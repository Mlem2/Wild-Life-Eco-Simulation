package brain.pathfinder;

import allEnum.Direction;
import core.enviroment.WorldMap;
import java.awt.Point;
import java.util.*;

public class Pathfinder {
    private final WorldMap worldMap;
    private final int size;
    private final int[][] visitedWithRunId;
    private final Node[][] nodeMap;
    private int currentRunID = 0;

    private static final double CARDINAL_COST = 1.0;
    private static final double DIAGONAL_COST = 1.4142;

    // Cache directions to avoid .values() array allocation
    private static final Direction[] DIRECTIONS = Arrays.stream(Direction.values())
            .filter(d -> d != Direction.CENTER)
            .toArray(Direction[]::new);

    public Pathfinder(WorldMap worldMap) {
        this.worldMap = worldMap;
        this.size = WorldMap.SIZE;
        this.visitedWithRunId = new int[size][size];
        this.nodeMap = new Node[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                nodeMap[y][x] = new Node(x, y);
            }
        }
    }

    private static class Node {
        final int x, y;
        double gCost;
        double hCost;
        double fCost;
        Node parent;
        int runId = -1;
        int heapIndex = -1;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void reset(int runId) {
            this.gCost = Double.MAX_VALUE;
            this.hCost = 0;
            this.fCost = Double.MAX_VALUE;
            this.parent = null;
            this.runId = runId;
            this.heapIndex = -1;
        }
    }

    /**
     * Optimized Binary Heap for Node objects.
     */
    private static class FastNodeHeap {
        private Node[] heap;
        private int size = 0;

        FastNodeHeap(int capacity) {
            heap = new Node[capacity];
        }

        void clear() {
            for (int i = 0; i < size; i++) {
                heap[i].heapIndex = -1;
            }
            size = 0;
        }

        boolean isEmpty() {
            return size == 0;
        }

        void add(Node node) {
            node.heapIndex = size;
            heap[size] = node;
            size++;
            bubbleUp(node.heapIndex);
        }

        Node poll() {
            if (size == 0) return null;
            Node root = heap[0];
            size--;
            if (size > 0) {
                heap[0] = heap[size];
                heap[0].heapIndex = 0;
                bubbleDown(0);
            }
            root.heapIndex = -1;
            return root;
        }

        void update(Node node) {
            if (node.heapIndex != -1) {
                bubbleUp(node.heapIndex);
            } else {
                add(node);
            }
        }

        private void bubbleUp(int index) {
            Node node = heap[index];
            while (index > 0) {
                int parentIndex = (index - 1) >> 1;
                Node parent = heap[parentIndex];
                if (node.fCost < parent.fCost || (node.fCost == parent.fCost && node.hCost < parent.hCost)) {
                    heap[index] = parent;
                    parent.heapIndex = index;
                    index = parentIndex;
                } else {
                    break;
                }
            }
            heap[index] = node;
            node.heapIndex = index;
        }

        private void bubbleDown(int index) {
            Node node = heap[index];
            int half = size >> 1;
            while (index < half) {
                int left = (index << 1) + 1;
                int right = left + 1;
                int best = left;
                if (right < size) {
                    if (heap[right].fCost < heap[left].fCost || (heap[right].fCost == heap[left].fCost && heap[right].hCost < heap[left].hCost)) {
                        best = right;
                    }
                }
                if (heap[best].fCost < node.fCost || (heap[best].fCost == node.fCost && heap[best].hCost < node.hCost)) {
                    heap[index] = heap[best];
                    heap[index].heapIndex = index;
                    index = best;
                } else {
                    break;
                }
            }
            heap[index] = node;
            node.heapIndex = index;
        }
    }

    private final FastNodeHeap openSet = new FastNodeHeap(WorldMap.SIZE * WorldMap.SIZE);

    public List<Point> calculatePath(Point start, Point end, List<Point> path) {
        if (path == null) {
            path = new ArrayList<>();
        }
        path.clear();
        if (start == null || end == null) return path;
        if (start.x == end.x && start.y == end.y) return path;

        currentRunID++;
        openSet.clear();

        Node startNode = nodeMap[start.y][start.x];
        startNode.reset(currentRunID);
        startNode.gCost = 0;
        startNode.hCost = getHeuristic(start.x, start.y, end.x, end.y);
        startNode.fCost = startNode.hCost;

        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.x == end.x && current.y == end.y) {
                retracePath(current, path);
                return path;
            }

            visitedWithRunId[current.y][current.x] = currentRunID;

            for (Direction dir : DIRECTIONS) {
                int nextX = current.x + dir.x;
                int nextY = current.y + dir.y;

                if (nextX < 0 || nextX >= size || nextY < 0 || nextY >= size) continue;
                if (visitedWithRunId[nextY][nextX] == currentRunID) continue;
                if (!worldMap.getTile(nextX, nextY).isPassable()) continue;

                double moveCost = (dir.x != 0 && dir.y != 0) ? DIAGONAL_COST : CARDINAL_COST;
                double edgeWeight = moveCost / worldMap.getTile(nextX, nextY).getSpeedMultiplier();
                double tentativeGCost = current.gCost + edgeWeight;

                Node neighbor = nodeMap[nextY][nextX];
                if (neighbor.runId != currentRunID) {
                    neighbor.reset(currentRunID);
                }

                if (tentativeGCost < neighbor.gCost) {
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = getHeuristic(nextX, nextY, end.x, end.y);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    neighbor.parent = current;
                    openSet.update(neighbor);
                }
            }
        }
        return path;
    }

    private double getHeuristic(int x, int y, int targetX, int targetY) {
        int dx = Math.abs(x - targetX);
        int dy = Math.abs(y - targetY);
        return (dx > dy) ? (DIAGONAL_COST * dy + CARDINAL_COST * (dx - dy))
                : (DIAGONAL_COST * dx + CARDINAL_COST * (dy - dx));
    }

    private void retracePath(Node endNode, List<Point> path) {
        Node current = endNode;
        int startIndex = path.size();
        while (current != null) {
            path.add(new Point(current.x, current.y));
            current = current.parent;
        }
        int endIndex = path.size() - 1;
        for (int i = startIndex, j = endIndex; i < j; i++, j--) {
            Point temp = path.get(i);
            path.set(i, path.get(j));
            path.set(j, temp);
        }
    }
}