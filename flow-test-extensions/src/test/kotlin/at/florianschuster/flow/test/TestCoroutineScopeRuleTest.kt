package at.florianschuster.flow.test

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class TestCoroutineScopeRuleTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

    @Test
    fun `TestFlow reset`() = runBlockingTest{
        val channel = BroadcastChannel<Int>(1)
        val testFlow = channel.asFlow().testIn(this)

        channel.offer(1)
        testFlow expect emissionCount(1)

        testFlow.reset()
        testFlow expect noEmissions()

        channel.offer(1)
        testFlow expect emissionCount(1)

        testFlow expect noCompletion()
    }
}