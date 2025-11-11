package com.easy.flowbalance.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easy.flowbalance.data.FlowSession
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    sessions: List<FlowSession>,
    onQuickCapture: () -> Unit,
    onCreate: (String, Int, Int, Double) -> Unit,
    onEdit: (FlowSession) -> Unit,
    onDelete: (FlowSession) -> Unit,
    onOpen: (FlowSession) -> Unit,
    onOpenTrends: () -> Unit
) {
    val editState = remember { mutableStateOf<DialogState?>(null) }
    val deleteState = remember { mutableStateOf<FlowSession?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    Column {
                        Text("FlowBalance Chronicle", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (sessions.isEmpty()) "No cycles recorded" else "${sessions.size} cycles tracked",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = onOpenTrends,
                        label = { Text("Trends") },
                        leadingIcon = { Icon(Icons.Outlined.PieChart, contentDescription = null) }
                    )
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
            ) {
                HeaderPanel(onQuickCapture = onQuickCapture, onCreateRequested = { editState.value = DialogState.Create })
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    if (sessions.isEmpty()) {
                        EmptyTimelineHint()
                    } else {
                        FlowTimelineList(
                            sessions = sessions,
                            onOpen = onOpen,
                            onEdit = { editState.value = DialogState.Edit(it) },
                            onDelete = { deleteState.value = it }
                        )
                    }
                }
            }
        }
    }

    editState.value?.let { state ->
        SessionDialog(
            state = state,
            onDismiss = { editState.value = null },
            onCreate = { label, year, month, inflow ->
                onCreate(label, year, month, inflow)
            },
            onUpdate = onEdit
        )
    }

    deleteState.value?.let { session ->
        ConfirmDeleteDialog(
            session = session,
            onDismiss = { deleteState.value = null },
            onConfirm = {
                onDelete(session)
                deleteState.value = null
            }
        )
    }
}

@Composable
private fun HeaderPanel(
    onQuickCapture: () -> Unit,
    onCreateRequested: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
        ) {
            Text(
                text = "Stay ahead of your cash flow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Capture each monthly cycle, monitor the outflow, and watch the margins shift.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onQuickCapture) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Log current month")
                }
                TextButton(onClick = onCreateRequested) {
                    Text("Custom cycle")
                }
            }
        }
    }
}

@Composable
private fun FlowTimelineList(
    sessions: List<FlowSession>,
    onOpen: (FlowSession) -> Unit,
    onEdit: (FlowSession) -> Unit,
    onDelete: (FlowSession) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        itemsIndexed(sessions) { index, session ->
            TimelineEntry(
                session = session,
                showConnector = index != sessions.lastIndex,
                onOpen = { onOpen(session) },
                onEdit = { onEdit(session) },
                onDelete = { onDelete(session) }
            )
        }
    }
}

@Composable
private fun TimelineEntry(
    session: FlowSession,
    showConnector: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(color = scheme.secondary.copy(alpha = 0.6f))
            }
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(scheme.secondary.copy(alpha = 0.6f))
                )
            }
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onOpen),
            colors = CardDefaults.cardColors(
                containerColor = scheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(session.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${monthName(session.month)} ${session.year}", style = MaterialTheme.typography.bodySmall)
                    }
                    FilledTonalIconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit session")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("Inflow ${currency(session.expectedInflow)}") })
                AssistChip(onClick = {}, label = { Text("Created ${friendlyDate(session.createdAt)}") })
                AssistChip(onClick = onOpen, label = { Text("Review") })
                }
                Text(
                    text = "Tap to review outgoing items or swipe for insights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = onDelete) {
                    Text("Remove cycle")
                }
            }
        }
    }
}

private sealed class DialogState {
    object Create : DialogState()
    data class Edit(val session: FlowSession) : DialogState()
}

@Composable
private fun SessionDialog(
    state: DialogState,
    onDismiss: () -> Unit,
    onCreate: (String, Int, Int, Double) -> Unit,
    onUpdate: (FlowSession) -> Unit
) {
    val isEditing = state is DialogState.Edit
    val context = LocalContext.current
    val session = (state as? DialogState.Edit)?.session
    val calendar = Calendar.getInstance().apply {
        if (session != null) {
            set(Calendar.YEAR, session.year)
            set(Calendar.MONTH, session.month - 1)
        }
    }
    val label = remember(state) { mutableStateOf(session?.label ?: "") }
    val year = remember(state) { mutableStateOf((session?.year ?: calendar.get(Calendar.YEAR)).toString()) }
    val month = remember(state) { mutableStateOf((session?.month ?: (calendar.get(Calendar.MONTH) + 1)).toString()) }
    val inflow = remember(state) { mutableStateOf((session?.expectedInflow ?: 0.0).toString()) }
    val error = remember { mutableStateOf<String?>(null) }

    fun openDatePicker() {
        DatePickerDialog(
            context,
            { _, y, m, _ ->
                year.value = y.toString()
                month.value = (m + 1).toString()
            },
            year.value.toIntOrNull() ?: calendar.get(Calendar.YEAR),
            (month.value.toIntOrNull()?.minus(1))?.coerceIn(0, 11) ?: calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit cycle" else "New cycle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label.value,
                    onValueChange = { label.value = it.take(24) },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = year.value,
                    onValueChange = { year.value = it.filter(Char::isDigit).take(4) },
                    label = { Text("Year") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = month.value,
                    onValueChange = { month.value = it.filter(Char::isDigit).take(2) },
                    label = { Text("Month (1-12)") },
                    singleLine = true,
                    modifier = Modifier.clickable { openDatePicker() },
                    readOnly = true
                )
                OutlinedTextField(
                    value = inflow.value,
                    onValueChange = { inflow.value = it },
                    label = { Text("Expected inflow") },
                    singleLine = true
                )
                AnimatedVisibility(visible = error.value != null) {
                    Text(error.value.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsedYear = year.value.toIntOrNull()
                val parsedMonth = month.value.toIntOrNull()
                val parsedInflow = inflow.value.toDoubleOrNull()
                if (label.value.isBlank() || parsedYear == null || parsedMonth == null || parsedMonth !in 1..12 || parsedInflow == null) {
                    error.value = "Fill in all the fields with valid values."
                    return@TextButton
                }
                if (isEditing && session != null) {
                    onUpdate(session.copy(label = label.value.trim(), year = parsedYear, month = parsedMonth, expectedInflow = parsedInflow))
                } else {
                    onCreate(label.value.trim().ifBlank { "Unnamed cycle" }, parsedYear, parsedMonth, parsedInflow)
                }
                onDismiss()
            }) {
                Text(if (isEditing) "Update" else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    session: FlowSession,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove ${session.label}?") },
        text = { Text("All outgoings belonging to this cycle will also be removed.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EmptyTimelineHint() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Start", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(18.dp))
        Text("No cycles yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add your first inflow cycle to map expenses and balance over time.",
            style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun monthName(month: Int): String =
    SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(0, month - 1, 1))

private fun friendlyDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

private fun currency(value: Double): String =
    String.format(Locale.getDefault(), "$%,.2f", value)


