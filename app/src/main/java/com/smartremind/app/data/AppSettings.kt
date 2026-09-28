package com.smartremind.app.data

const val LANG_AUTO = "auto"

/** Що показує «піла» (чіп) у статус-барі поруч із годинником. */
const val PILL_TIMER = "timer"
const val PILL_LABEL = "label"

/**
 * @param daysMask біти 0..6 = Пн..Нд
 * @param times хвилини від початку доби (наприклад 9:30 = 570)
 * @param pillContent PILL_TIMER (зворотний відлік) або PILL_LABEL (напис)
 * @param alertOnStart true — heads-up зі звуком/вібрацією на старті; false — лише тихий чіп
 */
data class AppSettings(
    val language: String = LANG_AUTO,
    val targetPackage: String = "",
    val timerMinutes: Int = 2,
    val label: String = "pep",
    val daysMask: Int = 0b0011111,
    val times: List<Int> = listOf(9 * 60, 18 * 60),
    val enabled: Boolean = true,
    val dynamicColor: Boolean = true,
    val pillContent: String = PILL_TIMER,
    val alertOnStart: Boolean = true
) {
    fun hasDay(dayIndex: Int): Boolean = (daysMask shr dayIndex) and 1 == 1
}
