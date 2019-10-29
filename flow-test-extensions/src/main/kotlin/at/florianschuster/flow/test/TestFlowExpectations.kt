@file:Suppress("TooManyFunctions")

package at.florianschuster.flow.test

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@FlowPreview
typealias TestFlowExpectation<T> = (TestFlow<T>) -> Unit

/**
 * Expects a certain [TestFlowExpectation] from the [TestFlow].
 */
@FlowPreview
infix fun <T> TestFlow<T>.expect(expectation: TestFlowExpectation<T>): TestFlow<T> {
    expectation(this)
    return this
}

/**
 * Asserts that no errors occurred during collection the the [Flow].
 */
@FlowPreview
fun noError(): TestFlowExpectation<*> = { testFlow ->
    assertNull(testFlow.error, "${testFlow.tag} has error")
}

/**
 * Asserts that an errors occurred during collection the the [Flow].
 */
@FlowPreview
fun anyError(): TestFlowExpectation<*> = { testFlow ->
    assertNotNull(testFlow.error, "${testFlow.tag} has no error")
}

/**
 * Asserts that an [T] error occurred during collection of the [Flow].
 */
@FlowPreview
inline fun <reified T : Throwable> error(): TestFlowExpectation<*> = { testFlow ->
    assertEquals(T::class, testFlow.error!!::class, "${testFlow.tag} has wrong error")
}

/**
 * Asserts that no emissions occurred during collection the the [Flow].
 */
@FlowPreview
fun noEmissions(): TestFlowExpectation<*> = { testFlow ->
    assertEquals(0, testFlow.emissions.count(), "${testFlow.tag} has values")
}

/**
 * Asserts that any emissions occurred during collection the the [Flow].
 */
@FlowPreview
fun anyEmission(): TestFlowExpectation<*> = { testFlow ->
    assertNotEquals(0, testFlow.emissions.count(), "${testFlow.tag} has no values")
}

/**
 * Asserts that [expected] count of emissions occurred during collection the the [Flow].
 */
@FlowPreview
fun emissionCount(expected: Int): TestFlowExpectation<*> = { testFlow ->
    assertEquals(
        expected,
        testFlow.emissions.count(),
        "${testFlow.tag} has wrong emissions count"
    )
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */
@FlowPreview
fun <T> emissions(vararg expected: T): TestFlowExpectation<T> = { testFlow ->
    assertEquals(expected.toList(), testFlow.emissions, "${testFlow.tag} has wrong emissions")
}

/**
 * Asserts that [expected] emissions occurred during collection the the [Flow].
 */
@FlowPreview
fun <T> emissions(expected: List<T>): TestFlowExpectation<T> = { testFlow ->
    assertEquals(expected, testFlow.emissions, "${testFlow.tag} has wrong emissions")
}

/**
 * Asserts that [expected] emission occurred at [index] in the [emissions] collection.
 */
@FlowPreview
fun <T> emission(index: Int, expected: T): TestFlowExpectation<T> = { testFlow ->
    assertEquals(expected, testFlow.emissions[index], "${testFlow.tag} no emission at $index")
}

/**
 * Asserts that [expected] emission occurred first in the [emissions] collection.
 */
@FlowPreview
fun <T> firstEmission(expected: T): TestFlowExpectation<T> = { testFlow ->
    assertEquals(expected, testFlow.emissions.first(), "${testFlow.tag} wrong first emission")
}

/**
 * Asserts that [expected] emission occurred last in the [emissions] collection.
 */
@FlowPreview
fun <T> lastEmission(expected: T): TestFlowExpectation<T> = { testFlow ->
    assertEquals(expected, testFlow.emissions.last(), "${testFlow.tag} wrong last emission")
}

/**
 * Asserts that the [TestFlow] has not completed emission collection.
 */
@FlowPreview
fun noCompletion(): TestFlowExpectation<*> = { testFlow ->
    assertFalse(testFlow.completed, "${testFlow.tag} has completion")
}

/**
 * Asserts that the [TestFlow] has completed emission collection.
 */
@FlowPreview
fun anyCompletion(): TestFlowExpectation<*> = { testFlow ->
    assertTrue(testFlow.completed, "${testFlow.tag} has no completion")
}

/**
 * Asserts that the [TestFlow] has completed emission collection with no error.
 */
@FlowPreview
fun regularCompletion(): TestFlowExpectation<*> = { testFlow ->
    testFlow expect anyCompletion()
    testFlow expect noError()
}

/**
 * Asserts that the [TestFlow] has completed emission collection with an error of type [T].
 */
@FlowPreview
inline fun <reified T : Throwable> exceptionalCompletion(): TestFlowExpectation<*> = { testFlow ->
    testFlow expect anyCompletion()
    testFlow expect error<T>()
}