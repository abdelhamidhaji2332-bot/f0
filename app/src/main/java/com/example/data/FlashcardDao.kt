package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY front ASC")
    fun getAllCards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE category = :category ORDER BY id ASC")
    fun getCardsByCategory(category: String): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC")
    fun getDueCards(currentTime: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getCardById(id: Int): Flashcard?

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Flashcard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<Flashcard>)

    @Update
    suspend fun updateCard(card: Flashcard)

    @Delete
    suspend fun deleteCard(card: Flashcard)

    @Query("SELECT COUNT(*) FROM flashcards")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewDate <= :currentTime")
    fun getDueCount(currentTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE repetitions >= 4")
    fun getMasteredCount(): Flow<Int>
}
