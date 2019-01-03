@file:JvmName("Main")

package com.github.kubode.reaktor.example.cli

import com.github.kubode.reaktor.SimpleReactor
import com.github.kubode.reaktor.debug
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class MySimpleReactor(
    coroutineContext: CoroutineContext
) : SimpleReactor<MySimpleReactor.Action, MySimpleReactor.Mutation, MySimpleReactor.State>(
    coroutineContext,
    State()
) {

    sealed class Action {
        object Increment : Action()
        object IncrementDelayed : Action()

        override fun toString() = javaClass.simpleName!!
    }

    sealed class Mutation {
        object Increment : Mutation()
    }

    data class State(
        val number: Int = 0
    )

    private var delayedJob: Job? = null

    override fun mutate(action: Action) = produce {
        debug("MyReactor.mutate $action")
        when (action) {
            is Action.Increment -> {
                send(Mutation.Increment)
            }
            is Action.IncrementDelayed -> {
                delayedJob?.cancel()
                delayedJob = launch {
                    try {
                        delay(1000)
                        send(Mutation.Increment)
                    } catch (e: CancellationException) {
                        debug("Cancel! ${e.message}")
                        throw e
                    }
                }
            }
        }
    }

    override fun reduce(state: State, mutation: Mutation): State {
        debug("MyReactor.reduce $state, $mutation")
        return when (mutation) {
            is Mutation.Increment -> state.copy(
                number = state.number + 1
            )
        }
    }
}

fun main() = runBlocking {
    debug("main start")
    val reactor = MySimpleReactor(coroutineContext + Dispatchers.Default)
    launch {
        reactor.state.consumeEach { state ->
            debug("main reactor.state.consumeEach $state")
        }
    }
    debug("main send increment 1")
    reactor.action.send(MySimpleReactor.Action.Increment)
    delay(1000)

    debug("main send increment 2")
    reactor.action.send(MySimpleReactor.Action.Increment)

    debug("main send increment delayed 3")
    reactor.action.send(MySimpleReactor.Action.IncrementDelayed)
    delay(500)

    debug("main send increment delayed 4")
    reactor.action.send(MySimpleReactor.Action.IncrementDelayed)
    delay(500)

    debug("main send increment 5")
    reactor.action.send(MySimpleReactor.Action.Increment)
    delay(1000)

    coroutineContext.cancelChildren()
    debug("main end")
}
