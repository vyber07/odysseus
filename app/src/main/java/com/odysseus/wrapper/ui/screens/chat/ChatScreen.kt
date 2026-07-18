package com.odysseus.wrapper.ui.screens.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.odysseus.wrapper.data.remote.MessageItem
import com.odysseus.wrapper.data.remote.SessionItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel = viewModel()) {
    val sessions       by vm.sessions.collectAsStateWithLifecycle()
    val currentSession by vm.currentSession.collectAsStateWithLifecycle()
    val messages       by vm.messages.collectAsStateWithLifecycle()
    val sending        by vm.sending.collectAsStateWithLifecycle()
    val error          by vm.error.collectAsStateWithLifecycle()
    val models         by vm.models.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var useWeb by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<SessionItem?>(null) }
    var showModelPicker by remember { mutableStateOf(false) }

    // Rename dialog
    showRenameDialog?.let { session ->
        var name by remember { mutableStateOf(session.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Session") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.renameSession(session.id, name); showRenameDialog = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") } }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Chats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { vm.newSession(); scope.launch { drawerState.close() } }) {
                        Icon(Icons.Default.Add, contentDescription = "New chat")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(sessions, key = { it.id }) { session ->
                        val selected = currentSession?.id == session.id
                        NavigationDrawerItem(
                            label = { Text(session.name.ifBlank { "Untitled" }, maxLines = 1) },
                            selected = selected,
                            onClick = {
                                vm.selectSession(session)
                                scope.launch { drawerState.close() }
                            },
                            badge = {
                                Row {
                                    IconButton(onClick = { showRenameDialog = session },
                                        modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename",
                                            modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(onClick = { vm.deleteSession(session.id) },
                                        modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                                            modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(currentSession?.name?.ifBlank { "Odysseus Chat" } ?: "Odysseus Chat",
                            maxLines = 1, fontWeight = FontWeight.SemiBold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { useWeb = !useWeb }) {
                            Icon(Icons.Default.Language,
                                contentDescription = "Web search",
                                tint = if (useWeb) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                        IconButton(onClick = { vm.newSession() }) {
                            Icon(Icons.Default.Add, contentDescription = "New chat")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {

                // Error banner
                error?.let {
                    Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(it, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                            IconButton(onClick = { vm.clearError() }, Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Messages list
                val listState = rememberLazyListState()
                LaunchedEffect(messages.size) {
                    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                }

                if (messages.isEmpty() && currentSession != null) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Chat, null,
                                Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(0.2f))
                            Spacer(Modifier.height(8.dp))
                            Text("Start a conversation",
                                color = MaterialTheme.colorScheme.onBackground.copy(0.4f))
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { "${it.id}_${it.created_at}" }) { msg ->
                            ChatBubble(msg)
                        }
                        if (sending) {
                            item {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Box(Modifier.background(MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(16.dp)).padding(12.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            repeat(3) { i ->
                                                Box(Modifier.size(6.dp)
                                                    .background(MaterialTheme.colorScheme.primary.copy(0.6f),
                                                        CircleShape))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Input row
                Row(
                    Modifier.fillMaxWidth().padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message…", color = MaterialTheme.colorScheme.onSurface.copy(0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        maxLines = 5
                    )
                    if (sending) {
                        IconButton(onClick = { vm.stopGeneration() }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    vm.sendMessage(inputText.trim(), useWeb)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier.clip(CircleShape)
                                .background(if (inputText.isBlank()) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send",
                                tint = if (inputText.isBlank()) MaterialTheme.colorScheme.onSurface.copy(0.3f)
                                       else MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(message: MessageItem) {
    val isUser = message.role == "user"
    val clipboard = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(Modifier.size(32.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center) {
                Text("O", color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
        }
        Box(
            Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd   = if (isUser) 4.dp else 16.dp))
                .background(if (isUser) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface)
                .combinedClickable(onClick = {}, onLongClick = { showMenu = true })
                .padding(12.dp)
        ) {
            Text(message.content,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp, lineHeight = 22.sp)
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Copy") },
                    onClick = { clipboard.setText(AnnotatedString(message.content)); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                )
            }
        }
    }
}
