package com.easy.flowbalance.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.easy.flowbalance.data.SessionDigest
import com.easy.flowbalance.data.FlowOutgoing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SessionScreen(
    digest: SessionDigest?,
    onBack: () -> Unit,
    onUpdateInflow: (Double) -> Unit,
    onCreateOutgoing: (String, Double, String, Long?) -> Unit,
    onUpdateOutgoing: (FlowOutgoing) -> Unit,
    onDeleteOutgoing: (FlowOutgoing) -> Unit
) {
    val showIncome = remember { mutableStateOf(false) }
    val editOutgoing = remember { mutableStateOf<FlowOutgoing?>(null) }
    val addOutgoing = remember { mutableStateOf(false) }
    val removeOutgoing = remember { mutableStateOf<FlowOutgoing?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        digest?.session?.label ?: "Loading",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showIncome.value = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Adjust inflow")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { addOutgoing.value = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add outgoing")
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.surface
        ) {
            when (digest) {
                null -> LoadingPlaceholder()
                else -> SessionContent(
                    digest = digest,
                    onEdit = { editOutgoing.value = it },
                    onDelete = { removeOutgoing.value = it }
                )
            }
        }
    }

    if (showIncome.value && digest != null) {
        InflowDialog(
            current = digest.session.expectedInflow,
            onDismiss = { showIncome.value = false },
            onConfirm = {
                onUpdateInflow(it)
                showIncome.value = false
            }
        )
    }

    if (addOutgoing.value) {
        OutgoingDialog(
            initial = null,
            onDismiss = { addOutgoing.value = false },
            onConfirm = { title, amount, category, millis ->
                onCreateOutgoing(title, amount, category, millis)
                addOutgoing.value = false
            }
        )
    }

    editOutgoing.value?.let { outgoing ->
        OutgoingDialog(
            initial = outgoing,
            onDismiss = { editOutgoing.value = null },
            onConfirm = { title, amount, category, millis ->
                onUpdateOutgoing(outgoing.copy(title = title, amount = amount, category = category, spentAt = millis ?: outgoing.spentAt))
                editOutgoing.value = null
            }
        )
    }

    removeOutgoing.value?.let { outgoing ->
        AlertDialog(
            onDismissRequest = { removeOutgoing.value = null },
            title = { Text("Remove ${outgoing.title}?") },
            text = { Text("This outgoing will be lost permanently.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteOutgoing(outgoing)
                    removeOutgoing.value = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { removeOutgoing.value = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SessionContent(
    digest: SessionDigest,
    onEdit: (FlowOutgoing) -> Unit,
    onDelete: (FlowOutgoing) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        BalanceHeader(digest)
        Spacer(Modifier.height(16.dp))
        CategorySummary(digest.outgoings)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Outgoing ledger",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(digest.outgoings, key = { it.id }) { outgoing ->
                OutgoingCard(outgoing = outgoing, onEdit = { onEdit(outgoing) }, onDelete = { onDelete(outgoing) })
            }
        }
    }
}

@Composable
private fun BalanceHeader(digest: SessionDigest) {
    val balanceRatio = animateFloatAsState(
        targetValue = when {
            digest.session.expectedInflow <= 0 -> 0f
            else -> (digest.spent / digest.session.expectedInflow).toFloat().coerceIn(0f, 1f)
        },
        label = "balance_ratio"
    )
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Inflow target ${currency(digest.session.expectedInflow)}", style = MaterialTheme.typography.labelLarge)
            Text("Spent ${currency(digest.spent)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                if (digest.balance >= 0) "Remaining ${currency(digest.balance)}"
                else "Exceeded by ${currency(kotlin.math.abs(digest.balance))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .fillMaxWidth(balanceRatio.value)
                )
            }
        }
    }
}

@Composable
private fun CategorySummary(outgoings: List<FlowOutgoing>) {
    if (outgoings.isEmpty()) {
        return
    }
    val grouped = outgoings.groupBy { it.category.ifBlank { "General" } }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("By category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            grouped.entries.sortedByDescending { entry -> entry.value.sumOf { it.amount } }.forEach { entry ->
                val total = entry.value.sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CA6B3).copy(alpha = 0.6f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(entry.key, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(currency(total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun OutgoingCard(
    outgoing: FlowOutgoing,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(outgoing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            AssistChip(onClick = {}, label = { Text(outgoing.category.ifBlank { "General" }) })
            Text(
                prettyDate(outgoing.spentAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currency(outgoing.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = onDelete, colors = androidx.compose.material3.ButtonDefaults.textButtonColors(MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(60.dp))
        Spacer(Modifier.height(12.dp))
        Text("Gathering flow details...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InflowDialog(
    current: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val inflow = remember { mutableStateOf(current.toString()) }
    val error = remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust expected inflow") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = inflow.value,
                    onValueChange = { inflow.value = it },
                    label = { Text("Expected inflow") },
                    singleLine = true
                )
                AnimatedVisibility(visible = error.value != null, enter = expandVertically()) {
                    Text(error.value.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = inflow.value.toDoubleOrNull()
                if (parsed == null) {
                    error.value = "Enter a valid number"
                } else {
                    onConfirm(parsed)
                }
            }) { Text("Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun OutgoingDialog(
    initial: FlowOutgoing?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Long?) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        if (initial != null) timeInMillis = initial.spentAt
    }
    val title = remember(initial) { mutableStateOf(initial?.title ?: "") }
    val amount = remember(initial) { mutableStateOf(initial?.amount?.toString() ?: "") }
    val category = remember(initial) { mutableStateOf(initial?.category ?: "") }
    val dateMillis = remember(initial) { mutableStateOf(initial?.spentAt ?: System.currentTimeMillis()) }
    val error = remember { mutableStateOf<String?>(null) }

    fun pickDate() {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dateMillis.value = chosen.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Log outgoing" else "Edit outgoing") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount.value,
                    onValueChange = { amount.value = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = category.value,
                    onValueChange = { category.value = it },
                    label = { Text("Category") },
                    singleLine = true
                )
                Button(onClick = ::pickDate) {
                    Text(prettyDate(dateMillis.value))
                }
                AnimatedVisibility(visible = error.value != null) {
                    Text(error.value.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmedTitle = title.value.trim()
                val parsedAmount = amount.value.replace(',', '.').toDoubleOrNull()
                when {
                    trimmedTitle.isEmpty() -> error.value = "Enter a title"
                    parsedAmount == null -> error.value = "Enter a numeric amount"
                    else -> {
                        error.value = null
                        onConfirm(trimmedTitle, parsedAmount, category.value.trim(), dateMillis.value)
                    }
                }
            }) { Text(if (initial == null) "Add" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun prettyDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

private fun currency(value: Double): String =
    String.format(Locale.getDefault(), "$%,.2f", value)


