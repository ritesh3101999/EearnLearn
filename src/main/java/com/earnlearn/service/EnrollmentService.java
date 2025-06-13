package com.earnlearn.service;

import com.earnlearn.model.Course;
import com.earnlearn.model.Enrollment;
import com.earnlearn.model.User;
import com.earnlearn.repository.CourseRepository;
import com.earnlearn.repository.EnrollmentRepository;
import com.earnlearn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EnrollmentService {
	@Autowired
	private EnrollmentRepository enrollmentRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CourseRepository courseRepository;

	@Transactional
	public Enrollment enrollUserInCourse(Long userId, Long courseId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
		if (enrollmentRepository.existsByUserAndCourse(user, course)) {
			throw new IllegalStateException("User is already enrolled in this course.");
		}
		Enrollment enrollment = new Enrollment(user, course);
		return enrollmentRepository.save(enrollment);
	}

	public List<Enrollment> getUserEnrollments(User user) {
		return enrollmentRepository.findByUser(user);
	}

	public boolean isUserEnrolledInCourse(User user, Course course) {
		return enrollmentRepository.existsByUserAndCourse(user, course);
	}

	@Transactional
	public Enrollment updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus newStatus, Long userId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + enrollmentId));
		// Basic authorization: ensure the user updating the enrollment is the enrolled
		// user
		if (!enrollment.getUser().getId().equals(userId)) {
			throw new SecurityException("Unauthorized to update this enrollment.");
		}

		// MODIFIED: Prevent re-completion and re-issuing certificate
		if (enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED
				&& newStatus == Enrollment.EnrollmentStatus.COMPLETED) {
			throw new IllegalStateException("Course has already been marked as completed.");
		}

		enrollment.setStatus(newStatus);
		return enrollmentRepository.save(enrollment);
	}

	@Transactional
	public void deleteEnrollment(Long enrollmentId, Long userId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + enrollmentId));
		// Basic authorization: ensure the user deleting the enrollment is the enrolled
		// user
		if (!enrollment.getUser().getId().equals(userId)) {
			throw new SecurityException("Unauthorized to delete this enrollment.");
		}
		enrollmentRepository.delete(enrollment);
	}
}