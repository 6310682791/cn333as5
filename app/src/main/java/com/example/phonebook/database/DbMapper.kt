package com.example.phonebook.database

import com.example.phonebook.domain.model.ColorModel
import com.example.phonebook.domain.model.NEW_NOTE_ID
import com.example.phonebook.domain.model.NoteModel

class DbMapper {
    fun mapNotes(
        noteDbModels: List<NoteDbModel>,
        colorDbModels: Map<Long, ColorDbModel>
    ): List<NoteModel> = noteDbModels.map {
        val colorDbModel = colorDbModels[it.colorId]
            ?: throw RuntimeException("Color for colorId: ${it.colorId} was not found. Make sure that all colors are passed to this method")

        mapNote(it, colorDbModel)
    }

    // convert NoteDbModel to NoteModel
    fun mapNote(noteDbModel: NoteDbModel, colorDbModel: ColorDbModel): NoteModel { //
        val color = mapColor(colorDbModel)
        val isCheckedOff = with(noteDbModel) { if (canBeCheckedOff) isCheckedOff else null }
        return with(noteDbModel) { NoteModel(id, firstname, lastname, number, isCheckedOff, color) }
    }

    // convert list of ColorDdModels to list of ColorModels
    fun mapColors(colorDbModels: List<ColorDbModel>): List<ColorModel> =
        colorDbModels.map { mapColor(it) }

    // convert ColorDbModel to ColorModel
    fun mapColor(colorDbModel: ColorDbModel): ColorModel =
        with(colorDbModel) { ColorModel(id, name, hex) }

    // convert NoteModel back to NoteDbModel
    fun mapDbNote(note: NoteModel): NoteDbModel =
        with(note) {
            val canBeCheckedOff = isCheckedOff != null
            val isCheckedOff = isCheckedOff ?: false
            if (id == NEW_NOTE_ID)
                NoteDbModel(
                    firstname = firstname,
                    lastname = lastname,
                    number = number,
                    canBeCheckedOff = canBeCheckedOff,
                    isCheckedOff = isCheckedOff,
                    colorId = color.id,
                    isInTrash = false
                )
            else
                NoteDbModel(id, firstname, lastname, number, canBeCheckedOff, isCheckedOff, color.id, false )
        }
}