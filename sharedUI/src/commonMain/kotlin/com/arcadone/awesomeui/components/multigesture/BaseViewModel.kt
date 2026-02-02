package com.arcadone.awesomeui.components.multigesture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface UiEvent
interface UiState
interface UiEffect

abstract class BaseViewModel<Event : UiEvent, State : UiState, Effect : UiEffect> : ViewModel() {
    private val initialState: State by lazy { createInitialState() }
    abstract fun createInitialState(): State

    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    @Suppress("ktlint:standard:backing-property-naming")
    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()

    private val _effect: MutableSharedFlow<Effect> = MutableSharedFlow()
    val effect = _effect.asSharedFlow()

    init {
        subscribeEvents()
    }

    fun setEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    protected fun setState(reduce: State.() -> State) {
        _uiState.update(reduce)
    }

    protected fun setEffect(builder: () -> Effect) {
        viewModelScope.launch { _effect.emit(builder()) }
    }

    private fun subscribeEvents() {
        viewModelScope.launch {
            _event.collect { handleEvent(it) }
        }
    }

    abstract fun handleEvent(event: Event)
}
