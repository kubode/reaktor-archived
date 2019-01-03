package com.github.kubode.reaktor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

fun debug(message: String) =
    println("[${DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now())}] [${Thread.currentThread().name}] $message")

interface IReactor<ActionT, MutationT, StateT> : CoroutineScope {
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

abstract class Reactor<ActionT, MutationT, StateT>(
    final override val coroutineContext: CoroutineContext,
    initialState: StateT
) : CoroutineScope {

    interface View : CoroutineScope

    private val _state = BroadcastChannel<StateT>(Channel.CONFLATED).apply {
        offer(initialState)
    }

    private val _action: SendChannel<ActionT> = actor {
        for (action in channel) {
            launch {
                for (mutation in mutate(action)) {
                    _reducer.send(mutation)
                }
            }
        }
    }

    private val _reducer: SendChannel<MutationT> = actor {
        for (mutation in channel) {
            val newState = reduce(currentState, mutation)
            currentState = newState
            _state.offer(newState)
        }
    }

    var currentState: StateT = initialState
        private set

    val state: ReceiveChannel<StateT> = _state.openSubscription()

    val action: SendChannel<ActionT> = _action

    open fun mutate(action: ActionT): ReceiveChannel<MutationT> {
        return Channel()
    }

    open fun reduce(state: StateT, mutation: MutationT): StateT {
        return state
    }
}
