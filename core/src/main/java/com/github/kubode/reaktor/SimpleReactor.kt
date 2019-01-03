package com.github.kubode.reaktor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

fun debug(message: String) =
    println("[${DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now())}] [${Thread.currentThread().name}] $message")

interface Reactor<ActionT, MutationT, StateT> : CoroutineScope {
    val action: SendChannel<ActionT>
    val initialState: StateT
    val currentState: StateT
    val state: ReceiveChannel<StateT>
    fun transformAction(action: ReceiveChannel<ActionT>): ReceiveChannel<ActionT> = action
    fun mutate(action: ActionT): ReceiveChannel<MutationT> = Channel()
    fun transformMutation(mutation: ReceiveChannel<MutationT>): ReceiveChannel<MutationT> = mutation
    fun reduce(state: StateT, mutation: MutationT): StateT = state
    fun transformState(state: ReceiveChannel<StateT>): ReceiveChannel<StateT> = state
}

class ReactorHelper<ActionT, MutationT, StateT>(
    override val coroutineContext: CoroutineContext,
    override val initialState: StateT
) : Reactor<ActionT, MutationT, StateT> {

    private val _action: SendChannel<ActionT> = actor {
        for (action in channel) {
            launch {
                for (mutation in mutate(action)) {
                    _reducer.send(mutation)
                }
            }
        }
    }

    private val _state: BroadcastChannel<StateT> = BroadcastChannel<StateT>(Channel.CONFLATED)
        .apply { offer(initialState) }

    private val _reducer: SendChannel<MutationT> = actor {
        for (mutation in channel) {
            val newState = reduce(currentState, mutation)
            currentState = newState
            _state.offer(newState)
        }
    }

    override val action: SendChannel<ActionT> = _action
    override var currentState: StateT = initialState
        private set
    override val state: ReceiveChannel<StateT> = _state.openSubscription()
}

abstract class SimpleReactor<ActionT, MutationT, StateT>(
    final override val coroutineContext: CoroutineContext,
    final override val initialState: StateT
) : Reactor<ActionT, MutationT, StateT> by ReactorHelper<ActionT, MutationT, StateT>(
    coroutineContext,
    initialState
)

class FooReactor(
    override val coroutineContext: CoroutineContext
) : Reactor<FooReactor.Action, FooReactor.Mutation, FooReactor.State> {

    private val helper = ReactorHelper<Action, Mutation, State>(coroutineContext, State())
    override val action = helper.action
    override val initialState = helper.initialState
    override val currentState: State
        get() = helper.currentState
    override val state = helper.state

    sealed class Action {
        object IncrementValue : Action()
    }

    sealed class Mutation {
        data class SetValue(val value: Int) : Mutation()
    }

    data class State(
        val value: Int = 0
    )

    override fun mutate(action: Action) = produce<Mutation> {
        when (action) {
            is Action.IncrementValue -> Mutation.SetValue(currentState.value + 1)
        }
    }

    override fun reduce(state: State, mutation: Mutation): State {
        return when (mutation) {
            is Mutation.SetValue -> state.copy(value = mutation.value)
        }
    }
}

fun main() = runBlocking {
    val reactor = FooReactor(coroutineContext)
    launch(coroutineContext + Dispatchers.Default) {
        reactor.state.consumeEach { state ->
            debug("main reactor.state.consumeEach $state")
        }
    }
    reactor.action.send(FooReactor.Action.IncrementValue)
}
