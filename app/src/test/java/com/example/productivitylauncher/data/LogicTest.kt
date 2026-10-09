package com.example.productivitylauncher.data

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    @Test fun phraseHasRequestedWordCount() {
        val p = generatePhrase(5, Random(1))
        assertEquals(5, p.split(" ").size)
    }

    @Test fun phraseMatchIgnoresCaseAndSpaces() {
        assertTrue(phraseMatches("  River   CANDLE ", "river candle"))
        assertFalse(phraseMatches("river candles", "river candle"))
    }

    @Test fun phraseProgressCountsLeadingMatch() {
        assertEquals(3, phraseProgress("rivxr", "river candle").coerceAtMost(3))
        assertEquals(5, phraseProgress("river", "river candle"))
    }

    @Test fun waitGrowsButIsCapped() {
        assertEquals(0, extraWaitSeconds(0, true))
        assertEquals(15, extraWaitSeconds(3, true))
        assertEquals(60, extraWaitSeconds(100, true))
        assertEquals(0, extraWaitSeconds(10, false))
    }

    @Test fun weakeningIsDetected() {
        assertTrue(isWeakening(GateSetting.Limit, 120, 180))
        assertFalse(isWeakening(GateSetting.Limit, 120, 60))
        assertTrue(isWeakening(GateSetting.Breaths, 3, 1))
        assertFalse(isWeakening(GateSetting.Breaths, 3, 5))
        assertTrue(isWeakening(GateSetting.GrowingWait, 1, 0))
    }

    @Test fun formatting() {
        assertEquals("1h 12m", formatMinutes(72))
        assertEquals("45m", formatMinutes(45))
        assertEquals("00:05", formatClock(5_000))
        assertEquals("10:00", formatClock(600_000))
    }

    @Test fun sections() {
        assertEquals("A", sectionOf("alarm"))
        assertEquals("#", sectionOf("1Password"))
        assertEquals("K", letterOf("kindle"))
    }

    private fun facts(
        hour: Int = 10, frog: String = "Study", done: Boolean = false, water: Int = 0,
        active: Long? = null, evening: Boolean = false,
    ) = PipFacts(hour, frog, done, water, 8, null, 120, active, 21, evening, 0, false)

    @Test fun pipAsksForFrogWhenEmpty() {
        assertEquals(PipAction.SetFrog, pipMessage(facts(frog = "")).action)
    }

    @Test fun pipSuggestsEveningLate() {
        assertEquals(PipAction.Evening, pipMessage(facts(hour = 22)).action)
    }

    @Test fun pipWarnsOnLongUse() {
        assertTrue(pipMessage(facts(active = 42)).nudge)
    }

    @Test fun pipCelebratesFrog() {
        assertEquals("[^o^]", pipMessage(facts(done = true, water = 8)).face)
    }
}
