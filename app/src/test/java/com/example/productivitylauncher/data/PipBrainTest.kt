package com.example.productivitylauncher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PipBrainTest {
    @Test fun water() {
        assertEquals(PipIntent.AddWater(2), parsePipCommand("drank 2 glasses of water"))
        assertEquals(PipIntent.AddWater(1), parsePipCommand("had a glass of water"))
        assertEquals(PipIntent.AddWater(3), parsePipCommand("Pip, I drank three glasses"))
        assertEquals(PipIntent.WaterStatus, parsePipCommand("how much water have I had?"))
    }

    @Test fun frog() {
        assertEquals(PipIntent.SetFrog("Finish chapter 4"), parsePipCommand("frog: Finish chapter 4"))
        assertEquals(PipIntent.SetFrog("Write essay"), parsePipCommand("set my frog to Write essay"))
        assertEquals(PipIntent.SetFrog("Maths"), parsePipCommand("set Maths as my frog"))
        assertEquals(PipIntent.SetTomorrow("Revise physics"), parsePipCommand("tomorrow's frog: Revise physics"))
        assertEquals(PipIntent.FrogDone, parsePipCommand("I ate the frog"))
        assertEquals(PipIntent.FrogDone, parsePipCommand("done"))
        assertEquals(PipIntent.FrogUndo, parsePipCommand("undo"))
        assertEquals(PipIntent.FrogStatus, parsePipCommand("what's my frog?"))
    }

    @Test fun focus() {
        assertEquals(PipIntent.Focus(30), parsePipCommand("focus 30"))
        assertEquals(PipIntent.Focus(25), parsePipCommand("start focus for 25 minutes"))
        assertEquals(PipIntent.Focus(null), parsePipCommand("let's focus"))
        assertEquals(PipIntent.Focus(60), parsePipCommand("focus for an hour"))
    }

    @Test fun capture() {
        assertEquals(PipIntent.AddDump("buy milk"), parsePipCommand("dump: buy milk"))
        assertEquals(PipIntent.AddDump("email the professor"), parsePipCommand("remind me to email the professor"))
        assertEquals(PipIntent.AddNote("Lab at 3"), parsePipCommand("note: Lab at 3"))
    }

    @Test fun questionsAndNavigation() {
        assertEquals(PipIntent.Ask(PipAsk.WhatNow), parsePipCommand("what should I do?"))
        assertEquals(PipIntent.Ask(PipAsk.HowAmI), parsePipCommand("how am I doing"))
        assertEquals(PipIntent.ScreenTime, parsePipCommand("screen time?"))
        assertEquals(PipIntent.Go(PipGo.Settings), parsePipCommand("open settings"))
        assertEquals(PipIntent.Go(PipGo.Evening), parsePipCommand("close the day"))
        assertEquals(PipIntent.Go(PipGo.BrainDump), parsePipCommand("show me my brain dump"))
    }

    @Test fun smallTalkAndUnknown() {
        assertEquals(PipIntent.Greeting, parsePipCommand("hi"))
        assertEquals(PipIntent.Greeting, parsePipCommand("hey pip"))
        assertEquals(PipIntent.Thanks, parsePipCommand("thanks!"))
        assertTrue(parsePipCommand("purple monkey dishwasher") is PipIntent.Unknown)
    }

    @Test fun habitWordMatching() {
        assertTrue(habitMatches("read one page", "I read my book today"))
        assertTrue(!habitMatches("read one page", "I went for a run"))
    }
}
