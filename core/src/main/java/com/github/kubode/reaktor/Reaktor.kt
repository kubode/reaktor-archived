package com.github.kubode.reaktor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

annotation class TestAnnotation

fun debug(message: String) =
    println("[${DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now())}] [${Thread.currentThread().name}] $message")

abstract class Reactor<ActionT, MutationT, StateT>(
    initialState: StateT,
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : CoroutineScope {

    object NoAction

    private val _action = Channel<ActionT>()
    private val _state = Channel<StateT>()

    val action: SendChannel<ActionT> = _action
    val state: ReceiveChannel<StateT> = _state

    var currentState: StateT = initialState
        private set(value) {
            field = value
            _state.offer(value)
        }

    init {
        _state.offer(initialState)
        launch {
            _action.consumeEach { action ->
                launch {
                    mutate(action).consumeEach { mutation ->
                        currentState = reduce(currentState, mutation)
                    }
                }
            }
        }
    }

    open fun mutate(action: ActionT): ReceiveChannel<MutationT> {
        return Channel()
    }

    open fun reduce(state: StateT, mutation: MutationT): StateT {
        return state
    }
}
