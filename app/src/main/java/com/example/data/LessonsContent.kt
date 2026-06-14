package com.example.data

data class LessonContent(
    val title: String,
    val tag: String
)

val LESSONS_CONTENT = mapOf(
    "tenses" to LessonContent("⏰ Tenses — الأزمنة الكاملة", "Grammar"),
    "passive" to LessonContent("🔄 Passive Voice — المبني للمجهول", "Grammar"),
    "reported-speech" to LessonContent("💭 Reported Speech — الكلام المنقول", "Grammar"),
    "conditionals" to LessonContent("🌿 Conditionals — جمل الشرط واستخدام wish", "Grammar"),
    "modals" to LessonContent("⚡ Modals — الأفعال الناقصة", "Grammar"),
    "relative-pronouns" to LessonContent("🔀 Relative Pronouns — ضمائر الوصل", "Grammar"),
    "purpose" to LessonContent("🎯 Purpose — الغرض والهدف", "Grammar"),
    "gerund-infinitive" to LessonContent("📌 Gerund & Infinitive — المصدر و ing", "Grammar"),
    "linking-words" to LessonContent("🔗 Linking Words — الروابط اللغوية", "Grammar"),
    "phrasal-verbs" to LessonContent("🧩 Phrasal Verbs — الأفعال المركبة", "Grammar"),
    "functions" to LessonContent("🗣️ Functions — الوظائف اللغوية", "Functions"),
    "hg-overview" to LessonContent("📜 منهجية الاجتماعيات والخرائط", "Methodology"),
    "hg-documentary" to LessonContent("📋 منهجية الأسئلة الخمسة للوثائق", "Documents"),
    "hg-essay" to LessonContent("✍️ منهجية كتابة الموضوع المقالي", "Essay")
)
