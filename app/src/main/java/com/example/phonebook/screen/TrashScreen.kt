package com.example.phonebook.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import com.example.phonebook.R
import com.example.phonebook.domain.model.NoteModel
import com.example.phonebook.routing.Screen
import com.example.phonebook.ui.components.AppDrawer
import com.example.phonebook.ui.components.Note
import com.example.phonebook.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private const val NO_DIALOG = 1
private const val CALL_DIALOG = 2 //undelete

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
@ExperimentalMaterialApi
fun TrashScreen(viewModel: MainViewModel) {

    val notesInThrash: List<NoteModel> by viewModel.notesInTrash
        .observeAsState(listOf())

    val selectedNotes: List<NoteModel> by viewModel.selectedNotes
        .observeAsState(listOf())

    val dialogState = rememberSaveable { mutableStateOf(NO_DIALOG) }

    val scaffoldState = rememberScaffoldState()

    val coroutineScope = rememberCoroutineScope()

    val notesnotInThrash by viewModel.notesNotInTrash.observeAsState(listOf())

    Scaffold(
        topBar = {
            val areActionsVisible = selectedNotes.isNotEmpty()
            TrashTopAppBar(
                onNavigationIconClick = {
                    coroutineScope.launch { scaffoldState.drawerState.open() }
                },
                call = { dialogState.value = CALL_DIALOG },
                areActionsVisible = areActionsVisible
            )
        },
        scaffoldState = scaffoldState,
        drawerContent = {
            AppDrawer(
                currentScreen = Screen.Trash
            ) {
                coroutineScope.launch { scaffoldState.drawerState.close() }
            }
        },
        content = {
            Content(
                notes = notesnotInThrash,
                onNoteClick = { viewModel.onNoteSelected(it) },
                selectedNotes = selectedNotes
            )

            val dialog = dialogState.value
            if (dialog != NO_DIALOG) {
                val confirmAction: () -> Unit = when (dialog) {
                    CALL_DIALOG -> {
                        {
                            viewModel.restoreNotes(selectedNotes)
                            dialogState.value = NO_DIALOG
                        }
                    }
                    else -> {
                        {
                            dialogState.value = NO_DIALOG
                        }
                    }
                }

                AlertDialog(
                    onDismissRequest = { dialogState.value = NO_DIALOG },
                    title = { Text(mapDialogTitle(dialog)) },
                    text = { Text(mapDialogText(dialog)) },
                    confirmButton = {
                        TextButton(onClick = confirmAction) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dialogState.value = NO_DIALOG }) {
                            Text("Dismiss")
                        }
                    }
                )
            }
        }
    )
}


@Composable
private fun TrashTopAppBar(
    onNavigationIconClick: () -> Unit,
    call: () -> Unit,
    areActionsVisible: Boolean
) {
    TopAppBar(
        title = { Text(text = "Contacts", color = MaterialTheme.colors.onPrimary) },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Filled.List,
                    contentDescription = "Drawer Button"
                )
            }
        },
        actions = {
            if (areActionsVisible) {
                IconButton(onClick = call) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stat_name),
                        contentDescription = "Call",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    )
}

@Composable
@ExperimentalMaterialApi
private fun Content(
    notes: List<NoteModel>,
    onNoteClick: (NoteModel) -> Unit,
    selectedNotes: List<NoteModel>,
) {
    val tabs = listOf("Home", "Work", "Friend", "Etc.")

    // Init state for selected tab
    var selectedTab by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        val filteredNotes = when (selectedTab) {
            0 -> {
                notes.filter { it.color.name == "Home" }
            }
            1 -> {
                notes.filter { it.color.name == "Work" }
            }
            2 -> {
                notes.filter { it.color.name == "Friend" }
            }
            3 -> {
                notes.filter { it.color.name == "Etc." }
            }
            else -> throw IllegalStateException("Tab not supported - index: $selectedTab")
        }

        LazyColumn {
            items(count = filteredNotes.size) { noteIndex ->
                val note = filteredNotes[noteIndex]
                val isNoteSelected = selectedNotes.contains(note)
                Note(
                    note = note,
                    onNoteClick = onNoteClick,
                    isSelected = isNoteSelected
                )
            }
        }
    }
}

private fun mapDialogTitle(dialog: Int): String = when (dialog) {
    CALL_DIALOG -> "Call"
    else -> throw RuntimeException("Dialog not supported: $dialog")
}
private fun mapDialogText(dialog: Int): String = when (dialog) {
    CALL_DIALOG -> "Are you sure you want to call?"
    else -> throw RuntimeException("Dialog not supported: $dialog")
}
