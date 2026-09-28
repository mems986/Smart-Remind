# Smart Remind & Stealth Notifier

Kotlin + Jetpack Compose (Material 3). minSdk 26, targetSdk 35.

## Запуск
1. Android Studio Ladybug (2024.2) або новіша, JDK 17.
2. File → Open → папка проєкту, дочекатися Gradle Sync.
3. Run на пристрої/емуляторі (Android 8.0+).

## План тестування
1. Надати дозволи (сповіщення, точні будильники) у верхній картці.
2. «Send test notification» — має з'явитися heads-up з написом, таймером 2:00 і кнопками Open/Close.
3. Вказати package (напр. com.android.chrome) → Open відкриває цей застосунок; Close ховає сповіщення.
4. «Test alarm in 10 seconds», заблокувати екран — перевірка AlarmManager у Doze.
5. Додати слот на +2 хв від поточного часу і відповідний день тижня — перевірка розкладу; перезавантажити пристрій — слот має відновитися.
6. Змінити мову (Авто/uk/en/de) — UI і текст кнопок сповіщення змінюються.
7. Hide launcher icon → іконка зникає. Повернути: набрати *#*#7373#*#* у «Телефоні»
   або через ADB: `adb shell am start -n com.smartremind.app/.MainActivity`.

## Структура
- data/ — AppSettings, SettingsRepository (Jetpack DataStore)
- scheduler/ — AlarmScheduler (setExactAndAllowWhileIdle), RescheduleWorker (WorkManager, страховка кожні 6 год)
- notification/ — NotificationHelper (RemoteViews, Chronometer-зворотний відлік, setTimeoutAfter)
- receiver/ — Alarm, Close, Boot (+час/пояс/оновлення), SecretCode
- util/ — StealthManager (activity-alias), LocaleHelper
- ui/ — Compose-екран налаштувань, ViewModel, тема Material You
