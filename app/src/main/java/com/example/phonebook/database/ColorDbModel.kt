package com.example.phonebook.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ColorDbModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "hex") val hex: String,
    @ColumnInfo(name = "name") val name: String
) {
    companion object {
        val DEFAULT_COLORS = listOf(
            ColorDbModel(1, "#1D9F90", "Home"),
            ColorDbModel(2, "#7B80E5", "Friend"),
            ColorDbModel(3, "#232AAF", "Work"),
            ColorDbModel(4, "#FFFFFF", "etc"),
        )
        val DEFAULT_COLOR = DEFAULT_COLORS[3]
    }
}