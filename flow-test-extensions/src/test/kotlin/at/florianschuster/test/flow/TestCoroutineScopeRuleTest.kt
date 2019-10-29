package at.florianschuster.test.flow

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import org.junit.Rule
import org.junit.Test

class TestCoroutineScopeRuleTest {

    @get:Rule
    val testScopeRule = TestCoroutineScopeRule()

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