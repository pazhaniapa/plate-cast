package com.palmah.cafe.amirtham.utils

import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun formatPrice(price: Double): String {
    val decimalValue = (price * 100).roundToInt()
    val wholeValue = decimalValue / 100
    val fraction = (decimalValue % 100).absoluteValue
    return "₹${wholeValue}.${fraction.toString().padStart(2, '0')}"
}

private val MONTH_NAMES = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/** Formats [epochMillis] (UTC) as e.g. "Sep 22, 2026". Returns null if [epochMillis] isn't set (<= 0). */
fun formatDate(epochMillis: Long): String? {
    if (epochMillis <= 0L) return null
    val (year, month, day) = civilDateFromEpochDay(epochMillis / 86_400_000L)
    return "${MONTH_NAMES[month - 1]} $day, $year"
}

/**
 * Howard Hinnant's `civil_from_days` algorithm: converts days since the 1970-01-01 UTC epoch into
 * a (year, month, day) proleptic-Gregorian date, without needing a date/time library.
 */
private fun civilDateFromEpochDay(epochDay: Long): Triple<Int, Int, Int> {
    val z = epochDay + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val dayOfEra = z - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPart = (5 * dayOfYear + 2) / 153
    val day = (dayOfYear - (153 * monthPart + 2) / 5 + 1).toInt()
    val month = (if (monthPart < 10) monthPart + 3 else monthPart - 9).toInt()
    return Triple((if (month <= 2) year + 1 else year).toInt(), month, day)
}
