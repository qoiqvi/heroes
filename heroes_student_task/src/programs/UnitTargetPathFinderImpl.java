package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

/**
 * Реализация интерфейса UnitTargetPathFinder.
 * Находит кратчайший путь между атакующим и атакуемым юнитами с помощью BFS.
 *
 * Алгоритмическая сложность: O(WIDTH * HEIGHT), где WIDTH=27, HEIGHT=21.
 * Обоснование:
 * - Создание сетки препятствий: O(n), где n — количество юнитов
 * - BFS обходит каждую клетку максимум один раз: O(WIDTH * HEIGHT)
 * - Восстановление пути: O(длина пути) <= O(WIDTH * HEIGHT)
 * - Итого: O(WIDTH * HEIGHT)
 */
public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;

    // 8 направлений движения (включая диагонали)
    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    /**
     * Находит кратчайший путь от атакующего юнита до цели.
     *
     * @param attackUnit атакующий юнит
     * @param targetUnit юнит-цель
     * @param existingUnitList список всех юнитов на поле (препятствия)
     * @return список координат пути от атакующего до цели, или пустой список если путь не найден
     */
    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        int startX = attackUnit.getxCoordinate();
        int startY = attackUnit.getyCoordinate();
        int endX = targetUnit.getxCoordinate();
        int endY = targetUnit.getyCoordinate();

        // Проверка границ
        if (!isValid(startX, startY) || !isValid(endX, endY)) {
            return Collections.emptyList();
        }

        // Создаём сетку препятствий O(n)
        boolean[][] obstacles = new boolean[WIDTH][HEIGHT];
        for (Unit unit : existingUnitList) {
            if (unit != null && unit.isAlive() && unit != attackUnit && unit != targetUnit) {
                int x = unit.getxCoordinate();
                int y = unit.getyCoordinate();
                if (isValid(x, y)) {
                    obstacles[x][y] = true;
                }
            }
        }

        // BFS для поиска кратчайшего пути O(WIDTH * HEIGHT)
        int[][] parentX = new int[WIDTH][HEIGHT];
        int[][] parentY = new int[WIDTH][HEIGHT];
        for (int[] row : parentX) Arrays.fill(row, -1);
        for (int[] row : parentY) Arrays.fill(row, -1);

        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        boolean found = false;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            // Достигли цели
            if (cx == endX && cy == endY) {
                found = true;
                break;
            }

            // Проверяем все 8 направлений
            for (int[] dir : DIRECTIONS) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                // Проверяем: в границах, не посещена, не препятствие (или это цель)
                if (isValid(nx, ny) && !visited[nx][ny] &&
                        (!obstacles[nx][ny] || (nx == endX && ny == endY))) {
                    visited[nx][ny] = true;
                    parentX[nx][ny] = cx;
                    parentY[nx][ny] = cy;
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        // Путь не найден
        if (!found) {
            return Collections.emptyList();
        }

        // Восстанавливаем путь от цели к старту O(длина пути)
        List<Edge> path = new ArrayList<>();
        int cx = endX;
        int cy = endY;

        while (cx != startX || cy != startY) {
            path.add(new Edge(cx, cy));
            int px = parentX[cx][cy];
            int py = parentY[cx][cy];
            cx = px;
            cy = py;
        }
        path.add(new Edge(startX, startY));

        // Разворачиваем путь (от старта к цели)
        Collections.reverse(path);

        return path;
    }

    /**
     * Проверяет, находятся ли координаты в пределах игрового поля.
     */
    private boolean isValid(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }
}
