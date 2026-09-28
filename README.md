# Smart Remind & Stealth Notifier — v2.0

Kotlin + Jetpack Compose, Material 3 Expressive. minSdk 26, compileSdk/targetSdk 36.
Toolchain: AGP 8.10.1, Gradle 8.13, Kotlin 2.1.21, JDK 17.

## Що нового у v2.0
- Live-віджет: на Android 16+ сповіщення стає Live Update (чіп у статус-барі поруч із годинником,
  шкала прогресу в шторці, зворотний відлік). На Android 8–15 — звичайне heads-up зі шкалою прогресу.
- Шкалу щосекунди рухає короткоживучий foreground-сервіс (CountdownService); тривалість — у налаштуваннях.
- Кнопка «Відкрити» відкриває застосунок, обраний у списку встановлених програм (пошук + іконки).
- Дизайн: Material 3 Expressive, кольорові картки, динамічні кольори або яскрава власна палітра.

## Збірка
GitHub Actions: Actions → Build APK → Run workflow → Artifacts → app-debug.
Локально: `./gradlew assembleDebug` → app/build/outputs/apk/debug/app-debug.apk

## Перший запуск
1. Дозволити сповіщення; на Android 12+ — точні будильники; на Android 16+ — «Живі сповіщення».
2. Обрати застосунок для кнопки «Відкрити».
3. «Показати віджет зараз» — перевірка Live-віджета.
4. «Тестовий будильник через 10 секунд» + заблокувати екран — перевірка AlarmManager.

## Повернення прихованої іконки
Набрати *#*#7373#*#* у «Телефоні» або `adb shell am start -n com.smartremind.app/.MainActivity`.
