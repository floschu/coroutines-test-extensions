package at.florianschuster.test.coroutines

import at.florianschuster.test.flow.emissionCount
import at.florianschuster.test.flow.expect
import at.florianschuster.test.flow.noCompletion
import at.florianschuster.test.flow.noEmissions
import at.florianschuster.test.flow.testIn
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import org.junit.Rule
import org.junit.Test

internal class TestCoroutineScopeRuleTest {

    @get:Rule
    val testScopeRule =
        TestCoroutineScopeRule()

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