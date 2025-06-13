package com.earnlearn.repository;

import com.earnlearn.model.Question;
import com.earnlearn.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizOrderByQuestionOrderAsc(Quiz quiz);
    Optional<Question> findByIdAndQuiz(Long id, Quiz quiz);
}