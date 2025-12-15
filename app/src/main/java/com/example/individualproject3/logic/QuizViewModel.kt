package com.example.individualproject3.logic

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val isQuizFinished: Boolean = false,
    val selectedTopic: QuizTopic? = null,
    val isAnswerCorrect: Boolean? = null // null = not answered, true = correct, false = wrong
)

class QuizViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    fun startQuiz(topicId: String) {
        val topic = QuizRepository.getTopic(topicId)
        if (topic != null) {
            _uiState.value = QuizUiState(selectedTopic = topic)
        }
    }

    fun submitAnswer(answerIndex: Int) {
        val currentState = _uiState.value
        val topic = currentState.selectedTopic ?: return
        if (currentState.isQuizFinished) return
        
        // Prevent multiple answers for same question if we wanted to show feedback first
        // But for simplicity, let's just move to next or simple feedback
        
        val currentQuestion = topic.questions[currentState.currentQuestionIndex]
        val isCorrect = answerIndex == currentQuestion.correctIndex
        
        _uiState.update { state ->
            state.copy(
                score = if (isCorrect) state.score + 1 else state.score,
                isAnswerCorrect = isCorrect
            )
        }
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val topic = currentState.selectedTopic ?: return
        
        if (currentState.currentQuestionIndex < topic.questions.size - 1) {
            _uiState.update { state ->
                state.copy(
                    currentQuestionIndex = state.currentQuestionIndex + 1,
                    isAnswerCorrect = null
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(isQuizFinished = true)
            }
        }
    }

    fun resetQuiz() {
        _uiState.value = QuizUiState()
    }
}
