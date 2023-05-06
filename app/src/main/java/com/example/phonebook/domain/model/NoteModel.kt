package com.example.phonebook.domain.model

const val NEW_NOTE_ID = -1L
data class NoteModel(
    val id: Long = NEW_NOTE_ID,
    val firstname: String = "",
    val lastname: String = "",
    val number: String = "",
    val isCheckedOff: Boolean? = null,
    val color: ColorModel = ColorModel.DEFAULT

)