@file:JvmName("Main")

package com.github.kubode.reaktor.example.cli

import com.github.kubode.reaktor.Reactor
import com.github.kubode.reaktor.TestAnnotation
import com.github.kubode.reaktor.debug
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.flatMap
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

@TestAnnotation
class MyReactor : Reactor<MyReactor.Action, MyReactor.Mutation, MyReactor.State>(State()) {

    sealed class Action {
        object Increment : Action()
        object IncrementDelayed : Action()

        override fun toString() = javaClass.simpleName
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
//    val channel = produce {
//        repeat(10) {i -> send(i) }
//    }
//        .flatMap { i ->
//            produce {
//                repeat(i) { j -> send(i * 10 + j) }
//            }
//        }
//    launch {
//        channel.consumeEach { debug("consume: $it") }
//    }
//    return@runBlocking
    debug("main start")
    val reactor = MyReactor()
    launch {
        reactor.state.consumeEach { state ->
            debug("main reactor.state.consumeEach $state")
        }
    }
    debug("main send increment 1")
    reactor.action.offer(MyReactor.Action.Increment)
    delay(1000)

    debug("main send increment 2")
    reactor.action.offer(MyReactor.Action.Increment)

    debug("main send increment delayed 3")
    reactor.action.offer(MyReactor.Action.IncrementDelayed)
    delay(500)

    debug("main send increment delayed 4")
    reactor.action.offer(MyReactor.Action.IncrementDelayed)
    delay(500)

    debug("main send increment 5")
    reactor.action.offer(MyReactor.Action.Increment)
    delay(1000)

    coroutineContext.cancelChildren()
    debug("main end")
}
