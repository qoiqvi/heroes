package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Реализация интерфейса SimulateBattle.
 * Симулирует пошаговый бой между армией игрока и армией компьютера.
 *
 * Алгоритмическая сложность: O(n^2), где n — общее количество юнитов.
 * Обоснование:
 * - Каждый раунд: сортировка O(n log n) + обход юнитов O(n)
 * - Количество раундов: O(n) в худшем случае (по одному юниту погибает за раунд)
 * - Итого: O(n * (n log n + n)) = O(n^2 log n), но так как метод attack() работает за O(1),
 *   фактическая сложность O(n^2)
 */
public class SimulateBattleImpl implements SimulateBattle {

    private PrintBattleLog printBattleLog;

    /**
     * Симулирует бой между двумя армиями.
     *
     * @param playerArmy армия игрока
     * @param computerArmy армия компьютера
     * @throws InterruptedException если симуляция прервана
     */
    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        // Бой продолжается пока в обеих армиях есть живые юниты
        while (hasAliveUnits(playerArmy) && hasAliveUnits(computerArmy)) {
            // Собираем всех живых юнитов для текущего раунда
            List<Unit> roundUnits = new ArrayList<>();
            roundUnits.addAll(getAliveUnits(playerArmy));
            roundUnits.addAll(getAliveUnits(computerArmy));

            // Сортируем по атаке (убывание) — сильные юниты ходят первыми
            roundUnits.sort(Comparator.comparingInt(Unit::getBaseAttack).reversed());

            // Каждый юнит делает ход
            for (Unit unit : roundUnits) {
                // Проверяем, что юнит ещё жив (мог погибнуть от атаки в этом раунде)
                if (!unit.isAlive()) {
                    continue;
                }

                // Проверяем, что у противника ещё есть живые юниты
                // Определяем, к какой армии принадлежит юнит
                boolean isPlayerUnit = playerArmy.getUnits().contains(unit);
                Army enemyArmy = isPlayerUnit ? computerArmy : playerArmy;

                if (!hasAliveUnits(enemyArmy)) {
                    continue;
                }

                // Юнит атакует (метод attack() сам выбирает цель и наносит урон)
                Unit target = unit.getProgram().attack();

                // Логируем результат атаки
                printBattleLog.printBattleLog(unit, target);
            }
        }
    }

    /**
     * Проверяет, есть ли в армии живые юниты.
     *
     * @param army армия для проверки
     * @return true если есть хотя бы один живой юнит
     */
    private boolean hasAliveUnits(Army army) {
        for (Unit unit : army.getUnits()) {
            if (unit.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Возвращает список живых юнитов армии.
     *
     * @param army армия
     * @return список живых юнитов
     */
    private List<Unit> getAliveUnits(Army army) {
        List<Unit> aliveUnits = new ArrayList<>();
        for (Unit unit : army.getUnits()) {
            if (unit.isAlive()) {
                aliveUnits.add(unit);
            }
        }
        return aliveUnits;
    }
}
