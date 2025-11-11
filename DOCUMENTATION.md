# FlowBalance Technical Notes

This document maps the major types and composables in the FlowBalance app. It describes responsibilities and behaviours at a high level so you can quickly grasp how data moves through the system without duplicating the source code.

---

## Architecture Overview

FlowBalance follows the same layered pattern as the other budgeting apps (Room + repository + ViewModels + Compose UI) but with its own naming and look:

1. **Data** – `FlowSession` and `FlowOutgoing` entities stored in a Room database (`FlowDatabase`).
2. **Domain** – `FlowRepository` combines DAO calls and produces `SessionDigest` models.
3. **Presentation** – `TimelineViewModel` and `SessionViewModel` expose flows to the UI.
4. **Compose UI** – `TimelineScreen`, `SessionScreen`, and `TrendsScreen` implement a timeline-style UX with different styling.

---

## Data Layer (`com.easy.flowbalance.data`)

### `FlowSession`
Represents a budgeting cycle. Fields:
- `label` – user friendly name.
- `year`, `month` – identify when the cycle takes place.
- `expectedInflow` – planned income for the cycle.
- `createdAt` – stored as epoch millis, used for timeline ordering.

### `FlowOutgoing`
Represents one expense/outgoing. Connected to a session via `sessionId`. Also stores `category` and `spentAt` timestamp. A foreign key cascades deletes when the parent session is removed.

### `FlowSessionDao`
Core queries:
- `watchAll()` returns a `Flow<List<FlowSession>>` sorted newest first.
- `fetchAll()` loads a snapshot list for the trends chart.
- `watch(sessionId)` and `find(sessionId)` observe or fetch a single session.
- Standard `insert`, `update`, `delete`.

### `FlowOutgoingDao`
Provides expense operations:
- `watchForSession(sessionId)` streams outgoings ordered by time.
- `watchTotal(sessionId)` and `fetchTotal(sessionId)` calculate the total spend.
- `insert`, `update`, `delete` mutate the outgoing table.

### `FlowDatabase`
Room singleton. `getInstance(context)` returns the `FlowDatabase` created with the name `flow_balance.db`. Exposes `sessionDao()` and `outgoingDao()`.

### `SessionDigest`
Simple carrier joining a session with its expenses, aggregate spend, and computed balance (expected inflow minus spend).

### `FlowRepository`
Implementation details:
- Constructed with both DAOs. Created inside each ViewModel.
- `observeSessions()` – emits the session list.
- `observeSessionDigest(sessionId)` – combines the session stream, outgoing stream, and total stream into a `SessionDigest`.
- `createSession(label, desiredYear, desiredMonth, expectedInflow)` – inserts a new session while defaulting missing year/month to the current date.
- `updateSession`, `deleteSession`, `getSession` wrap DAO calls.
- `addOutgoing`, `updateOutgoing`, `removeOutgoing` manage expenses with optional category and timestamp overrides.

---

## ViewModel Layer (`com.easy.flowbalance.viewmodel`)

### `TimelineViewModel`
Extends `AndroidViewModel`. Internally builds a `FlowRepository` from the database singleton. Exposes:
- `sessions` – `StateFlow<List<FlowSession>>` used by the timeline screen.
- `createSessionQuick(label, inflow)` – convenience helper that creates a session using the current month.
- `createSessionExplicit(label, year, month, inflow)` – used by the custom dialog.
- `updateSession`, `deleteSession` – pass through to the repository.

### `SessionViewModel`
Also an `AndroidViewModel` tied to a specific session ID.
- `digest` – `StateFlow<SessionDigest?>` delivering live updates for the detail screen.
- `updateInflow(newInflow)` – fetches the current session and updates `expectedInflow`.
- `addOutgoing(...)`, `updateOutgoing(outgoing)`, `deleteOutgoing(outgoing)` – mutate expense entries scoped to the selected session.

### `SessionViewModelFactory`
Simple factory that injects the session ID when creating `SessionViewModel`.

---

## UI Layer (`com.easy.flowbalance.ui`)

### `TimelineScreen`
Entry point UI shown by `MainActivity`. Distinct features:
- Gradient background with a hero card (`HeaderPanel`) explaining the workflow.
- `FlowTimelineList` renders sessions on a vertical timeline using `Canvas` markers and cards.
- `SessionDialog` handles both create and edit flows with name/year/month/inflow fields and uses a date picker for quick month selection.
- `ConfirmDeleteDialog` prevents accidental deletion.
- Quick actions: “Log current month” and “Custom cycle”, plus an insights icon that launches the trends screen.

### `SessionScreen`
Displayed inside `SessionActivity`. Layout:
- `BalanceHeader` – large capsule card summarizing inflow/spent/balance with an animated progress bar.
- `CategorySummary` – aggregated spend per category when outgoings exist.
- Outgoing ledger uses cards with category chips and edit/delete buttons.
- `InflowDialog` updates the expected inflow.
- `OutgoingDialog` adds/edits expenses (title, amount, category, date picker).

### `TrendsScreen`
Hosted by `TrendsActivity`. Flow:
- Loads a snapshot of sessions and totals from Room using the DAO’s `fetchAll` and `fetchTotal`.
- `TrendsCanvas` draws a custom line chart for inflow vs. outflow across up to five periods. Users drag horizontally to shift the window. The design uses rounded lines, large markers, and Material3 colours distinct from the chart in the other apps.

### Misc Helpers
- `TimelineScreen` also defines `DialogState`, `SessionDialog`, and utility functions (`monthName`, `friendlyDate`, `currency`).
- `SessionScreen` includes `SessionContent`, `LoadingPlaceholder`, plus elaborate top cards for a differentiated look.

---

## Activities

### `MainActivity`
Collects sessions from `TimelineViewModel` and forwards callbacks to create/update/delete sessions. Navigates to:
- `SessionActivity` with a session ID extra.
- `TrendsActivity` for the chart view.

### `SessionActivity`
Injects `SessionViewModel` via `SessionViewModelFactory`, observes the digest, and renders `SessionScreen`.

### `TrendsActivity`
Simple wrapper that sets content to `TrendsScreen`.

Manifest entries mark both secondary activities as non-exported (`android:exported="false"`).

---

## Theme and Palette (`ui/theme`)

- `Color.kt` defines teal/amber/clay/fog palettes (`DeepTeal40/80`, `Golden40/80`, etc.).
- `FlowBalanceTheme` locks dynamic colour to `false` and supplies custom light/dark colour schemes. The status bar colour is updated inside `SideEffect`.
- `Theme.kt` uses the new palette so FlowBalance looks completely different from MonthLedger and PocketSheets.

---

## Navigation Flow

1. **MainActivity** → `TimelineScreen`
   - FAB → opens `SessionDialog` (create).
   - Card edit → opens `SessionDialog` (edit).
   - Card delete → `ConfirmDeleteDialog`.
   - Card tap → `SessionActivity`.
   - Top-right icon → `TrendsActivity`.

2. **SessionActivity** → `SessionScreen`
   - FAB → `OutgoingDialog` (add).
   - Edit icon → `InflowDialog`.
   - Expense edit/delete → same dialog/dismissal patterns.

3. **TrendsActivity** → `TrendsScreen`
   - Canvas interactions let the user slide through periods.

---

## Distinguishing Features vs. Other Apps

- Timeline presentation with connectors and hero panel (no grid or plain list).
- Session dialog requires a label and emphasises narrative naming.
- Detail screen uses animated balance bar, category summary, and chip-style stats (instead of the cards used elsewhere).
- Trend chart is rendered inside a rounded “capsule” card with distinct colours and five-period window, using different colour palette and instructions.
- Overall palette uses teal + amber + clay, clearly different from MonthLedger (Material defaults) and PocketSheets (turquoise/orange).
- Documentation text here is unique to FlowBalance.

---

This should equip you to maintain or extend FlowBalance while keeping it demonstrably different from the other apps in the suite.

