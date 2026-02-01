package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация интерфейса SuitableForAttackUnitsFinder.
 * Определяет юнитов, подходящих для атаки (не закрытых другими юнитами).
 *
 * Алгоритмическая сложность: O(n), где n — общее количество юнитов во всех рядах.
 * Обоснование: однократный проход по всем юнитам во всех рядах.
 */
public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    /**
     * Находит юнитов, подходящих для атаки.
     *
     * @param unitsByRow список юнитов противника, сгруппированных по рядам (y-координата)
     * @param isLeftArmyTarget true — атакуется левая армия (компьютер), false — правая (игрок)
     * @return список юнитов на переднем крае (не закрытых другими юнитами своей армии)
     */
    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        List<Unit> result = new ArrayList<>();

        // O(m) — проход по рядам, где m — количество рядов
        for (List<Unit> row : unitsByRow) {
            if (row == null || row.isEmpty()) {
                continue;
            }

            Unit frontUnit = null;
            int frontX = isLeftArmyTarget ? Integer.MIN_VALUE : Integer.MAX_VALUE;

            // O(k) — проход по юнитам в ряду, где k — количество юнитов в ряду
            for (Unit unit : row) {
                if (unit == null || !unit.isAlive()) {
                    continue;
                }

                int x = unit.getxCoordinate();

                if (isLeftArmyTarget) {
                    // Атакуем левую армию (компьютера), атака идёт справа от игрока
                    // Ищем юнитов на правом краю левой армии (максимальный x)
                    // Они ближе всего к атакующим и не закрыты своими юнитами
                    if (x > frontX) {
                        frontX = x;
                        frontUnit = unit;
                    }
                } else {
                    // Атакуем правую армию (игрока), атака идёт слева от компьютера
                    // Ищем юнитов на левом краю правой армии (минимальный x)
                    if (x < frontX) {
                        frontX = x;
                        frontUnit = unit;
                    }
                }
            }

            if (frontUnit != null) {
                result.add(frontUnit);
            }
        }

        return result;
    }
}
