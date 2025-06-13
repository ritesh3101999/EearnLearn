package com.earnlearn.repository;

import com.earnlearn.model.Course;
import com.earnlearn.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourse(Course course);
    Optional<Quiz> findByIdAndCourse(Long id, Course course);
}