package com.earnlearn.service;

import com.earnlearn.model.Course;
import com.earnlearn.model.Question;
import com.earnlearn.model.Quiz;
import com.earnlearn.repository.QuestionRepository;
import com.earnlearn.repository.QuizRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CourseService courseService; // To fetch Course by ID

    /**
     * Retrieves a quiz by its ID.
     * @param quizId The ID of the quiz.
     * @return The Quiz object.
     * @throws RuntimeException if the quiz is not found.
     */
    public Quiz getQuizById(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));
    }

    /**
     * Retrieves all quizzes for a specific course.
     * @param courseId The ID of the course.
     * @return A list of Quiz objects.
     */
    public List<Quiz> getQuizzesByCourse(Long courseId) {
        Course course = courseService.getCourseById(courseId);
        return quizRepository.findByCourse(course);
    }

    /**
     * Saves a new quiz or updates an existing one.
     * @param quiz The Quiz object to save.
     * @param courseId The ID of the course this quiz belongs to.
     * @return The saved Quiz object.
     */
    @Transactional
    public Quiz saveQuiz(Quiz quiz, Long courseId) {
        Course course = courseService.getCourseById(courseId);
        quiz.setCourse(course);
        return quizRepository.save(quiz);
    }

    /**
     * Deletes a quiz by its ID.
     * @param quizId The ID of the quiz to delete.
     */
    @Transactional
    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }

    /**
     * Retrieves a question by its ID within a specific quiz.
     * @param questionId The ID of the question.
     * @param quizId The ID of the quiz the question belongs to.
     * @return The Question object.
     * @throws RuntimeException if the question or quiz is not found, or question doesn't belong to quiz.
     */
    public Question getQuestionById(Long questionId, Long quizId) {
        Quiz quiz = getQuizById(quizId);
        return questionRepository.findByIdAndQuiz(questionId, quiz)
                .orElseThrow(() -> new RuntimeException("Question not found or does not belong to quiz."));
    }

    /**
     * Retrieves all questions for a specific quiz, ordered by question order.
     * @param quizId The ID of the quiz.
     * @return A list of Question objects.
     */
    public List<Question> getQuestionsByQuiz(Long quizId) {
        Quiz quiz = getQuizById(quizId);
        return questionRepository.findByQuizOrderByQuestionOrderAsc(quiz);
    }

    /**
     * Saves a new question or updates an existing one.
     * @param question The Question object to save.
     * @param quizId The ID of the quiz this question belongs to.
     * @return The saved Question object.
     */
    @Transactional
    public Question saveQuestion(Question question, Long quizId) {
        Quiz quiz = getQuizById(quizId);
        question.setQuiz(quiz);
        // If it's a new question, set its order to be the next in sequence
        if (question.getId() == null) {
            int nextOrder = questionRepository.findByQuizOrderByQuestionOrderAsc(quiz).size() + 1;
            question.setQuestionOrder(nextOrder);
        }
        return questionRepository.save(question);
    }

    /**
     * Deletes a question by its ID.
     * @param questionId The ID of the question to delete.
     */
    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    /**
     * Evaluates a submitted quiz and returns the score.
     * @param quizId The ID of the quiz to evaluate.
     * @param submittedAnswers A map where key is question ID and value is the index of the selected option.
     * @return The score (number of correct answers).
     */
    public int evaluateQuiz(Long quizId, Map<Long, Integer> submittedAnswers) {
        Quiz quiz = getQuizById(quizId);
        List<Question> questions = quiz.getQuestions(); // Assuming questions are eagerly fetched or loaded via getQuestionsByQuiz

        int score = 0;
        for (Question question : questions) {
            if (submittedAnswers.containsKey(question.getId())) {
                Integer submittedOptionIndex = submittedAnswers.get(question.getId());
                if (question.getCorrectOptionIndex() != null &&
                    question.getCorrectOptionIndex().equals(submittedOptionIndex)) {
                    score++;
                }
            }
        }
        return score;
    }
}