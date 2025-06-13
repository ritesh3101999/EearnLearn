package com.earnlearn.service;

import com.earnlearn.model.Course;
import com.earnlearn.model.Lecture;
import com.earnlearn.model.Quiz;
import com.earnlearn.repository.CourseRepository;
import com.earnlearn.repository.LectureRepository;
import com.earnlearn.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {
	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private LectureRepository lectureRepository;

	@Autowired
	private QuizRepository quizRepository;

	public List<Course> getAllCourses() {
		return courseRepository.findAll();
	}

	public Course getCourseById(Long id) {
		return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
	}

	public Course saveCourse(Course course) {
		return courseRepository.save(course);
	}

	public void addLectureToCourse(Long courseId, Lecture lecture) {
		Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
		lecture.setCourse(course);
		lectureRepository.save(lecture);
	}

	// Methods for Quiz management within a Course
	@Transactional
	public Quiz addQuizToCourse(Long courseId, Quiz quiz) {
		Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
		quiz.setCourse(course);
		return quizRepository.save(quiz);
	}

	public List<Quiz> getQuizzesForCourse(Long courseId) {
		Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
		return quizRepository.findByCourse(course);
	}

	// Method to delete a course
	@Transactional
	public void deleteCourse(Long id) {
		if (!courseRepository.existsById(id)) {
			throw new RuntimeException("Course not found with ID: " + id);
		}
		courseRepository.deleteById(id);
	}
}