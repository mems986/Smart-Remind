# Smart Remind & Stealth Notifier — v3.0

Kotlin + Jetpack Compose, Material 3 Expressive. minSdk 26, compileSdk/targetSdk 36.
Toolchain (ті самі версії, що в LiveMedia): AGP 8.13.0, Gradle 8.13, Kotlin 2.2.21, JDK 17.

## Що нового у v3.0
- Live-віджет за схемою LiveMedia: promoted ongoing notification. На Android 16+ поруч із годинником
  з'являється «піла» (іконка + зворотний відлік або напис), у шторці — шкала прогресу та дві кнопки.
- Дві стандартні кнопки «Відкрити» / «Закрити» (система сама підбирає контрастні кольори).
- Вибір застосунку для «Відкрити» зі списку встановлених (іконки, пошук) або ручне введення package name.
- Дизайн Material 3 Expressive: cookie-форми, хвиляста шкала, динамічні кольори чи яскрава палітра.
- Налаштування: що показує піла (відлік/напис), heads-up на старті або лише тихий чіп.

## Збірка
GitHub Actions: Actions → Build APK → Run workflow → Artifacts → app-debug.
Локально: `./gradlew assembleDebug` → app/build/outputs/apk/debug/app-debug.apk

## Перший запуск
1. Дозволити сповіщення; на Android 12+ — точні будильники; на Android 16+ — «Живі сповіщення».
2. Обрати застосунок для кнопки «Відкрити».
3. «Показати віджет зараз» — перевірка піли у статус-барі.
4. «Тестовий будильник через 10 секунд» + заблокувати екран — перевірка AlarmManager.

## Повернення прихованої іконки
Набрати *#*#7373#*#* у «Телефоні» або `adb shell am start -n com.smartremind.app/.MainActivity`.
