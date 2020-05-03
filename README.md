# coroutines test extensions

[![version](https://img.shields.io/github/v/tag/floschu/coroutines-test-extensions?color=f88909&label=version)](https://bintray.com/flosch/test/coroutines-test-extensions) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/39072347acb94bf79651d7f16bfa63ca)](https://www.codacy.com/manual/floschu/coroutines-test-extensions?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=floschu/coroutines-test-extensions&amp;utm_campaign=Badge_Grade) [![build](https://github.com/floschu/coroutines-test-extensions/workflows/build/badge.svg)](https://github.com/floschu/coroutines-test-extensions/actions) [![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?color=7b6fe2)](LICENSE)

## installation

``` groovy
repositories {
    jcenter()
}

dependencies {
    testImplementation("at.florianschuster.test:coroutines-test-extensions:$version")
}
```

## what's in it

### TestFlow

A `TestFlow` contains all value emissions, the error and the completion of a `Flow` that is tested with `Flow.test()` or `Flow.testIn(CoroutineScope)`:

``` kotlin
@Test
fun testSomeFlow() = runBlockingTest {
    // given
    val testFlow = flow {
        emit(1)
        emit(2)
        emit(3)
        delay(1000)
        throw IOException()
    }.testIn(scope = this)

    // then
    testFlow expect emission(index = 0, expected = 1)
    testFlow expect emission(index = 2, expected = 3)
    testFlow expect emission(index = 2) { it >= 3 }
    testFlow expect emissions(1, 2, 3)
    testFlow expect emissionCount(3)
    testFlow expect noErrors()
    testFlow expect allEmissions { it is Number }
    
    advanceTimeBy(1000)
    
    testFlow expect error<IOException>()
    testFlow expect anyCompletion()
    testFlow expect exceptionalCompletion<IOException>()
}
```

### TestCoroutineScopeRule

A JUnit Rule that is a `TestCoroutineScope`.  Coroutine's launched in `TestCoroutineScopeRule` are auto canceled after the test completes.

When testing hot `Flow`s the job of the `Flow` is still active and the following exception is thrown:

> kotlinx.coroutines.test.UncompletedCoroutinesError: Test finished with active jobs

This can be avoided by launching the `Flow` in the `TestCoroutineScopeRule`.

``` kotlin
@get:Rule
val testScopeRule = TestCoroutineScopeRule()

@Test
fun testSomeNeverEndingFlow() {
    val channel = BroadcastChannel<Int>(1)
    val testFlow = channel.asFlow().testIn(testScopeRule)

    channel.offer(1)
    testFlow expect emissionCount(1)

    testFlow expect noCompletion()
    // `testFlow` never completes, but our `testScopeRule` will clean up after `testSomeNeverEndingFlow` is finished
}
```

## author

visit my [website](https://florianschuster.at/).
