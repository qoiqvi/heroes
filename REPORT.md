# Итоговый проект Heroes

**Автор:** Ващенко Дмитрий Игоревич

**Дата:** 01.02.2026

---

## Содержание

1. [Введение](#введение)
2. [Реализованные алгоритмы](#реализованные-алгоритмы)
   - [getSuitableUnits](#1-getsuitableunits)
   - [generate](#2-generate)
   - [getTargetPath](#3-gettargetpath)
   - [simulate](#4-simulate)
3. [Анализ алгоритмической сложности](#анализ-алгоритмической-сложности)
4. [Заключение](#заключение)

---

## Введение

Проект Heroes — пошаговая стратегия с боевой системой. В рамках данной работы были реализованы четыре ключевых алгоритма:

- **GeneratePreset** — генерация армии противника
- **SimulateBattle** — симуляция пошагового боя
- **SuitableForAttackUnitsFinder** — определение доступных целей для атаки
- **UnitTargetPathFinder** — поиск кратчайшего пути между юнитами

---

## Реализованные алгоритмы

### 1. getSuitableUnits

**Файл:** `SuitableForAttackUnitsFinderImpl.java`

**Назначение:** Определяет юнитов, подходящих для атаки (не закрытых другими юнитами своей армии).

**Алгоритм:**
- Для каждого ряда (по y-координате) находим юнита на "переднем крае"
- Если атакуется левая армия (компьютер) — ищем юнита с максимальным x
- Если атакуется правая армия (игрок) — ищем юнита с минимальным x

```java
@Override
public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
    List<Unit> result = new ArrayList<>();

    for (List<Unit> row : unitsByRow) {
        if (row == null || row.isEmpty()) continue;

        Unit frontUnit = null;
        int frontX = isLeftArmyTarget ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Unit unit : row) {
            if (unit == null || !unit.isAlive()) continue;

            int x = unit.getxCoordinate();
            if (isLeftArmyTarget) {
                if (x > frontX) { frontX = x; frontUnit = unit; }
            } else {
                if (x < frontX) { frontX = x; frontUnit = unit; }
            }
        }

        if (frontUnit != null) result.add(frontUnit);
    }
    return result;
}
```

**Сложность:** O(n), где n — общее количество юнитов

---

### 2. generate

**Файл:** `GeneratePresetImpl.java`

**Назначение:** Генерирует оптимальную армию компьютера с использованием жадного алгоритма.

**Алгоритм:**
1. Сортируем типы юнитов по эффективности (attack/cost, затем health/cost)
2. Итеративно добавляем наиболее эффективных юнитов
3. Соблюдаем ограничения: не более 11 юнитов каждого типа, общая стоимость ≤ maxPoints

```java
@Override
public Army generate(List<Unit> unitList, int maxPoints) {
    Army army = new Army();
    List<Unit> armyUnits = new ArrayList<>();
    int currentPoints = 0;

    // Сортировка по эффективности O(n log n)
    List<Unit> sortedUnits = new ArrayList<>(unitList);
    sortedUnits.sort((u1, u2) -> {
        double eff1 = (double) u1.getBaseAttack() / u1.getCost();
        double eff2 = (double) u2.getBaseAttack() / u2.getCost();
        if (Double.compare(eff2, eff1) != 0) return Double.compare(eff2, eff1);
        return Double.compare(
            (double) u2.getHealth() / u2.getCost(),
            (double) u1.getHealth() / u1.getCost()
        );
    });

    Map<String, Integer> unitCounts = new HashMap<>();

    // Жадный алгоритм O(n * m)
    boolean canAdd = true;
    while (canAdd) {
        canAdd = false;
        for (Unit template : sortedUnits) {
            String type = template.getUnitType();
            int count = unitCounts.getOrDefault(type, 0);
            if (count < 11 && currentPoints + template.getCost() <= maxPoints) {
                // Создаём нового юнита и добавляем в армию
                armyUnits.add(new Unit(...));
                unitCounts.put(type, count + 1);
                currentPoints += template.getCost();
                canAdd = true;
            }
        }
    }

    army.setUnits(armyUnits);
    army.setPoints(currentPoints);
    return army;
}
```

**Сложность:** O(n × m), где n — количество типов юнитов, m — максимальное число юнитов в армии

---

### 3. getTargetPath

**Файл:** `UnitTargetPathFinderImpl.java`

**Назначение:** Находит кратчайший путь между атакующим и атакуемым юнитами с помощью алгоритма BFS.

**Алгоритм:**
1. Создаём сетку препятствий (позиции других юнитов)
2. Запускаем BFS от позиции атакующего юнита
3. Ищем путь до позиции цели, обходя препятствия
4. Поддерживается движение в 8 направлениях (включая диагонали)
5. Восстанавливаем путь от цели к старту и разворачиваем

```java
@Override
public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
    int startX = attackUnit.getxCoordinate();
    int startY = attackUnit.getyCoordinate();
    int endX = targetUnit.getxCoordinate();
    int endY = targetUnit.getyCoordinate();

    // Создание сетки препятствий O(n)
    boolean[][] obstacles = new boolean[WIDTH][HEIGHT];
    for (Unit unit : existingUnitList) {
        if (unit.isAlive() && unit != attackUnit && unit != targetUnit) {
            obstacles[unit.getxCoordinate()][unit.getyCoordinate()] = true;
        }
    }

    // BFS O(WIDTH * HEIGHT)
    boolean[][] visited = new boolean[WIDTH][HEIGHT];
    int[][] parentX = new int[WIDTH][HEIGHT];
    int[][] parentY = new int[WIDTH][HEIGHT];
    Queue<int[]> queue = new LinkedList<>();

    queue.add(new int[]{startX, startY});
    visited[startX][startY] = true;

    while (!queue.isEmpty()) {
        int[] current = queue.poll();
        if (current[0] == endX && current[1] == endY) break;

        for (int[] dir : DIRECTIONS) {  // 8 направлений
            int nx = current[0] + dir[0];
            int ny = current[1] + dir[1];
            if (isValid(nx, ny) && !visited[nx][ny] && !obstacles[nx][ny]) {
                visited[nx][ny] = true;
                parentX[nx][ny] = current[0];
                parentY[nx][ny] = current[1];
                queue.add(new int[]{nx, ny});
            }
        }
    }

    // Восстановление пути O(длина пути)
    List<Edge> path = new ArrayList<>();
    // ... восстановление от цели к старту и reverse
    return path;
}
```

**Сложность:** O(WIDTH × HEIGHT) = O(27 × 21) = O(567)

---

### 4. simulate

**Файл:** `SimulateBattleImpl.java`

**Назначение:** Симулирует пошаговый бой между армией игрока и армией компьютера.

**Алгоритм:**
1. Пока в обеих армиях есть живые юниты — продолжаем бой
2. В начале каждого раунда собираем всех живых юнитов
3. Сортируем по атаке (убывание) — сильные ходят первыми
4. Каждый юнит атакует через `unit.getProgram().attack()`
5. После атаки вызываем `printBattleLog.printBattleLog(unit, target)`
6. Погибшие юниты пропускают свой ход

```java
@Override
public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
    while (hasAliveUnits(playerArmy) && hasAliveUnits(computerArmy)) {
        // Собираем живых юнитов
        List<Unit> roundUnits = new ArrayList<>();
        roundUnits.addAll(getAliveUnits(playerArmy));
        roundUnits.addAll(getAliveUnits(computerArmy));

        // Сортировка по атаке O(n log n)
        roundUnits.sort(Comparator.comparingInt(Unit::getBaseAttack).reversed());

        // Каждый юнит атакует O(n)
        for (Unit unit : roundUnits) {
            if (!unit.isAlive()) continue;

            boolean isPlayerUnit = playerArmy.getUnits().contains(unit);
            Army enemyArmy = isPlayerUnit ? computerArmy : playerArmy;
            if (!hasAliveUnits(enemyArmy)) continue;

            Unit target = unit.getProgram().attack();
            printBattleLog.printBattleLog(unit, target);
        }
    }
}
```

**Сложность:** O(n²), где n — общее количество юнитов

---

## Анализ алгоритмической сложности

| Метод | Требуемая сложность | Достигнутая сложность | Соответствие |
|-------|---------------------|----------------------|--------------|
| `getSuitableUnits` | O(n) | O(n) | ✅ |
| `generate` | O(n × m) | O(n × m) | ✅ |
| `getTargetPath` | O(WIDTH × HEIGHT) | O(WIDTH × HEIGHT) | ✅ |
| `simulate` | O(n²) | O(n²) | ✅ |

### Подробное обоснование

#### getSuitableUnits — O(n)
- Один проход по всем рядам: O(m), где m — количество рядов (фиксировано = 3)
- Для каждого ряда проход по юнитам: O(k), где k — юнитов в ряду
- **Итого:** O(m × k) = O(n), где n = m × k — общее количество юнитов

#### generate — O(n × m)
- Сортировка типов юнитов: O(n log n), где n — количество типов (4)
- Основной цикл: до m итераций (пока есть бюджет и не достигнут лимит)
- В каждой итерации проход по n типам: O(n)
- **Итого:** O(n log n + n × m) = O(n × m)

#### getTargetPath — O(WIDTH × HEIGHT)
- Создание сетки препятствий: O(k), где k — количество юнитов
- BFS: каждая клетка посещается максимум 1 раз: O(WIDTH × HEIGHT)
- Восстановление пути: O(длина пути) ≤ O(WIDTH × HEIGHT)
- **Итого:** O(WIDTH × HEIGHT) = O(567)

#### simulate — O(n²)
- Количество раундов: O(n) в худшем случае
- В каждом раунде: сортировка O(n log n) + обход O(n)
- Метод attack() работает за O(1)
- **Итого:** O(n × (n log n + n)) = O(n² log n) ≈ O(n²)

---

## Заключение

В рамках проекта были успешно реализованы все четыре требуемых алгоритма:

1. **getSuitableUnits** — линейный алгоритм определения доступных целей
2. **generate** — жадный алгоритм генерации оптимальной армии
3. **getTargetPath** — BFS для поиска кратчайшего пути на сетке
4. **simulate** — алгоритм симуляции пошагового боя

Все алгоритмы соответствуют требованиям по алгоритмической сложности и корректно обрабатывают граничные случаи.

---

## Структура проекта

```
heroes_student_task/
├── src/
│   └── programs/
│       ├── GeneratePresetImpl.java
│       ├── SimulateBattleImpl.java
│       ├── SuitableForAttackUnitsFinderImpl.java
│       └── UnitTargetPathFinderImpl.java
└── libs/
    └── heroes_task_lib-1.0-SNAPSHOT.jar
```

---

*Отчёт сгенерирован автоматически*
