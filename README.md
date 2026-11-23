# SkyRotor — вертолёт для Minecraft Forge 1.20.1

Сущность-вертолёт, в которую можно сесть и полететь.

## Как играть
1. Скрафти вертолёт (рецепт: 5 железных слитков + 2 стеклянные панели + железный блок) или возьми из креатива (вкладка «Инструменты»).
2. ПКМ по земле — вертолёт появится.
3. ПКМ по вертолёту — сесть.
4. **Зажми K на 2 секунды** — двигатель заведётся, винт раскрутится и вертолёт сам взлетит. Повторное удержание K глушит двигатель.
5. Управление: W / S — вперёд / назад, A / D — поворот, Пробел — вверх, Ctrl — вниз, Shift — выйти.
6. Чтобы забрать вертолёт, ударь по нему (когда в нём никто не сидит).

Клавишу K можно переназначить в настройках управления (категория SkyRotor).

## Структура
```
SkyRotor/
├── .github/workflows/build.yml   автосборка jar через GitHub Actions
├── build.gradle                  сборка ForgeGradle (Forge 1.20.1)
├── settings.gradle
├── gradle.properties             версии
├── .gitignore
└── src/main
    ├── java/com/skyrotor
    │   ├── SkyRotorMod.java          точка входа мода
    │   ├── entity/HelicopterEntity.java   физика, двигатель, посадка
    │   ├── item/HelicopterItem.java       предмет-спавнер
    │   ├── network/ModNetwork.java        пакеты ввода и запуска двигателя
    │   ├── registry/ModEntities.java, ModItems.java
    │   └── client/                        клавиша K, рендер, модель
    └── resources
        ├── META-INF/mods.toml
        ├── pack.mcmeta
        ├── assets/skyrotor/{lang,models,textures}
        └── data/skyrotor/recipes
```

## Сборка
Залей всё в репозиторий GitHub — вкладка Actions соберёт jar (артефакт `SkyRotor-jar`).
Локально: `gradle build` (нужна Java 17), jar окажется в `build/libs/`.
