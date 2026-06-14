package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Flashcard
import com.example.data.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FlashcardRepository
    private val prefs: SharedPreferences = application.getSharedPreferences("falcon_study_prefs", Context.MODE_PRIVATE)

    // Flow states
    val allCards: StateFlow<List<Flashcard>>
    val dueCount: StateFlow<Int>
    val totalCount: StateFlow<Int>
    val masteredCount: StateFlow<Int>

    // UI States
    private val _currentScreen = MutableStateFlow("dashboard") // dashboard, lessons, flashcards, verbs, writing, construct, quiz-hub, reviews, settings
    val currentScreen: StateFlow<String> = _currentScreen

    private val _subject = MutableStateFlow("english") // english, hg
    val subject: StateFlow<String> = _subject

    private val _theme = MutableStateFlow(prefs.getString("falcon_theme", "dark") ?: "dark")
    val theme: StateFlow<String> = _theme

    private val _sfxEnabled = MutableStateFlow(prefs.getBoolean("sfx_enabled", true))
    val sfxEnabled: StateFlow<Boolean> = _sfxEnabled

    // Spaced Repetition Study Session State
    private val _studySessionQueue = MutableStateFlow<List<Flashcard>>(emptyList())
    val studySessionQueue: StateFlow<List<Flashcard>> = _studySessionQueue

    private val _currentSessionIndex = MutableStateFlow(0)
    val currentSessionIndex: StateFlow<Int> = _currentSessionIndex

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped

    // Vocab Decks Browser States
    private val _selectedVocabUnit = MutableStateFlow("Vocab Unit 1")
    val selectedVocabUnit: StateFlow<String> = _selectedVocabUnit

    val vocabDecksCards: StateFlow<List<Flashcard>>

    // Custom Cards States
    val customCards: StateFlow<List<Flashcard>>

    // 10-Day Planner States (comma-separated string in shared preferences)
    private val _completedPlannerDays = MutableStateFlow<Set<Int>>(getCompletedDaysFromPrefs())
    val completedPlannerDays: StateFlow<Set<Int>> = _completedPlannerDays

    // H&G Essay Constructor States
    private val _constructorStep = MutableStateFlow(1)
    val constructorStep: StateFlow<Int> = _constructorStep

    val constructText1 = MutableStateFlow("")
    val constructText2 = MutableStateFlow("")
    val constructText3 = MutableStateFlow("")
    val constructText4 = MutableStateFlow("")

    private val _assembledHGEssay = MutableStateFlow("")
    val assembledHGEssay: StateFlow<String> = _assembledHGEssay

    // Interactive Quiz States
    private val _activeQuizType = MutableStateFlow("english") // english, hg
    val activeQuizType: StateFlow<String> = _activeQuizType

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished

    private val _selectedQuizAnswerIdx = MutableStateFlow<Int?>(null)
    val selectedQuizAnswerIdx: StateFlow<Int?> = _selectedQuizAnswerIdx

    // Search query State
    val searchQuery = MutableStateFlow("")

    // Irregular verb list filtering
    val irregularVerbsList: StateFlow<List<Flashcard>>

    val masteredFlashcards = MutableStateFlow<Set<String>>(emptySet())
    val masteredVerbs = MutableStateFlow<Set<String>>(emptySet())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FlashcardRepository(database.flashcardDao())

        // Setup the initial flows
        allCards = repository.allCards.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        totalCount = repository.getTotalCount().stateIn(viewModelScope, SharingStarted.Lazily, 0)
        masteredCount = repository.getMasteredCount().stateIn(viewModelScope, SharingStarted.Lazily, 0)
        
        val currentTime = System.currentTimeMillis()
        dueCount = repository.getDueCount(currentTime).stateIn(viewModelScope, SharingStarted.Lazily, 0)

        // Prepopulate data
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }

        // Selected Vocab Unit flow
        vocabDecksCards = combine(_selectedVocabUnit, allCards) { unit, cards ->
            cards.filter { it.category == unit }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Custom cards flow
        customCards = allCards.map { cards ->
            cards.filter { it.isCustom }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Filtered Irregular Verbs list
        irregularVerbsList = combine(searchQuery, allCards) { query, cards ->
            cards.filter { 
                it.category == "Irregular Verbs" && 
                (query.isEmpty() || it.front.contains(query, ignoreCase = true) || it.back.contains(query, ignoreCase = true))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Toggle Screen
    fun setScreen(screenName: String) {
        _currentScreen.value = screenName
    }

    // Toggle Subject
    fun setSubject(subj: String) {
        _subject.value = subj
        playTickSound()
    }

    // Toggle theme
    fun toggleTheme() {
        val nextTheme = if (_theme.value == "dark") "light" else "dark"
        _theme.value = nextTheme
        prefs.edit().putString("falcon_theme", nextTheme).apply()
        playTickSound()
    }

    // Toggle SFX
    fun toggleSFX() {
        val nextSfx = !_sfxEnabled.value
        _sfxEnabled.value = nextSfx
        prefs.edit().putBoolean("sfx_enabled", nextSfx).apply()
        if (nextSfx) {
            playTickSound()
        }
    }

    fun selectVocabUnit(unitName: String) {
        _selectedVocabUnit.value = unitName
        _currentSessionIndex.value = 0
        _isCardFlipped.value = false
    }

    fun nextFlashcard() {
        val size = vocabDecksCards.value.size
        val currentIdx = _currentSessionIndex.value
        if (currentIdx + 1 < size) {
            _isCardFlipped.value = false
            _currentSessionIndex.value = currentIdx + 1
            playTickSound()
        }
    }

    fun prevFlashcard() {
        val currentIdx = _currentSessionIndex.value
        if (currentIdx > 0) {
            _isCardFlipped.value = false
            _currentSessionIndex.value = currentIdx - 1
            playTickSound()
        }
    }

    fun toggleMasterFlashcard() {
        val cards = vocabDecksCards.value
        val idx = _currentSessionIndex.value
        if (cards.isNotEmpty() && idx < cards.size) {
            val card = cards[idx]
            val key = "${_selectedVocabUnit.value}_${card.front}"
            val activeMastered = masteredFlashcards.value.toMutableSet()
            if (activeMastered.contains(key)) {
                activeMastered.remove(key)
                playTickSound()
            } else {
                activeMastered.add(key)
                playSuccessSound()
            }
            masteredFlashcards.value = activeMastered
        }
    }

    fun toggleVerbMastery(base: String) {
        val activeMastered = masteredVerbs.value.toMutableSet()
        if (activeMastered.contains(base)) {
            activeMastered.remove(base)
            playTickSound()
        } else {
            activeMastered.add(base)
            playSuccessSound()
        }
        masteredVerbs.value = activeMastered
    }

    // Spaced repetition review queue startup
    fun startReviewSession() {
        playTickSound()
        viewModelScope.launch {
            val due = repository.getDueCards(System.currentTimeMillis()).first()
            _studySessionQueue.value = due.shuffled() // Shuffle to prevent monotony
            _currentSessionIndex.value = 0
            _isCardFlipped.value = false
            if (due.isNotEmpty()) {
                setScreen("reviews")
            }
        }
    }

    fun flipCard() {
        playTickSound()
        _isCardFlipped.value = !_isCardFlipped.value
    }

    // Processspaced repetition answer (Rating: 1 = Hard, 3 = Good, 5 = Easy)
    fun rateActiveCard(rating: Int) {
        val queue = _studySessionQueue.value
        val currentIndex = _currentSessionIndex.value
        if (queue.isEmpty() || currentIndex >= queue.size) return

        if (rating >= 3) {
            playSuccessSound()
        } else {
            playErrorSound()
        }

        val card = queue[currentIndex]
        viewModelScope.launch {
            repository.processReview(card, rating)
            
            // Advance to next card or exit
            if (currentIndex + 1 < queue.size) {
                _isCardFlipped.value = false
                _currentSessionIndex.value = currentIndex + 1
            } else {
                // Completed!
                _studySessionQueue.value = emptyList()
                _currentSessionIndex.value = 0
                _isCardFlipped.value = false
                // Play completion victory award sound
                playVictorySound()
                setScreen("dashboard")
            }
        }
    }

    // Customs Add Flashcard
    fun addNewCustomCard(front: String, back: String, category: String, hint: String) {
        if (front.isBlank() || back.isBlank()) return
        val newCard = Flashcard(
            front = front,
            back = back,
            category = category,
            hint = hint,
            isCustom = true
        )
        viewModelScope.launch {
            repository.insertCard(newCard)
            playSuccessSound()
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch {
            repository.deleteCard(card)
            playTickSound()
        }
    }

    // Planner Days Management
    fun togglePlannerDay(day: Int) {
        val activeCompleted = _completedPlannerDays.value.toMutableSet()
        if (activeCompleted.contains(day)) {
            activeCompleted.remove(day)
            playTickSound()
        } else {
            activeCompleted.add(day)
            playSuccessSound()
        }
        _completedPlannerDays.value = activeCompleted
        saveCompletedDaysToPrefs(activeCompleted)
    }

    private fun getCompletedDaysFromPrefs(): Set<Int> {
        val saved = prefs.getString("completed_planner_days", "") ?: ""
        if (saved.isBlank()) return emptySet()
        return saved.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun saveCompletedDaysToPrefs(days: Set<Int>) {
        val textStr = days.joinToString(",")
        prefs.edit().putString("completed_planner_days", textStr).apply()
    }

    // H&G Essay Constructor Management
    fun setConstructorStep(step: Int) {
        if (step in 1..4) {
            _constructorStep.value = step
            playTickSound()
        }
    }

    fun insertPhraseToConstructor(phrase: String) {
        when (_constructorStep.value) {
            1 -> constructText1.value += phrase
            2 -> constructText2.value += phrase
            3 -> constructText3.value += phrase
            4 -> constructText4.value += phrase
        }
        playTickSound()
    }

    fun assembleHGEssay() {
        val intro = constructText1.value.trim()
        val body1 = constructText2.value.trim()
        val body2 = constructText3.value.trim()
        val concl = constructText4.value.trim()

        if (intro.isBlank() || body1.isBlank() || body2.isBlank() || concl.isBlank()) {
            playErrorSound()
            return
        }

        playVictorySound()
        _assembledHGEssay.value = """
            الموضوع المقالي (التركيب المنهجي النهائي):
            
            [المقدمة الإشكالية]
            $intro
            
            [العرض - الفقرة الأولى]
            $body1
            
            [العرض - الفقرة الثانية]
            $body2
            
            [الخاتمة والتركيب]
            $concl
        """.trimIndent()
        
        prefs.edit().putBoolean("h_g_essay_assembled", true).apply()
    }

    fun isHGEssayAssembled(): Boolean {
        return prefs.getBoolean("h_g_essay_assembled", false)
    }

    // Quiz Hub logic
    fun startQuiz(type: String) {
        _activeQuizType.value = type
        _quizQuestions.value = loadQuizQuestions(type).shuffled()
        _currentQuizIndex.value = 0
        _quizScore.value = 0
        _quizFinished.value = false
        _selectedQuizAnswerIdx.value = null
        playTickSound()
    }

    fun selectQuizAnswer(index: Int) {
        if (_selectedQuizAnswerIdx.value != null || _quizFinished.value) return
        
        _selectedQuizAnswerIdx.value = index
        val currentQ = _quizQuestions.value[_currentQuizIndex.value]
        
        if (index == currentQ.correctIdx) {
            _quizScore.value += 2
            playSuccessSound()
        } else {
            playErrorSound()
        }

        // Proceed to next or finish after brief delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            if (_currentQuizIndex.value + 1 < _quizQuestions.value.size) {
                _selectedQuizAnswerIdx.value = null
                _currentQuizIndex.value += 1
            } else {
                _quizFinished.value = true
                if (_quizScore.value >= (_quizQuestions.value.size * 2) * 0.8) {
                    playVictorySound()
                }
            }
        }
    }


    /* Output Synthesized Audio Beeps */
    private fun playSineTone(freq: Double, durationMs: Int) {
        if (!_sfxEnabled.value) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sampleRate = 44100
                val numSamples = (durationMs * sampleRate / 1000.0).toInt()
                val signal = DoubleArray(numSamples)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    signal[i] = Math.sin(2.0 * Math.PI * i.toDouble() / (sampleRate / freq))
                    // Exponential dampening for soft audio chime release
                    val damp = 1.0 - (i.toDouble() / numSamples)
                    buffer[i] = (signal[i] * 32767.0 * 0.25 * damp).toInt().toShort()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    AudioTrack.MODE_STATIC
                )

                audioTrack.write(buffer, 0, numSamples)
                audioTrack.play()
                
                // Release audioTrack resource after play length expires
                kotlinx.coroutines.delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTickSound() {
        playSineTone(580.0, 60)
    }

    fun playSuccessSound() {
        viewModelScope.launch {
            playSineTone(523.25, 100) // C5
            kotlinx.coroutines.delay(70)
            playSineTone(659.25, 100) // E5
        }
    }

    fun playErrorSound() {
        playSineTone(130.0, 220) // Low buzz
    }

    fun playVictorySound() {
        viewModelScope.launch {
            playSineTone(523.25, 80) // C5
            kotlinx.coroutines.delay(60)
            playSineTone(659.25, 80) // E5
            kotlinx.coroutines.delay(60)
            playSineTone(783.99, 80) // G5
            kotlinx.coroutines.delay(60)
            playSineTone(1046.50, 300) // C6 High
        }
    }

    // Helper database content loading for quizzes
    private fun loadQuizQuestions(type: String): List<QuizQuestion> {
        return if (type == "english") {
            listOf(
                QuizQuestion(
                    q = "What tense uses 'yesterday' as a signal word?",
                    options = listOf("Simple Present", "Simple Past", "Present Perfect", "Future Simple"),
                    correctIdx = 1,
                    ex = "نستخدم الماضي البسيط (Simple Past) بسبب وجود الكلمة الدلالية yesterday."
                ),
                QuizQuestion(
                    q = "Identify: 'By the time I arrived, she _______ already left.'",
                    options = listOf("has", "had", "will", "is"),
                    correctIdx = 1,
                    ex = "نستخدم Past Perfect (had + V3) للتعبير عن فعل حدث قبل فعل آخر في الماضي."
                ),
                QuizQuestion(
                    q = "Choose correct conditional: 'If I _______ you, I would study harder.'",
                    options = listOf("was", "were", "am", "be"),
                    correctIdx = 1,
                    ex = "في الجملة الشرطية من النوع الثاني (Type 2)، نستخدم 'were' للتعبير عن الاستحالة أو التخيل."
                ),
                QuizQuestion(
                    q = "Complete the linker sentence: 'They came to work _______ the heavy rain.'",
                    options = listOf("although", "despite", "because", "so"),
                    correctIdx = 1,
                    ex = "نستخدم despite لأنها تتبع باسم أو فعل ينتهي بـ ing."
                ),
                QuizQuestion(
                    q = "Give matching function: 'Could you please post this letter for me?'",
                    options = listOf("Apologizing", "Giving advice", "Requesting", "Complaining"),
                    correctIdx = 2,
                    ex = "'Could you please' تستعمل لصياغة طلب مؤدب (Requesting)."
                )
            )
        } else {
            listOf(
                QuizQuestion(
                    q = "ما هي العناصر الأساسية المحددة للإطار التاريخي للوثائق؟",
                    options = listOf("العنوان والفقرات والمصدر", "الزمان والمكان والموضوع", "مقدمة وعرض وخاتمة", "المفاهيم والأعلام والنتائج"),
                    correctIdx = 1,
                    ex = "الإطار التاريخي للوثائق يبنى دائما على ثلاثة أسس: الزمان، المكان، والموضوع."
                ),
                QuizQuestion(
                    q = "متى يلجأ التلميذ إلى رسم مبيان بالمنحنى في مادة الجغرافيا؟",
                    options = listOf("عند مقارنة عدة معطيات ثابتة", "عند وجود نسب مئوية مجموعها 100%", "عند تتبع ظاهرة متطورة في الزمن باستمرار", "عند توطين معطيات على خريطة"),
                    correctIdx = 2,
                    ex = "نلجأ إلى مبيان بالمنحنى عند تمثيل وتتبع تطور ظاهرة جغرافية معينة باستمرار عبر الزمن."
                ),
                QuizQuestion(
                    q = "ما هي الخطوات الثلاث للمنهج والخطاب الجغرافي؟",
                    options = listOf("طرح الإشكالية والتوليف والتركيب", "الشرح والمقارنة والتصنيف", "الوصف الجغرافي والتفسير والتعميم", "المقدمة والعرض والخاتمة"),
                    correctIdx = 2,
                    ex = "الخطوات المنهجية الثلاث لدراسة الجغرافيا هي: الوصف الجغرافي، التفسير الجغرافي، ثم التعميم الجغرافي."
                ),
                QuizQuestion(
                    q = "ما النسبة المئوية المخصصة للموضوع المقالي في الامتحان الوطني؟",
                    options = listOf("30% من النقطة النهائية", "50% (10 نقاط كاملة)", "25% (5 نقاط فقط)", "المقال إجباري في المادتين معاً"),
                    correctIdx = 1,
                    ex = "يخصص للموضوع المقالي 10 نقاط كاملة (50%)، والـ 10 نقاط الأخرى للاشتغال على الوثائق."
                )
            )
        }
    }
}

data class QuizQuestion(
    val q: String,
    val options: List<String>,
    val correctIdx: Int,
    val ex: String
)
