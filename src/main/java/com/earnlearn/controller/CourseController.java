package com.earnlearn.controller;

import com.earnlearn.model.Course;
import com.earnlearn.model.Lecture;
import com.earnlearn.model.User;
import com.earnlearn.service.CourseService;
import com.earnlearn.service.EnrollmentService;
import com.earnlearn.service.QuizService;
import com.earnlearn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courses")
public class CourseController {

	@Autowired
	private CourseService courseService;

	@Autowired
	private QuizService quizService;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private UserService userService;

	private User getCurrentUser(UserDetails userDetails) {
		if (userDetails == null) {
			return null;
		}
		return userService.findByUsername(userDetails.getUsername()).orElse(null);
	}

	@GetMapping
	public String listCourses(Model model) {
		model.addAttribute("courses", courseService.getAllCourses());
		return "courses/list";
	}

	@GetMapping("/{id}")
	public String viewCourse(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
		Course course = courseService.getCourseById(id);
		model.addAttribute("course", course);
		model.addAttribute("quizzes", quizService.getQuizzesByCourse(id));

		// Check if the current user is enrolled in this course
		boolean isEnrolled = false;
		User currentUser = getCurrentUser(userDetails);
		if (currentUser != null) {
			isEnrolled = enrollmentService.isUserEnrolledInCourse(currentUser, course);
		}
		model.addAttribute("isEnrolled", isEnrolled);
		model.addAttribute("currentUser", currentUser); // Pass current user to template

		return "courses/detail";
	}

	@GetMapping("/new")
	public String showCourseForm(Model model) {
		model.addAttribute("course", new Course());
		return "courses/form";
	}

	@PostMapping("/save")
	public String saveCourse(@ModelAttribute Course course, RedirectAttributes redirectAttributes) {
		courseService.saveCourse(course);
		redirectAttributes.addFlashAttribute("successMessage", "Course saved successfully!");
		return "redirect:/courses";
	}

	@GetMapping("/{courseId}/lectures/new")
	public String showLectureForm(@PathVariable Long courseId, Model model) {
		model.addAttribute("courseId", courseId);
		model.addAttribute("lecture", new Lecture());
		return "courses/lecture-form";
	}

	@PostMapping("/{courseId}/lectures/save")
	public String saveLecture(@PathVariable Long courseId, @ModelAttribute Lecture lecture,
			RedirectAttributes redirectAttributes) {
		courseService.addLectureToCourse(courseId, lecture);
		redirectAttributes.addFlashAttribute("successMessage", "Lecture added to course successfully!");
		return "redirect:/courses/" + courseId;
	}

	@PostMapping("/delete/{id}")
	public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			courseService.deleteCourse(id);
			redirectAttributes.addFlashAttribute("successMessage", "Course deleted successfully!");
		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error deleting course: " + e.getMessage());
		}
		return "redirect:/courses";
	}
}
