package at.florianschuster.flow.test

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestFlowTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `Flow test extension`() {
        val testFlow = (0 until 3).asFlow().test()
        testFlow.launchIn(testScopeRule)

        assertEquals(listOf(0, 1, 2), testFlow.emissions)
    }

    @Test
    fun `Flow testIn extension`() {
        val testFlow = (0 until 3).asFlow().testIn(testScopeRule)

        assertEquals(listOf(0, 1, 2), testFlow.emissions)
    }

    @Test
    fun `TestFlow emission expectations`() {
        val testFlow = (0 until 3).asFlow().testIn(testScopeRule)

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
    fun `TestFlow error expectations`() {
        val exception = IOException()
        val testFlow = flow<Int> { throw exception }.testIn(testScopeRule)

        testFlow expect anyError()
        testFlow expect error<IOException>()
        assertEquals(exception, testFlow.error)

        testFlow expect noEmissions()

        testFlow expect anyCompletion()
        testFlow expect exceptionalCompletion<IOException>()
    }

    @Test
    fun `TestFlow reset`() {
        val channel = BroadcastChannel<Int>(1)
        val testFlow = channel.asFlow().testIn(testScopeRule)

        channel.offer(1)
        testFlow expect emissionCount(1)

        testFlow.reset()
        testFlow expect noEmissions()

        channel.offer(1)
        testFlow expect emissionCount(1)

        testFlow expect noCompletion()
    }
}