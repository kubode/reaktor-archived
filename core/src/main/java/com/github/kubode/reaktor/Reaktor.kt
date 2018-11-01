package com.github.kubode.reaktor

import kotlinx.coroutines.channels.ReceiveChannel

annotation class TestAnnotation

interface Reactor<ActionT, MutationT, StateT> {
    val initialState: StateT
    suspend fun mutate(action: ActionT): ReceiveChannel<MutationT>
    fun reduce(state: StateT, mutation: MutationT): StateT
}
