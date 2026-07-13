package com.jot.app

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
    val trashedAt: Long = 0L
)
