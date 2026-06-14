package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val front: String,
    val back: String,
    val category: String, // e.g. "Vocab Unit 1", "Irregular Verbs", "History & Geography", "Grammar Rules"
    val hint: String = "", // Optional hint, e.g. "Education", "Verb", "Methodology"
    val nextReviewDate: Long = System.currentTimeMillis(),
    val intervalDays: Int = 0,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false // If the user created it manually
)
