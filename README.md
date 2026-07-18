# Odysseus Android

Native Android client for [Odysseus](https://github.com/pewdiepie-archdaemon/odysseus) — a self-hosted AI workspace.

## Features

Full feature parity with the web app — all modules work natively on Android:

| Module | What you can do |
|---|---|
| **Chat** | Multi-session AI chat, web search toggle, session management |
| **Notes** | Create/edit notes, todos with checkboxes, reminders, pin, archive, color labels |
| **Tasks** | Schedule AI tasks (daily/weekly/cron), run manually, view run history |
| **Calendar** | View/create/edit events, recurring events, CalDAV sync |
| **Email** | Inbox, folders, read/compose/reply, AI-generated reply, flag/archive/delete |
| **Documents** | Full document editor (markdown/html/plain), library, archive |
| **Gallery** | Image grid, upload from phone, albums, AI auto-tagging, favorites |
| **Research** | Start deep web research, live progress, full report with sources |
| **Memory** | Browse/add/delete memories by category, Skills management |
| **Settings** | Server URL, test connection, dark mode, change password, logout |

## Requirements

- Android 8.0+ (API 26+)
- Odysseus server running on AWS EC2 (or any public server)
- Port 80 open in EC2 Security Group (inbound from 0.0.0.0/0)

## Build

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 (bundled with Android Studio)

### Steps

```bash
git clone https://github.com/YOUR_USERNAME/odysseus-android.git
cd odysseus-android
```

Open in Android Studio → wait for Gradle sync → **Run ▶** on your device or emulator.

> No API keys, no `.env` file, no secrets needed. The app is configured at runtime.

## First Launch

On first launch you will see the **Server Setup** screen:

1. Enter your Odysseus server URL — e.g. `http://YOUR_EC2_PUBLIC_IP/`
2. Tap **Test Connection** to verify the server is reachable
3. Tap **Continue to Login**
4. Login with your Odysseus username and password

The URL is saved on device. You can change it later in **Settings**.

## EC2 Server Setup

Your Odysseus Docker stack must be running and accessible:

```bash
# On your EC2 instance
git clone https://github.com/pewdiepie-archdaemon/odysseus.git
cd odysseus
cp .env.example .env
# Edit .env as needed
docker compose up -d --build
```

**Security Group inbound rules required:**

| Type | Port | Source |
|------|------|--------|
| HTTP | 80   | 0.0.0.0/0 (anywhere) |
| SSH  | 22   | Your IP only |

## Architecture

```
app/
├── core/
│   ├── NetworkClient.kt      # Retrofit + OkHttp + cookie jar
│   └── UserPreferences.kt    # DataStore (server URL, login state)
├── data/
│   ├── remote/
│   │   ├── Models.kt         # All request/response data classes
│   │   └── ApiServices.kt    # All Retrofit service interfaces
│   └── repository/
│       └── Repositories.kt   # All repository classes
└── ui/
    ├── screens/
    │   ├── auth/             # FirstRunScreen, LoginScreen, AuthViewModel
    │   ├── chat/             # ChatScreen, ChatViewModel
    │   ├── notes/            # NotesScreen, NotesViewModel
    │   ├── tasks/            # TasksScreen, TasksViewModel
    │   ├── calendar/         # CalendarScreen, CalendarViewModel
    │   ├── email/            # EmailScreen, EmailViewModel
    │   ├── documents/        # DocumentsScreen, DocumentsViewModel
    │   ├── gallery/          # GalleryScreen, GalleryViewModel
    │   ├── research/         # ResearchScreen, ResearchViewModel
    │   ├── memory/           # MemoryScreen, MemoryViewModel
    │   └── settings/         # SettingsScreen
    ├── components/           # Shared composables (LoadingBox, ErrorBox, etc.)
    └── theme/                # Odysseus dark/light color palette
```

**Tech stack:** Kotlin · Jetpack Compose · Material 3 · Retrofit · OkHttp · DataStore · Coil · Navigation Compose

## Auth

Authentication is **cookie-based** — identical to the web browser. The `odysseus_session` cookie is stored in memory and sent automatically with every request. No bearer tokens, no API keys.

## Contributing

PRs welcome. To add a new feature from the Odysseus web API:
1. Add request/response models to `Models.kt`
2. Add the Retrofit interface to `ApiServices.kt`
3. Add a repository method to `Repositories.kt`
4. Add a ViewModel + Screen under `ui/screens/`
5. Register the route in `MainActivity.kt`

## License

AGPL-3.0-or-later — same as Odysseus server.
