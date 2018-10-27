package com.github.kubode.reaktor

import kotlinx.coroutines.experimental.channels.Channel

annotation class LiveDataState

interface Reactor<ActionT, MutationT, StateT> {
    interface View

    val initialState: StateT
    suspend fun mutate(channel: Channel<MutationT>, action: ActionT)
    fun reduce(state: StateT, mutation: MutationT): StateT
}
