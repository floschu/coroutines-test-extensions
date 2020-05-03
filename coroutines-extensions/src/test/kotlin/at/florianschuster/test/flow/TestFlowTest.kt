package at.florianschuster.test.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TestFlowTest {

    @Test
    fun `Flow test extension`() = runBlockingTest {
        val testFlow = (0 until 3).asFlow().test()
        testFlow.launchIn(this)

        assertEquals(listOf(0, 1, 2), testFlow.emissions)
    }

    @Test
    fun `Flow testIn extension`() = runBlockingTest {
        val testFlow = (0 until 3).asFlow().testIn(this)

        assertEquals(listOf(0, 1, 2), testFlow.emissions)
    }

    @Test
    fun `TestFlow emission expectations`() = runBlockingTest {
        val testFlow = (0 until 3).asFlow().testIn(this)

        testFlow expect noError()
        assertNull(testFlow.error)

        testFlow expect anyEmission()
        testFlow expect emissionCount(3)
        testFlow expect firstEmission(0)
        testFlow expect lastEmission(2)
        testFlow expect emissions(listOf(0, 1, 2))
        testFlow expect emission(index = 0, expected = 0)
        testFlow expect emission(1, 1)
        testFlow expect emission(2, 2)
        assertEquals(listOf(0, 1, 2), testFlow.emissions)

        testFlow expect anyCompletion()
        testFlow expect regularCompletion()
    }

    @Test
    fun `TestFlow error expectations`() = runBlockingTest {
        val exception = IOException()
        val testFlow = flow<Int> { throw exception }.testIn(this)

        testFlow expect anyError()
        testFlow expect error<IOException>()
        assertEquals(exception, testFlow.error)

        testFlow expect noEmissions()

        testFlow expect anyCompletion()
        testFlow expect exceptionalCompletion<IOException>()
    }

    @Test
    fun `TestFlow with delay`() = runBlockingTest {
        val testFlow = flow {
            emit(1)
            delay(1000)
            emit(2)
            delay(1000)
            throw IOException()
        }.testIn(this)

        testFlow expect emission(0, 1)
        advanceTimeBy(1000)
        testFlow expect emission(1, 2)
        advanceUntilIdle()
        testFlow expect error<IOException>()
    }
}