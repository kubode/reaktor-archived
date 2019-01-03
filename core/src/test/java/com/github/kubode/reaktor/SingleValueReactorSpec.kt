package com.github.kubode.reaktor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.coroutines.CoroutineContext
import kotlin.test.expect

private class SingleValueSimpleReactor(
    coroutineContext: CoroutineContext
) : SimpleReactor<SingleValueSimpleReactor.Action, SingleValueSimpleReactor.Mutation, SingleValueSimpleReactor.State>(
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
    val reactor by memoized { SingleValueSimpleReactor(coroutineContext) }
    afterEachTest { coroutineContext.cancel() }
    describe("currentState") {
        context("after initialized") {
            it("returns 0") {
                expect(0) { reactor.currentState.value }
            }
        }
        context("after offer UpdateValue(1)") {
            before { reactor.action.offer(SingleValueSimpleReactor.Action.UpdateValue(1)) }
            it("returns 1") {
                expect(1) { reactor.currentState.value }
            }
        }
    }
    describe("state") {
        context("after initialized") {
            it("returns 0") {
                expect(0) { runBlocking { reactor.state.receive().value } }
            }
        }
        context("after offer UpdateValue(1)") {
            before { reactor.action.offer(SingleValueSimpleReactor.Action.UpdateValue(1)) }
            it("returns 1") {
                expect(1) { runBlocking { reactor.state.receive().value } }
            }
        }
        context("when offer some action") {
            it("receives list of changes") {
                val results = runBlocking {
                    val results = mutableListOf<Int>()
                    launch {
                        reactor.state.consumeEach { results += it.value }
                    }
                    launch {
                        reactor.action.send(SingleValueSimpleReactor.Action.UpdateValue(1))
                        reactor.action.send(SingleValueSimpleReactor.Action.UpdateValue(2))
                    }.join()
                    this.coroutineContext.cancelChildren()
                    results
                }
                expect(listOf(0, 1, 2)) { results }
            }
        }
    }
})
