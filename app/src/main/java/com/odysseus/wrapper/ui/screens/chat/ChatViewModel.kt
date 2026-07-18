package com.odysseus.wrapper.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.odysseus.wrapper.data.remote.*
import com.odysseus.wrapper.data.repository.ChatRepository
import com.odysseus.wrapper.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val sessionRepo = SessionRepository()
    private val chatRepo    = ChatRepository()

    // ── Sessions list ─────────────────────────────────────────────────────
    private val _sessions   = MutableStateFlow<List<SessionItem>>(emptyList())
    val sessions: StateFlow<List<SessionItem>> = _sessions

    private val _sessionsLoading = MutableStateFlow(false)
    val sessionsLoading: StateFlow<Boolean> = _sessionsLoading

    // ── Active session ────────────────────────────────────────────────────
    private val _currentSession = MutableStateFlow<SessionItem?>(null)
    val currentSession: StateFlow<SessionItem?> = _currentSession

    // ── Messages ──────────────────────────────────────────────────────────
    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages

    // ── Models / endpoints ────────────────────────────────────────────────
    private val _models    = MutableStateFlow<List<ModelItem>>(emptyList())
    val models: StateFlow<List<ModelItem>> = _models

    private val _endpoints = MutableStateFlow<List<EndpointItem>>(emptyList())
    val endpoints: StateFlow<List<EndpointItem>> = _endpoints

    // ── UI state ──────────────────────────────────────────────────────────
    private val _sending  = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending

    private val _error    = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadSessions()
        loadModels()
    }

    fun loadSessions() = viewModelScope.launch {
        _sessionsLoading.value = true
        sessionRepo.list().fold(
            onSuccess = {
                _sessions.value = it
                if (_currentSession.value == null && it.isNotEmpty()) selectSession(it.first())
            },
            onFailure = { _error.value = it.message }
        )
        _sessionsLoading.value = false
    }

    fun selectSession(session: SessionItem) {
        _currentSession.value = session
        loadHistory(session.id)
    }

    private fun loadHistory(sessionId: String) = viewModelScope.launch {
        sessionRepo.history(sessionId).fold(
            onSuccess  = { _messages.value = it },
            onFailure  = { _error.value = it.message }
        )
    }

    fun newSession(name: String = "") = viewModelScope.launch {
        sessionRepo.create(name).fold(
            onSuccess = { session ->
                _sessions.value = listOf(session) + _sessions.value
                selectSession(session)
            },
            onFailure = { _error.value = it.message }
        )
    }

    fun renameSession(id: String, name: String) = viewModelScope.launch {
        sessionRepo.rename(id, name).fold(
            onSuccess = { loadSessions() },
            onFailure = { _error.value = it.message }
        )
    }

    fun deleteSession(id: String) = viewModelScope.launch {
        sessionRepo.delete(id).fold(
            onSuccess = {
                val updated = _sessions.value.filter { it.id != id }
                _sessions.value = updated
                if (_currentSession.value?.id == id) {
                    _messages.value = emptyList()
                    _currentSession.value = updated.firstOrNull()
                    updated.firstOrNull()?.let { loadHistory(it.id) }
                }
            },
            onFailure = { _error.value = it.message }
        )
    }

    fun archiveSession(id: String) = viewModelScope.launch {
        sessionRepo.archive(id).fold(
            onSuccess = { loadSessions() },
            onFailure = { _error.value = it.message }
        )
    }

    fun sendMessage(text: String, useWeb: Boolean = false) = viewModelScope.launch {
        val sid = _currentSession.value?.id ?: run {
            _error.value = "No active session"; return@launch
        }
        // Optimistically add user message
        val tempUser = MessageItem(id = -1, role = "user", content = text,
            created_at = System.currentTimeMillis())
        _messages.value = _messages.value + tempUser
        _sending.value = true
        chatRepo.send(text, sid, useWeb).fold(
            onSuccess = { resp ->
                val aiMsg = MessageItem(id = -2, role = "assistant", content = resp.response,
                    created_at = System.currentTimeMillis())
                _messages.value = _messages.value + aiMsg
            },
            onFailure = { _error.value = it.message }
        )
        _sending.value = false
    }

    fun stopGeneration() = viewModelScope.launch {
        _currentSession.value?.id?.let { chatRepo.stop(it) }
        _sending.value = false
    }

    private fun loadModels() = viewModelScope.launch {
        sessionRepo.models().fold(onSuccess = { _models.value = it }, onFailure = {})
        sessionRepo.endpoints().fold(onSuccess = { _endpoints.value = it }, onFailure = {})
    }

    fun clearError() { _error.value = null }
}
