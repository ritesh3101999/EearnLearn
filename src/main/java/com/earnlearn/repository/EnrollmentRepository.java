package com.earnlearn.repository;

import com.earnlearn.model.Course;
import com.earnlearn.model.Enrollment;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    // Find all enrollments for a specific user
    List<Enrollment> findByUser(User user);

    // Find a specific enrollment by user and course
    Optional<Enrollment> findByUserAndCourse(User user, Course course);

    // Check if a user is already enrolled in a specific course
    boolean existsByUserAndCourse(User user, Course course);

    // Find all enrollments for a specific course
    List<Enrollment> findByCourse(Course course);

    // Find enrollments by user and status
    List<Enrollment> findByUserAndStatus(User user, Enrollment.EnrollmentStatus status);
}
