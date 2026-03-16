package com.miftakhulrizky.yearprogress.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.hours
import androidx.compose.ui.platform.LocalContext

data class YearProgressState(
    val date: LocalDate,
    val year: Int,
    val dayOfYear: Int,
    val totalDays: Int,
    val daysLeft: Int,
    val percent: Int,
    val headerDate: String,
    val monthItems: List<MonthProgress>
) {
    companion object {
        fun from(date: LocalDate, holidays: Set<LocalDate> = emptySet()): YearProgressState {
            val totalDays = date.lengthOfYear()
            val percent = ((date.dayOfYear.toFloat() / totalDays.toFloat()) * 100).toInt()

            return YearProgressState(
                date = date,
                year = date.year,
                dayOfYear = date.dayOfYear,
                totalDays = totalDays,
                daysLeft = totalDays - date.dayOfYear,
                percent = percent,
                headerDate = date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US)).uppercase(Locale.US),
                monthItems = Month.entries.map { month ->
                    MonthProgress.from(
                        year = date.year,
                        month = month,
                        currentDate = date,
                        holidays = holidays
                    )
                }
            )
        }
    }
}

data class MonthProgress(
    val month: Month,
    val label: String,
    val isPast: Boolean,
    val isCurrent: Boolean,
    val columns: Int,
    val cells: List<DayCell>
) {
    companion object {
        fun from(year: Int, month: Month, currentDate: LocalDate, holidays: Set<LocalDate>): MonthProgress {
            val yearMonth = YearMonth.of(year, month)
            val firstDay = yearMonth.atDay(1)
            val offset = firstDay.dayOfWeek.toSundayBasedIndex()
            val totalCells = offset + yearMonth.lengthOfMonth()
            val columns = kotlin.math.ceil(totalCells / 7f).toInt()
            val currentMonth = currentDate.month == month

            val cells = buildList {
                repeat(offset) { add(DayCell.Empty) }
                for (day in 1..yearMonth.lengthOfMonth()) {
                    val date = yearMonth.atDay(day)
                    val isHoliday = holidays.contains(date)
                    add(
                        when {
                            date.isBefore(currentDate) && isHoliday -> DayCell.Holiday
                            date.isBefore(currentDate) -> DayCell.Filled
                            date == currentDate -> DayCell.Current
                            isHoliday -> DayCell.Holiday
                            else -> DayCell.Pending
                        }
                    )
                }
            }

            return MonthProgress(
                month = month,
                label = month.getDisplayName(TextStyle.SHORT, Locale.US).uppercase(Locale.US),
                isPast = month.value < currentDate.monthValue,
                isCurrent = currentMonth,
                columns = columns,
                cells = cells
            )
        }
    }
}

enum class DayCell {
    Empty,
    Filled,
    Current,
    Pending,
    Holiday
}

@Composable
fun rememberCurrentYearProgressState(): YearProgressState {
    val context = LocalContext.current
    val state by produceState(initialValue = YearProgressState.from(LocalDate.now(), emptySet())) {
        val holidays = HolidayManager.getHolidays(context, LocalDate.now().year)
        while (true) {
            value = YearProgressState.from(LocalDate.now(), holidays)
            delay(1.hours)
        }
    }
    return state
}

private fun DayOfWeek.toSundayBasedIndex(): Int = when (this) {
    DayOfWeek.SUNDAY -> 0
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
}
