package com.jot.app

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index("trashedAt"),
        Index("isArchived"),
        Index("title"),
        Index("content")
    ]
)
data class Note(
    @PrimaryKey val id: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val trashedAt: Long = 0L,
    val isArchived: Boolean = false
)