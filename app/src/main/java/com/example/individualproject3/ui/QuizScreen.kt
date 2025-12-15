package com.example.individualproject3.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.individualproject3.logic.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavController,
    topicId: String,
    onBack: () -> Unit
) {
    val viewModel: QuizViewModel = viewModel()
    
    // Initialize quiz on entry
    LaunchedEffect(topicId) {
        viewModel.startQuiz(topicId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val soundManager = remember { com.example.individualproject3.util.SoundManager(context) }

    LaunchedEffect(uiState.isAnswerCorrect) {
        val correct = uiState.isAnswerCorrect
        if (correct == true) {
            soundManager.playCorrectSound()
        } else if (correct == false) {
            soundManager.playWrongSound()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.selectedTopic?.title ?: "Quiz") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            if (uiState.isQuizFinished) {
                QuizResultContent(
                    score = uiState.score,
                    total = uiState.selectedTopic?.questions?.size ?: 0,
                    onBack = onBack
                )
            } else {
                val currentQuestion = uiState.selectedTopic?.questions?.getOrNull(uiState.currentQuestionIndex)
                if (currentQuestion != null) {
                   QuizGameContent(
                       question = currentQuestion,
                       questionIndex = uiState.currentQuestionIndex,
                       totalQuestions = uiState.selectedTopic?.questions?.size ?: 0,
                       isAnswerCorrect = uiState.isAnswerCorrect,
                       onAnswerSelected = { index -> viewModel.submitAnswer(index) },
                       onNext = { viewModel.nextQuestion() }
                   )
                }
            }
        }
    }
}

@Composable
fun QuizGameContent(
    question: com.example.individualproject3.logic.QuizQuestion,
    questionIndex: Int,
    totalQuestions: Int,
    isAnswerCorrect: Boolean?,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Progress
        LinearProgressIndicator(
            progress = { (questionIndex + 1) / totalQuestions.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
        )
        Text(
            text = "Question ${questionIndex + 1} / $totalQuestions",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        // Question Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            )
        }

        // Options
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEachIndexed { index, option ->
                val isSelected = isAnswerCorrect != null && index == question.correctIndex // Highlight correct answer if revealed?
                // Actually simple logic: if answered, show correct/wrong feedback
                // But ViewModel logic sets isAnswerCorrect only for the submitted one? 
                // Let's assume onAnswerSelected just submits.
                // We need to disable buttons if isAnswerCorrect is not null.
                
                Button(
                    onClick = { onAnswerSelected(index) },
                    enabled = isAnswerCorrect == null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnswerCorrect != null && index == question.correctIndex) 
                            Color(0xFF66BB6A) // Green for correct
                        else if (isAnswerCorrect == false && isAnswerCorrect != null) // User selected this and it was wrong... wait we don't know WHICH one user selected in this simplified state.
                            MaterialTheme.colorScheme.primary // Fallback
                        else MaterialTheme.colorScheme.primary
                        // Complex color logic requires knowing user selection. For simplicity, let's just show Next button and simple highlight.
                    )
                ) {
                    Text(option, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Feedback / Next
        if (isAnswerCorrect != null) {
             Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAnswerCorrect) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAnswerCorrect) "Correct! 🎉" else "Oops! The correct answer was: ${question.options[question.correctIndex]}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNext) {
                        Text("Next Question")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultContent(score: Int, total: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You scored $score out of $total",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Back to Topics")
        }
    }
}


