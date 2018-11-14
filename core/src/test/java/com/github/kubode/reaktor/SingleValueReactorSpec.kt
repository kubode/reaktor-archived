package com.github.kubode.reaktor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.produce
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.coroutines.CoroutineContext
import kotlin.test.expect

private class SingleValueReactor(
    coroutineContext: CoroutineContext
) : Reactor<SingleValueReactor.Action, SingleValueReactor.Mutation, SingleValueReactor.State>(
    coroutineContext,
    State()
) {
    sealed class Action {
        data class UpdateValue(val value: Int) : Action()
    }

    sealed class Mutation {
        data class SetValue(val value: Int) : Mutation()
    }

    data class State(
        val value: Int = 0
    )

    override fun mutate(action: Action) = produce {
        when (action) {
            is Action.UpdateValue -> send(Mutation.SetValue(action.value))
        }
    }

    override fun reduce(state: State, mutation: Mutation): State {
        return when (mutation) {
            is Mutation.SetValue -> state.copy(
                value = mutation.value
            )
        }
    }
}

object SingleValueReactorSpec : Spek({
    val coroutineContext by memoized { Dispatchers.Unconfined }
    val reactor by memoized { SingleValueReactor(coroutineContext) }
    afterEachTest { coroutineContext.cancel() }
    describe("currentState") {
        context("after initialized") {
            it("returns 0") {
                expect(0) { reactor.currentState.value }
            }
        }
        context("after offer UpdateValue(1)") {
            before { reactor.action.offer(SingleValueReactor.Action.UpdateValue(1)) }
            it("returns 1") {
                expect(1) { reactor.currentState.value }
            }
        }
    }
})
