package com.odysseus.wrapper.data.repository

import com.odysseus.wrapper.core.NetworkClient
import com.odysseus.wrapper.data.remote.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

// ── helper ────────────────────────────────────────────────────────────────────

private suspend fun <T> safeCall(block: suspend () -> retrofit2.Response<T>): Result<T> = try {
    val r = block()
    if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
    else Result.failure(Exception("HTTP ${r.code()}: ${r.errorBody()?.string()}"))
} catch (e: Exception) { Result.failure(e) }

// ═══════════════════════════════════════════════════════════════════════════════
// AUTH
// ═══════════════════════════════════════════════════════════════════════════════

class AuthRepository {
    private val api get() = NetworkClient.create<AuthApiService>()

    suspend fun login(username: String, password: String) =
        safeCall { api.login(LoginRequest(username, password)) }

    suspend fun logout(): Result<GenericResponse> = safeCall { api.logout() }

    suspend fun status() = safeCall { api.status() }

    suspend fun changePassword(current: String, new: String) =
        safeCall { api.changePassword(ChangePasswordRequest(current, new)) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SESSIONS
// ═══════════════════════════════════════════════════════════════════════════════

class SessionRepository {
    private val api get() = NetworkClient.create<SessionApiService>()
    private val chatApi get() = NetworkClient.create<ChatApiService>()

    suspend fun list() = safeCall { api.list() }
    suspend fun archived() = safeCall { api.archived() }
    suspend fun create(name: String = "") = safeCall { api.create(name = name) }
    suspend fun delete(id: String) = safeCall { api.delete(id) }
    suspend fun rename(id: String, name: String) = safeCall { api.rename(id, mapOf("name" to name)) }
    suspend fun archive(id: String) = safeCall { api.archive(id) }
    suspend fun unarchive(id: String) = safeCall { api.unarchive(id) }
    suspend fun history(id: String) = safeCall { api.history(id) }
    suspend fun models() = safeCall { chatApi.models() }
    suspend fun endpoints() = safeCall { chatApi.endpoints() }
    suspend fun defaultChat() = safeCall { chatApi.defaultChat() }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHAT
// ═══════════════════════════════════════════════════════════════════════════════

class ChatRepository {
    private val api get() = NetworkClient.create<ChatApiService>()

    suspend fun send(message: String, sessionId: String, useWeb: Boolean = false) =
        safeCall { api.send(ChatRequest(message, sessionId, use_web = useWeb)) }

    suspend fun stop(sessionId: String) = safeCall { api.stop(sessionId) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// NOTES
// ═══════════════════════════════════════════════════════════════════════════════

class NotesRepository {
    private val api get() = NetworkClient.create<NotesApiService>()

    suspend fun list(type: String? = null, archived: Boolean = false) =
        safeCall { api.list(type, archived) }

    suspend fun create(req: NoteCreateRequest) = safeCall { api.create(req) }
    suspend fun get(id: Int) = safeCall { api.get(id) }
    suspend fun update(id: Int, req: NoteUpdateRequest) = safeCall { api.update(id, req) }
    suspend fun delete(id: Int) = safeCall { api.delete(id) }
    suspend fun pin(id: Int) = safeCall { api.pin(id) }
    suspend fun archive(id: Int) = safeCall { api.archive(id) }
    suspend fun toggleItem(id: Int, index: Int) = safeCall { api.toggleItem(id, index) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TASKS
// ═══════════════════════════════════════════════════════════════════════════════

class TasksRepository {
    private val api get() = NetworkClient.create<TasksApiService>()

    suspend fun list() = safeCall { api.list() }
    suspend fun create(req: TaskCreateRequest) = safeCall { api.create(req) }
    suspend fun get(id: String) = safeCall { api.get(id) }
    suspend fun update(id: String, req: TaskUpdateRequest) = safeCall { api.update(id, req) }
    suspend fun delete(id: String) = safeCall { api.delete(id) }
    suspend fun run(id: String) = safeCall { api.run(id) }
    suspend fun pause(id: String) = safeCall { api.pause(id) }
    suspend fun resume(id: String) = safeCall { api.resume(id) }
    suspend fun stop(id: String) = safeCall { api.stop(id) }
    suspend fun runs(id: String) = safeCall { api.runs(id) }
    suspend fun recentRuns() = safeCall { api.recentRuns() }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CALENDAR
// ═══════════════════════════════════════════════════════════════════════════════

class CalendarRepository {
    private val api get() = NetworkClient.create<CalendarApiService>()

    suspend fun calendars() = safeCall { api.calendars() }
    suspend fun events(from: String? = null, to: String? = null) = safeCall { api.events(from, to) }
    suspend fun createEvent(req: EventCreateRequest) = safeCall { api.createEvent(req) }
    suspend fun updateEvent(uid: String, req: EventUpdateRequest) = safeCall { api.updateEvent(uid, req) }
    suspend fun deleteEvent(uid: String) = safeCall { api.deleteEvent(uid) }
    suspend fun sync() = safeCall { api.sync() }
    suspend fun quickParse(text: String) = safeCall { api.quickParse(mapOf("text" to text)) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// EMAIL
// ═══════════════════════════════════════════════════════════════════════════════

class EmailRepository {
    private val api get() = NetworkClient.create<EmailApiService>()

    suspend fun list(folder: String = "INBOX", accountId: Int? = null, offset: Int = 0) =
        safeCall { api.list(folder, accountId, offset) }

    suspend fun read(uid: String, accountId: Int? = null) = safeCall { api.read(uid, accountId) }
    suspend fun send(req: SendEmailRequest) = safeCall { api.send(req) }
    suspend fun folders(accountId: Int? = null) = safeCall { api.folders(accountId) }
    suspend fun accounts() = safeCall { api.accounts() }
    suspend fun markRead(uid: String) = safeCall { api.markRead(uid) }
    suspend fun markUnread(uid: String) = safeCall { api.markUnread(uid) }
    suspend fun flag(uid: String) = safeCall { api.flag(uid) }
    suspend fun archive(uid: String) = safeCall { api.archiveEmail(uid) }
    suspend fun delete(uid: String) = safeCall { api.delete(uid) }
    suspend fun search(q: String, accountId: Int? = null) = safeCall { api.search(q, accountId) }
    suspend fun aiReply(uid: String, instruction: String = "") =
        safeCall { api.aiReply(mapOf("uid" to uid, "instruction" to instruction)) }
    suspend fun move(uid: String, folder: String) = safeCall { api.move(uid, mapOf("folder" to folder)) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// DOCUMENTS
// ═══════════════════════════════════════════════════════════════════════════════

class DocumentsRepository {
    private val api get() = NetworkClient.create<DocumentsApiService>()

    suspend fun library(q: String? = null) = safeCall { api.library(q) }
    suspend fun create(req: DocumentCreateRequest) = safeCall { api.create(req) }
    suspend fun get(id: String) = safeCall { api.get(id) }
    suspend fun update(id: String, req: DocumentUpdateRequest) = safeCall { api.update(id, req) }
    suspend fun delete(id: String) = safeCall { api.delete(id) }
    suspend fun archive(id: String) = safeCall { api.archive(id) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// GALLERY
// ═══════════════════════════════════════════════════════════════════════════════

class GalleryRepository {
    private val api get() = NetworkClient.create<GalleryApiService>()

    suspend fun library(q: String? = null, offset: Int = 0, albumId: String? = null) =
        safeCall { api.library(q, offset, 40, albumId) }

    suspend fun upload(file: File): Result<GalleryImage> {
        val body = file.asRequestBody("image/*".toMediaType())
        val part = MultipartBody.Part.createFormData("file", file.name, body)
        return safeCall { api.upload(part) }
    }

    suspend fun delete(id: String) = safeCall { api.delete(id) }
    suspend fun favorite(id: String) = safeCall { api.favorite(id) }
    suspend fun aiTag(id: String) = safeCall { api.aiTag(id) }
    suspend fun rename(id: String, name: String) = safeCall { api.rename(id, mapOf("name" to name)) }
    suspend fun albums() = safeCall { api.albums() }
    suspend fun createAlbum(name: String) = safeCall { api.createAlbum(mapOf("name" to name)) }
    suspend fun deleteAlbum(id: String) = safeCall { api.deleteAlbum(id) }
    suspend fun addToAlbum(albumId: String, imageIds: List<String>) =
        safeCall { api.addToAlbum(albumId, mapOf("image_ids" to imageIds)) }
    suspend fun removeFromAlbum(albumId: String, imageIds: List<String>) =
        safeCall { api.removeFromAlbum(albumId, mapOf("image_ids" to imageIds)) }
    suspend fun stats() = safeCall { api.stats() }
    suspend fun aiTagBatch(ids: List<String>) = safeCall { api.aiTagBatch(mapOf("image_ids" to ids)) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// RESEARCH
// ═══════════════════════════════════════════════════════════════════════════════

class ResearchRepository {
    private val api get() = NetworkClient.create<ResearchApiService>()

    suspend fun library() = safeCall { api.library() }
    suspend fun start(query: String, maxSources: Int = 10) =
        safeCall { api.start(ResearchStartRequest(query, maxSources)) }
    suspend fun status(id: String) = safeCall { api.status(id) }
    suspend fun cancel(id: String) = safeCall { api.cancel(id) }
    suspend fun report(id: String) = safeCall { api.report(id) }
    suspend fun archive(id: String) = safeCall { api.archive(id) }
    suspend fun delete(id: String) = safeCall { api.delete(id) }
    suspend fun active() = safeCall { api.active() }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MEMORY
// ═══════════════════════════════════════════════════════════════════════════════

class MemoryRepository {
    private val memApi get() = NetworkClient.create<MemoryApiService>()
    private val skillApi get() = NetworkClient.create<SkillsApiService>()

    suspend fun listMemories(category: String? = null) = safeCall { memApi.list(category) }
    suspend fun addMemory(text: String, category: String = "fact") =
        safeCall { memApi.add(MemoryAddRequest(text, category)) }
    suspend fun updateMemory(id: String, text: String, category: String? = null) =
        safeCall { memApi.update(id, MemoryUpdateRequest(text, category)) }
    suspend fun deleteMemory(id: String) = safeCall { memApi.delete(id) }
    suspend fun pinMemory(id: String) = safeCall { memApi.pin(id) }

    suspend fun listSkills() = safeCall { skillApi.list() }
    suspend fun createSkill(title: String, problem: String, solution: String, tags: String = "") =
        safeCall { skillApi.create(mapOf("title" to title, "problem" to problem, "solution" to solution, "tags" to tags)) }
    suspend fun deleteSkill(id: String) = safeCall { skillApi.delete(id) }
}
