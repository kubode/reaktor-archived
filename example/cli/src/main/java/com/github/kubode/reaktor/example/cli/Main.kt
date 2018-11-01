@file:JvmName("Main")

package com.github.kubode.reaktor.example.cli

import com.github.kubode.reaktor.Reactor
import com.github.kubode.reaktor.TestAnnotation
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.coroutineContext

@TestAnnotation
class MyReactor : Reactor<MyReactor.Action, MyReactor.Mutation, MyReactor.State> {
    sealed class Action {
        object Increment : Action()
        object IncrementDelayed : Action()
    }

    sealed class Mutation {
        object Increment : Mutation()
    }

    data class State(
        val number: Int = 0
    )

    override val initialState = State()

    private var delayedJob: Job? = null

    override suspend fun mutate(action: Action): ReceiveChannel<Mutation> {
        return CoroutineScope(coroutineContext).produce {
            when (action) {
                is Action.Increment -> {
                    send(Mutation.Increment)
                }
                is Action.IncrementDelayed -> {
                    delayedJob?.cancel()
                    delayedJob = CoroutineScope(coroutineContext).launch {
                        delay(1000)
                        send(Mutation.Increment)
                    }
                }
            }
        }
    }

    override fun reduce(state: State, mutation: Mutation): State {
        return when (mutation) {
            is Mutation.Increment -> state.copy(
                number = state.number + 1
            )
        }
    }
}

class GeneratedMyReactor {
    val state = Channel<MyReactor.State>()
    val action = Channel<MyReactor.Action>()

}

fun main(vararg args: String) = runBlocking {
    delay(1000)
    println("completed")
}
