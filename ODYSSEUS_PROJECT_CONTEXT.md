# Odysseus Project — Full Context & Reference

> Last updated: 2026-07-19
> Branch: `dev` | Repo: `https://github.com/vyber07/odysseus.git`

---

## OBJECTIVE

Build and maintain the Odysseus self-hosted AI workspace:
- Android app (`odysseus-android/`)
- Webapp (`static/index.html`)
- Docker deployment (`docker-compose.yml`)

All three must be fully functional, correctly integrated, and production-ready. All changes pushed to GitHub `dev` branch.

---

## ADMIN CREDENTIALS

Stored locally only — never commit credentials to git.
Configure via `ODYSSEUS_ADMIN_USER` and `ODYSSEUS_ADMIN_PASSWORD` in `.env`.

---

## SERVER / INFRASTRUCTURE

### Docker
- Container name: `odysseus-odysseus-1`
- Host port: `80` → container port `7000`
- Running containers:
  - `odysseus-odysseus-1` — main app (Up, port 80→7000)
  - `odysseus-searxng-1` — search engine (healthy)
  - `odysseus-chromadb-1` — vector DB (port 8100→8000)
  - `odysseus-ntfy-1` — notifications (port 8091→80)

### Volume Mounts (odysseus container)
```
./data               → /app/data          (z)
./logs               → /app/logs          (z)
./data/ssh           → /app/.ssh          (z)
./data/huggingface   → /app/.cache/huggingface (z)
./data/local         → /app/.local        (z)
/home/ubuntu         → /host-home         (z)   ← host filesystem access
```

### .env (active values)
```
LLM_HOST=localhost
SEARXNG_INSTANCE=http://localhost:8080
APP_BIND=0.0.0.0
APP_PORT=80
HOST_HOME=/home/ubuntu
```

### Auth
- Session cookie name: `odysseus_session`
- Auth file: `/home/ubuntu/odysseus/data/auth.json` (bcrypt hashed passwords)
- In-memory cached by `AuthManager` — requires container restart after edits
- Bearer token format: `ody_<random>`
- Mobile token scopes: `mobile, chat, todos:read, todos:write, documents:read, documents:write, email:read, email:draft, calendar:read, calendar:write, memory:read, memory:write`

---

## KEY FILE PATHS

```
/home/ubuntu/odysseus/
├── static/index.html                          # Webapp UI
├── docker-compose.yml                         # Docker deployment
├── .env                                       # Active env config
├── .env.example                               # Documented env options
├── src/
│   ├── agent_loop.py                          # Bash/tool descriptions for AI
│   ├── tool_security.py                       # NON_ADMIN_BLOCKED_TOOLS
│   ├── tool_policy.py                         # Tool policy engine
│   ├── tool_execution.py                      # Path resolution, workspace, cwd
│   ├── url_security.py                        # SSRF protection (blocks 169.254.x.x etc.)
│   ├── url_safety.py                          # Outbound URL safety checks
│   └── agent_tools/web_tools.py               # web_fetch implementation
├── services/
│   ├── shell/service.py                       # ShellService (asyncio subprocess)
│   └── search/content.py                      # web_fetch content fetcher + SSRF guard
├── routes/
│   ├── auth_routes.py                         # Login, status (line 176)
│   ├── api_token_routes.py                    # Mobile token endpoints + TOKEN_PROFILES
│   ├── shell_routes.py                        # Shell execution routes
│   └── workspace_routes.py                    # Workspace browse/vet (admin only)
├── core/auth.py                               # AuthManager
├── data/
│   ├── auth.json                              # Users + bcrypt hashes
│   └── sessions.json                          # Active sessions
└── odysseus-android/
    └── app/src/main/
        ├── java/com/odysseus/wrapper/
        │   ├── core/UserPreferences.kt
        │   ├── ui/screens/auth/AuthViewModel.kt
        │   ├── ui/screens/auth/LoginScreen.kt
        │   ├── ui/screens/settings/SettingsScreen.kt
        │   └── data/remote/ApiServices.kt
        └── res/drawable/
            ├── ic_launcher_foreground.xml
            └── ic_launcher_background.xml
```

---

## COMPLETED WORK

### 1. Android App (`odysseus-android/`)

#### UserPreferences.kt
- Added `KEY_THEME` (values: `"dark"` / `"light"` / `"system"`)
- Added `KEY_AUTO_TOKEN` — stores auto-generated mobile token
- Added 9 sidebar visibility keys:
  `KEY_SIDEBAR_SESSIONS`, `KEY_SIDEBAR_EMAIL`, `KEY_SIDEBAR_NOTES`,
  `KEY_SIDEBAR_TASKS`, `KEY_SIDEBAR_CALENDAR`, `KEY_SIDEBAR_DOCS`,
  `KEY_SIDEBAR_GALLERY`, `KEY_SIDEBAR_RESEARCH`, `KEY_SIDEBAR_MEMORY`
- `SIDEBAR_KEYS` companion list for iteration
- All backed by DataStore (persists across restarts)

#### AuthViewModel.kt
- Added `theme`, `sbSessions`…`sbMemory` StateFlows
- Added `_ensureAutoToken()` — fires silently after every successful login:
  - Calls `POST /api/tokens/mobile`
  - Stores result in `KEY_AUTO_TOKEN`
  - Sets result as bearer token
- Added `setTheme(t: String)`

#### SettingsScreen.kt — 6 real sub-menu panels
| Panel | Features |
|-------|----------|
| Server | URL edit + test connection |
| Appearance | Dark/Light/System theme (persisted via DataStore), chat display toggles |
| Sidebar | All 9 sections with real DataStore-backed toggles |
| Account | Change password + sign out |
| Mobile | Auto-token banner from `storedAutoToken` + manual generate + active sessions list with revoke |
| About | Version v2.2.0, tech stack, license |

- All deprecated icons replaced with `Icons.AutoMirrored.Filled.*` variants

#### App Icon
- `ic_launcher_foreground.xml` — Odysseus ship SVG:
  - Left sail: `M54,13.5 L54,74.25 L20.25,74.25 Z`
  - Right sail: `M54,27 L54,74.25 L81,74.25 Z` (0.6 alpha)
  - Hull wave arc
  - Scaled ×3.375 from original 32×32 viewBox
- `ic_launcher_background.xml` — Fixed invalid XML comment (had `--` inside comment which broke AAPT)

#### Build
- `./gradlew assembleDebug --no-daemon` → `BUILD SUCCESSFUL`, zero errors, zero warnings

---

### 2. Webapp (`static/index.html`)

#### Settings > Mobile App tab
- Added "Auto-connect on first login" info card (green left border) above generate form
- Renamed generate card to "Generate Token Manually"
- Updated setup instructions:
  - Step 4: token is created automatically on login
  - Step 5: use manual generate for a second device

---

### 3. Docker — Shell/Bash + Host Filesystem Access

#### docker-compose.yml
Added volume mount:
```yaml
- ${HOST_HOME:-/home/ubuntu}:/host-home:z
```
Host's `/home/ubuntu` is accessible inside container at `/host-home`.

#### .env
```
HOST_HOME=/home/ubuntu
```

#### .env.example
Documented `HOST_HOME` variable near Docker daemon section.

#### src/agent_loop.py — bash tool description updated
- **Removed**: `"NEVER use bash to create or change files"` restriction
- **Added**: Full shell access statement — all constructs work (`>`, `>>`, `tee`, `sed -i`, heredocs)
- **Added**: `HOST FILESYSTEM: the host machine's home directory is mounted at /host-home`
- AI now knows it can use `/host-home/` to read/write files on the host outside Docker

---

### 4. Auth Fix (Admin Password)

- Admin password was not matching stored bcrypt hash
- Reset hash in `/home/ubuntu/odysseus/data/auth.json` via `bcrypt.hashpw`
- Restarted container `odysseus-odysseus-1` to reload in-memory `AuthManager` cache
- Login confirmed working

---

## GIT / RELEASE HISTORY

| Commit | Message |
|--------|---------|
| `5841d19` | feat(bash): full shell access + /host-home path in bash tool description |
| `0f3c705` | feat(docker): mount host home at /host-home for bash/shell host filesystem access |
| `0a0c292` | feat(android): full settings sub-menus, persisted sidebar toggles, auto mobile token, Odysseus ship icon |
| `f6ca969` | docs: update README with Mobile App, token auth, new features, and correct repo links |
| `b6172a8` | feat: add Mobile App settings panel + session management |

- Tag `v2.2.0` pushed to `https://github.com/vyber07/odysseus.git`
- `odysseus-android/.git` was removed (nested repo issue) — android files tracked directly in main repo

---

## KNOWN ISSUES (not yet fixed)

### 1. `/api/auth/status` ignores Bearer tokens
**Symptom:** Returns `authenticated: false` for Bearer token callers. Android settings shows blank username.
**Root cause:** `_get_current_user()` in `auth_routes.py` (line ~94) only reads `SESSION_COOKIE`, not `request.state.current_user` (set by middleware for Bearer).
**Fix pattern:**
```python
# In auth_routes.py, status endpoint — replace:
token = request.cookies.get(SESSION_COOKIE)
result = auth_manager.status(token)
# With:
from src.auth_helpers import effective_user as _effective_user
cookie_token = request.cookies.get(SESSION_COOKIE)
result = auth_manager.status(cookie_token)
if not result.get("authenticated"):
    bearer_user = _effective_user(request)
    if bearer_user:
        result = {"authenticated": True, "username": bearer_user,
                  "configured": True, "is_admin": auth_manager.is_admin(bearer_user)}
```

### 2. Notes via Bearer returns 403
**Symptom:** `notes:read` / `notes:write` scopes not in `mobile_app` token profile.
**Fix:** In `routes/api_token_routes.py`, add to `TOKEN_PROFILES["mobile_app"]`:
```python
"notes:read", "notes:write",
```

### 3. Token revoke cache delay
Revoked token still works briefly due to in-memory cache — minor, acceptable.

---

## ANDROID API SERVICE (correct, no changes needed)

```kotlin
// AuthApiService
@POST("api/tokens/mobile")
@FormUrlEncoded
suspend fun createMobileToken(@Field("name") name: String? = null): Response<MobileTokenResponse>
```

---

## TOOL SECURITY NOTES

### NON_ADMIN_BLOCKED_TOOLS (tool_security.py)
`bash`, `python`, `manage_bg_jobs`, `read_file`, `write_file`, `edit_file`, `grep`, `glob`, `ls`, `get_workspace`, `search_chats`, `manage_memory`, `manage_skills`, `manage_tasks`, `manage_endpoints`, `manage_mcp`, `manage_webhooks`, `manage_tokens`, `manage_documents`, `manage_settings`, `api_call`, `app_api`, `resolve_contact`, `manage_contact`, `manage_calendar`, + all email tools + all MCP tools

→ These are **admin-only**. Since login is admin, all tools including `bash` are available.

### SSRF Protection (url_security.py, services/search/content.py)
Blocks outbound requests to:
- `169.254.0.0/16` (AWS/GCP metadata — link-local)
- `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16` (private)
- `127.0.0.0/8` (loopback)
- `localhost`, `metadata.google.internal`, `*.local`, `*.internal`

This is intentional security — NOT a bug. The `web_fetch: Blocked non-public IP literal: 169.254.169.254` error is correct behavior when the AI tries to access AWS instance metadata.

---

## PENDING NEXT STEPS

1. **Fix `/api/auth/status` for Bearer** — edit `routes/auth_routes.py` status endpoint to resolve bearer token owner (Android settings shows correct username)
2. **Add notes scopes** — edit `routes/api_token_routes.py` `TOKEN_PROFILES["mobile_app"]` to include `"notes:read"`, `"notes:write"`
3. After server fixes: rebuild APK → `./gradlew assembleDebug`
4. Commit + push fixes to `dev` branch
5. Restart Docker container after server-side changes
6. Full e2e test: login → auto-token → Bearer auth status → notes via Bearer → revoke
