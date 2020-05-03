package at.florianschuster.test.flow

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        testFlow expect firstEmission { it == 0 }
        testFlow expect lastEmission(2)
        testFlow expect lastEmission { it == 2 }
        testFlow expect emissions(listOf(0, 1, 2))
        testFlow expect emission(index = 0, expected = 0)
        testFlow expect emission(index = 0) { it == 0 }
        testFlow expect emission(index = 1, expected = 1)
        testFlow expect emission(index = 1) { it == 1 }
        testFlow expect emission(index = 2, expected = 2)
        testFlow expect emission(index = 2) { it == 2 }
        testFlow expect allEmissions { it < 10 }
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

    @Test
    fun `TestFlow from BroadCastChannel`() = runBlockingTest {
        val channel = BroadcastChannel<Int>(BUFFERED)
        val testFlow = channel.asFlow().testIn(this)
        val testFlow2 = channel.asFlow().testIn(this)

        channel.offer(0)
        channel.send(1)
        channel.sendBlocking(2)
        testFlow expect emissions(0, 1, 2)

        channel.offer(4)
        testFlow expect emissions(0, 1, 2, 4)

        channel.close()
        testFlow expect regularCompletion()

        testFlow2 expect emissions(0, 1, 2, 4)
        testFlow expect regularCompletion()
    }

    @Test
    fun `TestFlow throws assertion errors`() = runBlockingTest {
        val emptyTestFlow = emptyFlow<Int>().testIn(this)
        val nonEmptyTestFlow = flowOf(0).testIn(this)
        val errorTestFlow = flow<Int> { throw IOException() }.testIn(this)
        val channel = BroadcastChannel<Int>(BUFFERED)
        val channelTestFlow = channel.asFlow().testIn(this)

        assertFailsWith<AssertionError> { errorTestFlow expect noError() }
        assertFailsWith<AssertionError> { emptyTestFlow expect anyError() }
        assertFailsWith<AssertionError> { emptyTestFlow expect error<Throwable>() }
        assertFailsWith<AssertionError> { nonEmptyTestFlow expect noEmissions() }
        assertFailsWith<AssertionError> { emptyTestFlow expect anyEmission() }
        assertFailsWith<AssertionError> { emptyTestFlow expect emissionCount(1) }
        assertFailsWith<AssertionError> { emptyTestFlow expect emissions(0, 1) }
        assertFailsWith<AssertionError> { emptyTestFlow expect emissions(listOf(0, 1)) }
        assertFailsWith<AssertionError> { emptyTestFlow expect allEmissions { it > 0 } }
        assertFailsWith<AssertionError> { emptyTestFlow expect emission(index = 0, expected = 1) }
        assertFailsWith<AssertionError> { emptyTestFlow expect emission(index = 0) { it == 1 } }
        assertFailsWith<AssertionError> { emptyTestFlow expect firstEmission(0) }
        assertFailsWith<AssertionError> { emptyTestFlow expect firstEmission { it == 0 } }
        assertFailsWith<AssertionError> { emptyTestFlow expect lastEmission(0) }
        assertFailsWith<AssertionError> { emptyTestFlow expect lastEmission { it == 0 } }
        assertFailsWith<AssertionError> { nonEmptyTestFlow expect noCompletion() }
        assertFailsWith<AssertionError> { channelTestFlow expect anyCompletion() }
        assertFailsWith<AssertionError> { errorTestFlow expect regularCompletion() }
        assertFailsWith<AssertionError> { nonEmptyTestFlow expect exceptionalCompletion<Throwable>() }

        channel.close()
    }
}