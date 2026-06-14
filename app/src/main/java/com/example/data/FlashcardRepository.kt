package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FlashcardRepository(private val dao: FlashcardDao) {

    val allCards: Flow<List<Flashcard>> = dao.getAllCards()
    
    fun getCardsByCategory(category: String): Flow<List<Flashcard>> = dao.getCardsByCategory(category)
    
    fun getDueCards(currentTime: Long): Flow<List<Flashcard>> = dao.getDueCards(currentTime)
    
    fun getTotalCount(): Flow<Int> = dao.getTotalCount()
    
    fun getDueCount(currentTime: Long): Flow<Int> = dao.getDueCount(currentTime)
    
    fun getMasteredCount(): Flow<Int> = dao.getMasteredCount()

    suspend fun insertCard(card: Flashcard) {
        dao.insertCard(card)
    }

    suspend fun updateCard(card: Flashcard) {
        dao.updateCard(card)
    }

    suspend fun deleteCard(card: Flashcard) {
        dao.deleteCard(card)
    }

    /**
     * Spaced Repetition (SM-2 Algorithm) Update
     * @param card The flashcard to update
     * @param score The quality score: 1 (Forgot), 3 (Good), 5 (Easy)
     */
    suspend fun processReview(card: Flashcard, score: Int) {
        val nextRepetitions: Int
        val nextInterval: Int
        val nextEaseFactor: Float

        if (score < 3) {
            // Incorrect recall
            nextRepetitions = 0
            nextInterval = 1
            // Slightly reduce ease factor
            nextEaseFactor = (card.easeFactor - 0.2f).coerceAtLeast(1.3f)
        } else {
            // Correct recall
            nextRepetitions = card.repetitions + 1
            nextInterval = when (nextRepetitions) {
                1 -> 1
                2 -> 3
                else -> (card.intervalDays * card.easeFactor).toInt().coerceAtLeast(1)
            }
            
            // Adjust ease factor based on score
            val adjustment = 0.1f - (5f - score) * (0.08f + (5f - score) * 0.02f)
            nextEaseFactor = (card.easeFactor + adjustment).coerceAtLeast(1.3f)
        }

        // Calculate next review timestamp
        val nextReviewMillis = System.currentTimeMillis() + (nextInterval * 24L * 60L * 60L * 1000L)

        val updatedCard = card.copy(
            repetitions = nextRepetitions,
            intervalDays = nextInterval,
            easeFactor = nextEaseFactor,
            nextReviewDate = nextReviewMillis
        )
        
        dao.updateCard(updatedCard)
    }

    /**
     * Check and prepopulate empty database with Moroccan BAC study cards.
     */
    suspend fun checkAndPrepopulate() {
        if (dao.getCount() > 0) return // Already populated

        val defaultCards = ArrayList<Flashcard>()

        // 1. Vocabulary Unit 1: Education
        defaultCards.add(Flashcard(front = "attend", back = "يحضر\n\nExample: To go to or be present at a class, meeting, or event.", category = "Vocab Unit 1", hint = "Education"))
        defaultCards.add(Flashcard(front = "enroll", back = "يسجل / ينخرط\n\nExample: To officially register or join a school, course, or list.", category = "Vocab Unit 1", hint = "Education"))
        defaultCards.add(Flashcard(front = "graduate", back = "يتخرج\n\nExample: To complete a degree or course of study at a school or college.", category = "Vocab Unit 1", hint = "Education"))
        defaultCards.add(Flashcard(front = "drop out", back = "ينقطع عن الدراسة\n\nExample: To leave school or university before completing the course.", category = "Vocab Unit 1", hint = "Education"))
        defaultCards.add(Flashcard(front = "scholarship", back = "منحة دراسية\n\nExample: Financial aid awarded to a student for further education.", category = "Vocab Unit 1", hint = "Education"))
        defaultCards.add(Flashcard(front = "literacy", back = "القدرة على القراءة والكتابة (محو الأمية)\n\nExample: The ability to read and write.", category = "Vocab Unit 1", hint = "Education"))
        defaultCards.add(Flashcard(front = "compulsory", back = "إجباري / ملزم\n\nExample: Required by law or a rule; obligatory.", category = "Vocab Unit 1", hint = "Education"))

        // 2. Vocabulary Unit 2: Immigration
        defaultCards.add(Flashcard(front = "emigrate", back = "يهاجر (مغادرة الوطن)\n\nExample: To leave one's own country to settle permanently in another.", category = "Vocab Unit 2", hint = "Immigration"))
        defaultCards.add(Flashcard(front = "immigrate", back = "يستقر (وصول لبلد جديد)\n\nExample: To come to live permanently in a foreign country.", category = "Vocab Unit 2", hint = "Immigration"))
        defaultCards.add(Flashcard(front = "brain drain", back = "هجرة الأدمغة\n\nExample: The emigration of highly trained or intelligent people from a country.", category = "Vocab Unit 2", hint = "Immigration"))
        defaultCards.add(Flashcard(front = "refugee", back = "لاجئ\n\nExample: A person forced to leave their country to escape war or persecution.", category = "Vocab Unit 2", hint = "Immigration"))
        defaultCards.add(Flashcard(front = "asylum", back = "اللجوء\n\nExample: Protection granted by a nation to someone left their native country as a refugee.", category = "Vocab Unit 2", hint = "Immigration"))

        // 3. Vocabulary Unit 4: Women & Society
        defaultCards.add(Flashcard(front = "empowerment", back = "التمكين\n\nExample: Giving people authority or power to make life-changing decisions.", category = "Vocab Unit 4", hint = "Women & Society"))
        defaultCards.add(Flashcard(front = "gender equality", back = "المساواة بين الجنسين\n\nExample: Equal rights, responsibilities, and opportunities for all genders.", category = "Vocab Unit 4", hint = "Women & Society"))
        defaultCards.add(Flashcard(front = "stereotype", back = "صورة نمطية\n\nExample: A widely held but fixed and oversimplified image or idea of a person or thing.", category = "Vocab Unit 4", hint = "Women & Society"))

        // 4. Vocabulary Unit 5: Charity & Volunteering
        defaultCards.add(Flashcard(front = "volunteer", back = "متطوع\n\nExample: A person who freely offers to take part in an enterprise or task.", category = "Vocab Unit 5", hint = "Charity"))
        defaultCards.add(Flashcard(front = "fundraising", back = "جمع التبرعات\n\nExample: The seeking of financial support for a charity, cause, or list.", category = "Vocab Unit 5", hint = "Charity"))
        defaultCards.add(Flashcard(front = "NGO", back = "منظمة غير حكومية\n\nExample: Non-Governmental Organization.", category = "Vocab Unit 5", hint = "Charity"))

        // 5. Vocabulary Unit 6: Internet & Technology
        defaultCards.add(Flashcard(front = "privacy", back = "الخصوصية\n\nExample: The state or condition of being free from public attention.", category = "Vocab Unit 6", hint = "Technology"))
        defaultCards.add(Flashcard(front = "cyberbullying", back = "التنمر الإلكتروني\n\nExample: Use of electronic communication to bully or intimidate a person.", category = "Vocab Unit 6", hint = "Technology"))
        defaultCards.add(Flashcard(front = "addiction", back = "الإدمان\n\nExample: The fact or condition of being addicted to a particular substance or activity.", category = "Vocab Unit 6", hint = "Technology"))

        // 6. Vocabulary Unit 10: Citizenship & Environment
        defaultCards.add(Flashcard(front = "deforestation", back = "إزالة الغابات\n\nExample: The action of clearing a wide area of trees.", category = "Vocab Unit 10", hint = "Environment"))
        defaultCards.add(Flashcard(front = "global warming", back = "الاحتباس الحراري\n\nExample: A gradual increase in the overall temperature of the earth's atmosphere.", category = "Vocab Unit 10", hint = "Environment"))
        defaultCards.add(Flashcard(front = "citizenship", back = "المواطنة\n\nExample: The position or status of being a citizen of a particular country.", category = "Vocab Unit 10", hint = "Citizenship"))

        // 7. Irregular Verbs
        defaultCards.add(Flashcard(front = "be", back = "Past: was / were\nPP: been\nDarija: يكون", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "become", back = "Past: became\nPP: become\nDarija: يصبح / يولّي", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "begin", back = "Past: began\nPP: begun\nDarija: يبدا", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "break", back = "Past: broke\nPP: broken\nDarija: يكسر", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "bring", back = "Past: brought\nPP: brought\nDarija: يجيب / يحضر", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "build", back = "Past: built\nPP: built\nDarija: يبني", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "buy", back = "Past: bought\nPP: bought\nDarija: يشري", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "choose", back = "Past: chose\nPP: chosen\nDarija: يختار", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "come", back = "Past: came\nPP: come\nDarija: يجي", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "do", back = "Past: did\nPP: done\nDarija: يدير", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "eat", back = "Past: ate\nPP: eaten\nDarija: ياكل", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "go", back = "Past: went\nPP: gone\nDarija: يمشي", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "have", back = "Past: had\nPP: had\nDarija: عنده", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "make", back = "Past: made\nPP: made\nDarija: يصنع", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "see", back = "Past: saw\nPP: seen\nDarija: يشوف", category = "Irregular Verbs", hint = "Irregular Verb"))
        defaultCards.add(Flashcard(front = "write", back = "Past: wrote\nPP: written\nDarija: يكتب", category = "Irregular Verbs", hint = "Irregular Verb"))

        // 8. History & Geography
        defaultCards.add(Flashcard(
            front = "ما هي العناصر الثلاثة لتحديد الإطار التاريخي للوثائق؟",
            back = "الزمان (متى وقع الحدث؟)\nالمكان (أين وقع؟)\nالموضوع (ما هي القضية الأساسية للوثيقة؟)",
            category = "History & Geography",
            hint = "منهجية الوثائق"
        ))
        defaultCards.add(Flashcard(
            front = "ما هي خطوات كتابة موضوع مقالي جغرافي؟",
            back = "1. المقدمة: التمهيد وتوطئة الإشكالية وطرح الأسئلة.\n2. العرض: مناقشة الإشكاليات بالتفصيل في فقرات منظمة وداعمة بدقة.\n3. الخاتمة: استخلاص أو نتاج عام مع فتح آفاق جغرافية بسؤال امتداد.",
            category = "History & Geography",
            hint = "منهجية المقال"
        ))
        defaultCards.add(Flashcard(
            front = "متى نلجأ إلى الرسم المبياني بالمنحنى؟",
            back = "نلجأ للمنحنى عند وجود ظاهرة متطورة في الزمن باستمرار (مثل تطور السكان أو الناتج الوطني عبر السنوات).",
            category = "History & Geography",
            hint = "منهجية المبيانات"
        ))
        
        // 9. Grammar key rules
        defaultCards.add(Flashcard(
            front = "ما الفرق بين although و despite؟",
            back = "- although: يتبعها جملة كاملة (subject + verb).\n  *مثال:* Although he studied, he failed.\n\n- despite: يتبعها اسم (noun) أو فعل+ing.\n  *مثال:* Despite his studying, he failed.",
            category = "Grammar Rules",
            hint = "Linkers Contrast"
        ))
        defaultCards.add(Flashcard(
            front = "ما هي تركيبة Future Perfect (المستقبل التام)؟",
            back = "الصيغة: will have + Past Participle (V3)\n\nتستعمل لفعل سينتهي قبل تاريخ محدد فالمستقبل.\n*مفتاح:* By next June, I will have graduated.",
            category = "Grammar Rules",
            hint = "Future Perfect"
        ))

        dao.insertCards(defaultCards)
    }
}
