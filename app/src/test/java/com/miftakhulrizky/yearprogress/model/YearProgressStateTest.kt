package com.miftakhulrizky.yearprogress.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YearProgressStateTest {
    @Test
    fun `calculates year progress and current month cell`() {
        val state = YearProgressState.from(LocalDate.of(2026, 3, 6))

        assertEquals(2026, state.year)
        assertEquals(65, state.dayOfYear)
        assertEquals(300, state.daysLeft)
        assertEquals(17, state.percent)

        val march = state.monthItems[2]
        assertTrue(march.isCurrent)
        assertEquals(DayCell.Current, march.cells.first { it == DayCell.Current })
    }
}
