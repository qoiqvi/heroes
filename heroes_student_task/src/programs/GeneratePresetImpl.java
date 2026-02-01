package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

/**
 * Реализация интерфейса GeneratePreset.
 * Генерирует оптимальную армию компьютера с использованием жадного алгоритма.
 *
 * Алгоритмическая сложность: O(n * m), где n — количество типов юнитов, m — максимальное число юнитов в армии.
 * Обоснование:
 * - Сортировка типов юнитов: O(n log n)
 * - Основной цикл: максимум m итераций (пока можем добавлять юнитов)
 * - В каждой итерации проходим по n типам юнитов
 * - Итого: O(n log n + n * m) = O(n * m)
 */
public class GeneratePresetImpl implements GeneratePreset {

    private static final int MAX_UNITS_PER_TYPE = 11;

    /**
     * Генерирует армию компьютера с максимальной эффективностью.
     *
     * @param unitList список шаблонов юнитов (по одному каждого типа)
     * @param maxPoints максимальное количество очков для армии
     * @return сгенерированная армия
     */
    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        Army army = new Army();
        List<Unit> armyUnits = new ArrayList<>();
        int currentPoints = 0;

        // Сортируем типы юнитов по эффективности: attack/cost (убывание), затем health/cost
        // O(n log n)
        List<Unit> sortedUnits = new ArrayList<>(unitList);
        sortedUnits.sort((u1, u2) -> {
            double efficiency1 = (double) u1.getBaseAttack() / u1.getCost();
            double efficiency2 = (double) u2.getBaseAttack() / u2.getCost();
            if (Double.compare(efficiency2, efficiency1) != 0) {
                return Double.compare(efficiency2, efficiency1);
            }
            double healthEff1 = (double) u1.getHealth() / u1.getCost();
            double healthEff2 = (double) u2.getHealth() / u2.getCost();
            return Double.compare(healthEff2, healthEff1);
        });

        // Счётчик юнитов каждого типа
        Map<String, Integer> unitCounts = new HashMap<>();

        // Жадный алгоритм: добавляем наиболее эффективных юнитов
        // O(n * m), где m — максимальное число юнитов
        boolean canAdd = true;
        while (canAdd) {
            canAdd = false;
            for (Unit template : sortedUnits) {
                String unitType = template.getUnitType();
                int count = unitCounts.getOrDefault(unitType, 0);

                // Проверяем ограничения: не более 11 юнитов каждого типа и бюджет
                if (count < MAX_UNITS_PER_TYPE && currentPoints + template.getCost() <= maxPoints) {
                    // Создаём нового юнита
                    Unit newUnit = new Unit(
                            unitType + "_" + count,           // уникальное имя
                            template.getUnitType(),
                            template.getHealth(),
                            template.getBaseAttack(),
                            template.getCost(),
                            template.getAttackType(),
                            template.getAttackBonuses(),
                            template.getDefenceBonuses(),
                            0,                                // xCoordinate (установится позже)
                            0                                 // yCoordinate (установится позже)
                    );

                    armyUnits.add(newUnit);
                    unitCounts.put(unitType, count + 1);
                    currentPoints += template.getCost();
                    canAdd = true;
                }
            }
        }

        army.setUnits(armyUnits);
        army.setPoints(currentPoints);

        return army;
    }
}
