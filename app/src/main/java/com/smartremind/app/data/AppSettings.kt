package com.smartremind.app.data

const val LANG_AUTO = "auto"

/**
 * @param daysMask біти 0..6 = Пн..Нд
 * @param times хвилини від початку доби (наприклад 9:30 = 570)
 */
data class AppSettings(
    val language: String = LANG_AUTO,
    val targetPackage: String = "",
    val timerMinutes: Int = 2,
    val label: String = "pep",
    val daysMask: Int = 0b0011111,
    val times: List<Int> = listOf(9 * 60, 18 * 60),
    val enabled: Boolean = true,
    val dynamicColor: Boolean = true
) {
    fun hasDay(dayIndex: Int): Boolean = (daysMask shr dayIndex) and 1 == 1
}
