package com.example.individualproject3.logic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Eco
import androidx.compose.ui.graphics.vector.ImageVector

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

data class QuizTopic(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val questions: List<QuizQuestion>
)

object QuizRepository {
    private val plantsQuestions = listOf(
        QuizQuestion(
            text = "What do plants need to grow?",
            options = listOf("Candy", "Sunlight & Water", "Video Games", "Rocks"),
            correctIndex = 1
        ),
        QuizQuestion(
            text = "Which part of the plant grows underground?",
            options = listOf("Leaves", "Flowers", "Roots", "Stem"),
            correctIndex = 2
        ),
        QuizQuestion(
            text = "What color are most plant leaves?",
            options = listOf("Blue", "Red", "Green", "Purple"),
            correctIndex = 2
        ),
        QuizQuestion(
            text = "What do bees carry from flower to flower?",
            options = listOf("Pollen", "Honey", "Seeds", "Water"),
            correctIndex = 0
        ),
        QuizQuestion(
            text = "Which of these is a fruit?",
            options = listOf("Carrot", "Potato", "Apple", "Spinach"),
            correctIndex = 2
        )
    )

    private val bugsQuestions = listOf(
        QuizQuestion(
            text = "How many legs does an insect have?",
            options = listOf("2", "4", "6", "8"),
            correctIndex = 2
        ),
        QuizQuestion(
            text = "What do caterpillars turn into?",
            options = listOf("Spiders", "Butterflies", "Beetles", "Flies"),
            correctIndex = 1
        ),
        QuizQuestion(
            text = "Which bug makes honey?",
            options = listOf("Ant", "Bee", "Ladybug", "Mosquito"),
            correctIndex = 1
        ),
        QuizQuestion(
            text = "Which bug spins a web?",
            options = listOf("Spider", "Ant", "Grasshopper", "Beetle"),
            correctIndex = 0
        ),
        QuizQuestion(
            text = "What do ladybugs eat?",
            options = listOf("Leaves", "Aphids", "Dirt", "Rocks"),
            correctIndex = 1
        )
    )

    val topics = listOf(
        QuizTopic("plants", "Plants", Icons.Default.Eco, plantsQuestions),
        QuizTopic("bugs", "Bugs", Icons.Default.BugReport, bugsQuestions)
    )

    fun getTopic(id: String): QuizTopic? {
        return topics.find { it.id == id }
    }
}
