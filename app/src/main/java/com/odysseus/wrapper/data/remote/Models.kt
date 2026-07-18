package com.odysseus.wrapper.data.remote

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════════════════════════
// AUTH
// ═══════════════════════════════════════════════════════════════════════════════

data class LoginRequest(val username: String, val password: String, val remember: Boolean = true)
data class LoginResponse(val ok: Boolean, val username: String? = null, val requires_totp: Boolean = false)
data class AuthStatusResponse(val authenticated: Boolean, val username: String? = null, val is_admin: Boolean = false)
data class ChangePasswordRequest(val current_password: String, val new_password: String)

// ═══════════════════════════════════════════════════════════════════════════════
// SESSIONS / CHAT
// ═══════════════════════════════════════════════════════════════════════════════

data class SessionItem(
    val id: String,
    val name: String = "",
    val model: String = "",
    val endpoint_url: String = "",
    val rag: Boolean = false,
    val archived: Boolean = false,
    val created_at: Long = 0,
    val updated_at: Long = 0,
    val message_count: Int = 0
)

data class ChatRequest(
    val message: String,
    val session: String,
    val use_web: Boolean = false,
    val use_research: Boolean = false,
    val attachments: List<String> = emptyList()
)

data class ChatResponse(val response: String)

data class MessageItem(
    val id: Int = 0,
    val role: String = "",       // "user" | "assistant"
    val content: String = "",
    val created_at: Long = 0,
    val model: String? = null,
    val attachments: List<AttachmentRef>? = null
)

data class AttachmentRef(val id: String, val name: String, val mime: String)

data class ModelItem(
    val id: String,
    val name: String = "",
    val provider: String = "",
    val endpoint_url: String = ""
)

data class EndpointItem(
    val id: String,
    val label: String = "",
    val base_url: String = "",
    val model: String = "",
    val is_local: Boolean = false,
    val is_default: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// NOTES
// ═══════════════════════════════════════════════════════════════════════════════

data class NoteItem(
    val id: Int = 0,
    val title: String = "",
    val content: String? = null,
    val items: List<NoteItem_CheckItem>? = null,
    val note_type: String = "note",   // "note" | "todo" | "reminder"
    val color: String? = null,
    val label: String? = null,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val due_date: String? = null,
    val repeat: String? = "none",
    val created_at: String = "",
    val updated_at: String = ""
)

data class NoteItem_CheckItem(
    val text: String = "",
    val done: Boolean = false
)

data class NoteCreateRequest(
    val title: String = "",
    val content: String? = null,
    val items: List<NoteItem_CheckItem>? = null,
    val note_type: String = "note",
    val color: String? = null,
    val pinned: Boolean = false,
    val due_date: String? = null
)

data class NoteUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val items: List<NoteItem_CheckItem>? = null,
    val note_type: String? = null,
    val color: String? = null,
    val pinned: Boolean? = null,
    val archived: Boolean? = null,
    val due_date: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// TASKS (Scheduled)
// ═══════════════════════════════════════════════════════════════════════════════

data class TaskItem(
    val id: String = "",
    val name: String = "",
    val prompt: String? = null,
    val task_type: String = "llm",
    val action: String? = null,
    val schedule: String? = null,
    val scheduled_time: String = "09:00",
    val scheduled_day: Int? = null,
    val scheduled_date: String? = null,
    val cron_expression: String? = null,
    val trigger_type: String = "schedule",
    val trigger_event: String? = null,
    val output_target: String = "session",
    val model: String? = null,
    val endpoint_url: String? = null,
    val paused: Boolean = false,
    val last_run: String? = null,
    val last_run_status: String? = null,
    val notifications_enabled: Boolean? = null
)

data class TaskCreateRequest(
    val name: String? = null,
    val prompt: String? = null,
    val task_type: String = "llm",
    val schedule: String? = null,
    val scheduled_time: String = "09:00",
    val scheduled_day: Int? = null,
    val scheduled_date: String? = null,
    val cron_expression: String? = null,
    val trigger_type: String = "schedule",
    val output_target: String = "session",
    val model: String? = null,
    val endpoint_url: String? = null,
    val notifications_enabled: Boolean? = null
)

data class TaskUpdateRequest(
    val name: String? = null,
    val prompt: String? = null,
    val schedule: String? = null,
    val scheduled_time: String? = null,
    val paused: Boolean? = null,
    val model: String? = null
)

data class TaskRun(
    val id: Int = 0,
    val task_id: String = "",
    val started_at: String = "",
    val finished_at: String? = null,
    val status: String = "",
    val result: String? = null,
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// CALENDAR
// ═══════════════════════════════════════════════════════════════════════════════

data class CalendarItem(
    val id: String = "",
    val name: String = "",
    val color: String? = null,
    val is_default: Boolean = false,
    val source: String = "local"
)

data class CalendarEvent(
    val uid: String = "",
    val summary: String = "",
    val dtstart: String = "",
    val dtend: String? = null,
    val all_day: Boolean = false,
    val description: String = "",
    val location: String = "",
    val rrule: String? = null,
    val color: String? = null,
    val calendar_id: String? = null,
    val calendar_name: String? = null
)

data class EventCreateRequest(
    val summary: String,
    val dtstart: String,
    val dtend: String? = null,
    val all_day: Boolean = false,
    val description: String = "",
    val location: String = "",
    val calendar_href: String? = null,
    val rrule: String? = null,
    val color: String? = null
)

data class EventUpdateRequest(
    val summary: String? = null,
    val dtstart: String? = null,
    val dtend: String? = null,
    val all_day: Boolean? = null,
    val description: String? = null,
    val location: String? = null,
    val rrule: String? = null,
    val color: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// EMAIL
// ═══════════════════════════════════════════════════════════════════════════════

data class EmailItem(
    val uid: String = "",
    val subject: String = "",
    val sender: String = "",
    val sender_name: String = "",
    val date: String = "",
    val snippet: String = "",
    val is_read: Boolean = false,
    val is_flagged: Boolean = false,
    val has_attachments: Boolean = false,
    val folder: String = "INBOX",
    val account_id: Int = 0
)

data class EmailDetail(
    val uid: String = "",
    val subject: String = "",
    val sender: String = "",
    val sender_name: String = "",
    val to: List<String> = emptyList(),
    val cc: List<String> = emptyList(),
    val date: String = "",
    val body_html: String = "",
    val body_text: String = "",
    val is_read: Boolean = false,
    val is_flagged: Boolean = false,
    val attachments: List<EmailAttachment> = emptyList()
)

data class EmailAttachment(
    val index: Int = 0,
    val filename: String = "",
    val content_type: String = "",
    val size: Int = 0
)

data class SendEmailRequest(
    val account_id: Int,
    val to: String,
    val subject: String,
    val body: String,
    val cc: String = "",
    val reply_to_uid: String? = null
)

data class EmailFolder(val name: String, val full_name: String, val unread_count: Int = 0)

data class EmailAccountItem(
    val id: Int = 0,
    val label: String = "",
    val email: String = "",
    val imap_host: String = "",
    val smtp_host: String = "",
    val is_default: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// DOCUMENTS
// ═══════════════════════════════════════════════════════════════════════════════

data class DocumentItem(
    val id: String = "",
    val title: String = "",
    val content_type: String = "markdown",
    val created_at: String = "",
    val updated_at: String = "",
    val word_count: Int = 0,
    val is_archived: Boolean = false,
    val session_id: String? = null
)

data class DocumentDetail(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val content_type: String = "markdown",
    val created_at: String = "",
    val updated_at: String = ""
)

data class DocumentCreateRequest(
    val title: String,
    val content: String = "",
    val content_type: String = "markdown"
)

data class DocumentUpdateRequest(
    val title: String? = null,
    val content: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// GALLERY
// ═══════════════════════════════════════════════════════════════════════════════

data class GalleryImage(
    val id: String = "",
    val filename: String = "",
    val url: String = "",
    val thumbnail_url: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val size: Int = 0,
    val created_at: String = "",
    val tags: List<String> = emptyList(),
    val ai_tags: List<String> = emptyList(),
    val is_favorite: Boolean = false,
    val album_ids: List<String> = emptyList()
)

data class GalleryAlbum(
    val id: String = "",
    val name: String = "",
    val cover_url: String? = null,
    val image_count: Int = 0,
    val created_at: String = ""
)

data class GalleryStats(
    val total_images: Int = 0,
    val total_size_bytes: Long = 0,
    val albums_count: Int = 0
)

// ═══════════════════════════════════════════════════════════════════════════════
// RESEARCH
// ═══════════════════════════════════════════════════════════════════════════════

data class ResearchStartRequest(
    val query: String,
    val max_sources: Int = 10,
    val mode: String = "standard"
)

data class ResearchSession(
    val id: String = "",
    val query: String = "",
    val status: String = "",      // "running" | "done" | "error" | "cancelled"
    val created_at: String = "",
    val completed_at: String? = null,
    val sources_count: Int = 0,
    val is_archived: Boolean = false
)

data class ResearchStatus(
    val session_id: String = "",
    val status: String = "",
    val progress: Int = 0,
    val message: String = "",
    val sources_found: Int = 0
)

data class ResearchReport(
    val session_id: String = "",
    val query: String = "",
    val report: String = "",
    val sources: List<ResearchSource> = emptyList(),
    val created_at: String = ""
)

data class ResearchSource(
    val url: String = "",
    val title: String = "",
    val snippet: String = ""
)

// ═══════════════════════════════════════════════════════════════════════════════
// MEMORY
// ═══════════════════════════════════════════════════════════════════════════════

data class MemoryItem(
    val id: String = "",
    val text: String = "",
    val category: String = "fact",
    val source: String = "user",
    val timestamp: Long = 0,
    val session_id: String? = null,
    val pinned: Boolean = false
)

data class MemoryAddRequest(
    val text: String,
    val category: String = "fact",
    val source: String = "user"
)

data class MemoryUpdateRequest(
    val text: String,
    val category: String? = null
)

data class SkillItem(
    val id: String = "",
    val title: String = "",
    val problem: String = "",
    val solution: String = "",
    val tags: List<String> = emptyList(),
    val status: String = "approved",
    val created_at: String = ""
)

// ═══════════════════════════════════════════════════════════════════════════════
// GENERIC
// ═══════════════════════════════════════════════════════════════════════════════

data class GenericResponse(val ok: Boolean = true, val message: String? = null)
data class IdResponse(val id: String)
data class UploadResponse(
    val id: String,
    val name: String,
    val mime: String,
    val size: Int,
    val url: String? = null
)
