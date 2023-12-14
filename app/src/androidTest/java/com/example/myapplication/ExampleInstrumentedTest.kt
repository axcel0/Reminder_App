package com.example.myapplication

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import androidx.test.platform.app.InstrumentationRegistry

class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.myapplication", appContext.packageName)
    }
}
