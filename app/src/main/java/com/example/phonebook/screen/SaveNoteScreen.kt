package com.example.phonebook.screen

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonebook.routing.MyNotesRouter
import com.example.phonebook.routing.Screen
import com.example.phonebook.viewmodel.MainViewModel
import com.example.phonebook.R
import com.example.phonebook.domain.model.ColorModel
import com.example.phonebook.domain.model.NEW_NOTE_ID
import com.example.phonebook.domain.model.NoteModel
import com.example.phonebook.ui.components.NoteColor
import com.example.phonebook.util.fromHex
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@ExperimentalMaterialApi
@Composable
fun SaveNoteScreen(viewModel: MainViewModel) {
    val noteEntry by viewModel.noteEntry.observeAsState(NoteModel())

    val colors: List<ColorModel> by viewModel.colors.observeAsState(listOf())

    val bottomDrawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)

    val coroutineScope = rememberCoroutineScope()

    val moveNoteToTrashDialogShownState = rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (bottomDrawerState.isOpen) {
            coroutineScope.launch { bottomDrawerState.close() }
        } else {
            MyNotesRouter.navigateTo(Screen.Notes)
        }
    }

    Scaffold(
        topBar = {
            val isEditingMode: Boolean = noteEntry.id != NEW_NOTE_ID
            SaveNoteTopAppBar(
                isEditingMode = isEditingMode,
                onBackClick = { MyNotesRouter.navigateTo(Screen.Notes) },
                onSaveNoteClick = { viewModel.saveNote(noteEntry) },
                onOpenColorPickerClick = {
                    coroutineScope.launch { bottomDrawerState.open() }
                },
                onDeleteNoteClick = {
                    moveNoteToTrashDialogShownState.value = true
                }
            )
        }
    ) {
        BottomDrawer(
            drawerState = bottomDrawerState,
            drawerContent = {
                ColorPicker(
                    colors = colors,
                    onColorSelect = { color ->
                        viewModel.onNoteEntryChange(noteEntry.copy(color = color))
                    }
                )
            }
        ) {
            SaveNoteContent(
                note = noteEntry,
                onNoteChange = { updateNoteEntry ->
                    viewModel.onNoteEntryChange(updateNoteEntry)
                }
            )
        }

        if (moveNoteToTrashDialogShownState.value) {
            AlertDialog(
                onDismissRequest = {
                    moveNoteToTrashDialogShownState.value = false
                },
                title = {
                    Text("Move Contact to the trash?")
                },
                text = {
                    Text(
                        "Are you sure you want to " +
                                "move this Contact to the trash?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.moveNoteToTrash(noteEntry)
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        moveNoteToTrashDialogShownState.value = false
                    }) {
                        Text("Dismiss")
                    }
                }
            )
        }
    }
}

@Composable
fun SaveNoteTopAppBar(
    isEditingMode: Boolean,
    onBackClick: () -> Unit,
    onSaveNoteClick: () -> Unit,
    onOpenColorPickerClick: () -> Unit,
    onDeleteNoteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Phone Book",
                color = MaterialTheme.colors.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Button",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onSaveNoteClick) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save Note Button",
                    tint = MaterialTheme.colors.onPrimary
                )
            }

            IconButton(onClick = onOpenColorPickerClick) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_color_lens_24),
                    contentDescription = "Open Color Picker Button",
                    tint = MaterialTheme.colors.onPrimary
                )
            }

            if (isEditingMode) {
                IconButton(onClick = onDeleteNoteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Contact Button",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    )
}

@Composable
private fun SaveNoteContent(
    note: NoteModel,
    onNoteChange: (NoteModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
        ContentTextField(
            label = "Firstname",
            text = note.firstname,
            onTextChange = { newTitle ->
                onNoteChange.invoke(note.copy(firstname = newTitle))
            }
        )

        ContentTextField(
            modifier = Modifier
                .heightIn(max = 240.dp)
                .padding(top = 16.dp),
            label = "Lastname",
            text = note.lastname,
            onTextChange = { newContent ->
                onNoteChange.invoke(note.copy(lastname = newContent))
            }
        )

        ContentTextField(
            modifier = Modifier
                .heightIn(max = 240.dp)
                .padding(top = 16.dp),
            label = "Number",
            text = note.number,
            onTextChange = { newType ->
                onNoteChange.invoke(note.copy(number = newType))
            }
        )

        PickedColor(color = note.color)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = {},
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Call")
            }
        }
    }
}

@Composable
private fun ContentTextField(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    onTextChange: (String) -> Unit
) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.surface
        )
    )
}

@Composable
private fun PickedColor(color: ColorModel) {
    Row(
        Modifier
            .padding(8.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Picked : "  + color.name,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        NoteColor(
            color = Color.fromHex(color.hex),
            size = 40.dp,
            border = 1.dp,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun ColorPicker(
    colors: List<ColorModel>,
    onColorSelect: (ColorModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pick tag",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(colors.size) { itemIndex ->
                val color = colors[itemIndex]
                ColorItem(
                    color = color,
                    onColorSelect = onColorSelect
                )
            }
        }
    }
}

@Composable
fun ColorItem(
    color: ColorModel,
    onColorSelect: (ColorModel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onColorSelect(color)
                }
            )
    ) {
        NoteColor(
            modifier = Modifier.padding(10.dp),
            color = Color.fromHex(color.hex),
            size = 80.dp,
            border = 2.dp
        )
        Text(
            text = color.name,
            fontSize = 22.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

@Preview
@Composable
fun ColorItemPreview() {
    ColorItem(ColorModel.DEFAULT) {}
}

@Preview
@Composable
fun ColorPickerPreview() {
    ColorPicker(
        colors = listOf(
            ColorModel.DEFAULT,
            ColorModel.DEFAULT,
            ColorModel.DEFAULT
        )
    ) { }
}

@Preview
@Composable
fun PickedColorPreview() {
    PickedColor(ColorModel.DEFAULT)
}