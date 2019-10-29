package at.florianschuster.flow.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestCoroutineScope

/**
 * Tests a [Flow] by creating and returning a [TestFlow] which caches all value
 * emissions, error and completion.
 */
@FlowPreview
@ExperimentalCoroutinesApi
fun <T> Flow<T>.test(): TestFlow<T> =
    TestFlow(this)

/**
 * Tests a [Flow] by creating and returning a [TestFlow] which caches all value
 * emissions, error and completion.
 *
 * The [TestFlow] is also launched inside the [scope].
 */
@FlowPreview
@ExperimentalCoroutinesApi
fun <T> Flow<T>.testIn(scope: TestCoroutineScope): TestFlow<T> {
    val testFlow = TestFlow(this)
    testFlow.launchIn(scope)
    return testFlow
}