package at.florianschuster.test.flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.coroutines.ContinuationInterceptor

/**
 * Rule that is a [TestCoroutineScope].
 * Coroutine's launched in [TestCoroutineScopeRule] are auto canceled after the test completes.
 *
 * @property overrideMainDispatcher Boolean if set to true, [Dispatchers.Main] will use
 * [TestCoroutineDispatcher] of [TestCoroutineScope].
 */
@ExperimentalCoroutinesApi
class TestCoroutineScopeRule(
    val overrideMainDispatcher: Boolean = false
) : TestRule, TestCoroutineScope by TestCoroutineScope() {

    val mainDispatcher = coroutineContext[ContinuationInterceptor] as TestCoroutineDispatcher

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                if (overrideMainDispatcher) Dispatchers.setMain(mainDispatcher)
                base.evaluate()
                cleanupTestCoroutines()
                if (overrideMainDispatcher) Dispatchers.resetMain()
            }
        }
}