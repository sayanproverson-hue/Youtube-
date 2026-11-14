package com.example.data.ai

import kotlinx.coroutines.delay

data class AiTutorResponse(
    val title: String,
    val answerText: String,
    val keyPoints: List<String>,
    val isPreviewPlaceholder: Boolean = true
)

interface AiRepository {
    suspend fun askStudyDoubt(subject: String, question: String): AiTutorResponse
    suspend fun generateStudyPlan(examTrack: String, dailyHours: Int): AiTutorResponse
}

/**
 * Placeholder implementation used when Firebase AI Logic is not enabled.
 * Routed behind [AiRepository] so it can be swapped cleanly for Firebase.ai once provisioned.
 */
class PlaceholderAiRepository : AiRepository {

    override suspend fun askStudyDoubt(subject: String, question: String): AiTutorResponse {
        delay(650)
        val qLower = question.lowercase()
        val topicHint = when {
            "heart" in qLower || "blood" in qLower || "রক্ত" in qLower || "হৃৎপিণ্ড" in qLower ->
                "Circulatory System & Blood Groups"
            "hormone" in qLower || "neuron" in qLower || "brain" in qLower || "হরমোন" in qLower ->
                "Control & Coordination (Endocrine & Nervous System)"
            "train" in qLower || "speed" in qLower || "percentage" in qLower || "profit" in qLower ->
                "Quantitative Aptitude & Ratio Shortcuts"
            "railway" in qLower || "constitution" in qLower || "article" in qLower ->
                "Indian Polity & Railway General Awareness"
            else -> subject
        }

        return AiTutorResponse(
            title = "Sample AI Explanation • $topicHint",
            answerText = buildString {
                appendLine("[Sample Study Response for \"$question\"]")
                appendLine()
                appendLine("১. মূল ধারণা (Core Concept): $topicHint পরীক্ষায় উচ্চ-গুরুত্বপূর্ণ অধ্যায়। প্রশ্নটি সমাধান করার সময় মূল সূত্র ও সংজ্ঞাগুলো ধাপে ধাপে যাচাই করুন।")
                appendLine("2. Exam Tip: Focus on units, elimination of wrong options, and negative marking safety (0.25 / 0.33 marks per wrong answer).")
                appendLine("3. Recommended Target SSB Module: Review the corresponding chapter PDF Note and solve the 10-question practice mock test.")
            },
            keyPoints = listOf(
                "Topic identified: $topicHint ($subject)",
                "Practice 15–20 previous year MCQs from this chapter",
                "Revise formula/diagram sheet in Target SSB PDF Library"
            ),
            isPreviewPlaceholder = true
        )
    }

    override suspend fun generateStudyPlan(examTrack: String, dailyHours: Int): AiTutorResponse {
        delay(650)
        return AiTutorResponse(
            title = "Sample 7-Day AI Study Plan • $examTrack ($dailyHours hrs/day)",
            answerText = buildString {
                appendLine("[Sample Study Schedule – $examTrack]")
                appendLine()
                appendLine("• Morning (${(dailyHours * 0.4).toInt().coerceAtLeast(1)} hr): Watch 1 Recorded Video + Read Chapter PDF Notes")
                appendLine("• Afternoon (${(dailyHours * 0.3).toInt().coerceAtLeast(1)} hr): Solve 1 Timed Online Mock Test & Review Wrong Answers")
                appendLine("• Evening (${(dailyHours * 0.3).toInt().coerceAtLeast(1)} hr): Attend Live Class & Revise Formula / GK One-Liners")
            },
            keyPoints = listOf(
                "40% Concept Building (Live & Recorded)",
                "35% Active MCQ Practice (Mock Tests & Papers)",
                "25% Daily Bengali + English Revision"
            ),
            isPreviewPlaceholder = true
        )
    }
}
