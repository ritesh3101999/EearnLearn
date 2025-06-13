package com.earnlearn.controller;

import com.earnlearn.model.Course;
import com.earnlearn.model.Enrollment;
import com.earnlearn.model.User;
import com.earnlearn.service.CertificateService;
import com.earnlearn.service.EnrollmentService;
import com.earnlearn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import com.earnlearn.service.ActivityService;
import com.earnlearn.service.CourseService;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

	@Autowired
	private ActivityService activityService;
	
	@Autowired
	private CourseService courseService;
	
	@Autowired
	private EnrollmentService enrollmentService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private CertificateService certificateService; // ADDED

	private User getCurrentUser(UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException(
					"User details not available. Ensure the endpoint is secured and the user is authenticated.");
		}
		return userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found for username: " + userDetails.getUsername()));
	}

	@PostMapping("/enroll/{courseId}")
	public String enrollInCourse(@PathVariable Long courseId, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User currentUser = getCurrentUser(userDetails);
		try {
			enrollmentService.enrollUserInCourse(currentUser.getId(), courseId);
			redirectAttributes.addFlashAttribute("successMessage", "Successfully enrolled in the course!");
			
			// Log activity
	        Course course = courseService.getCourseById(courseId);
	        String description = "You enrolled in a new course: '" + course.getTitle() + "'";
	        String link = "/courses/" + courseId;
	        activityService.createActivity(currentUser, "Course Enrollment", description, link);
	        
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error enrolling in course: " + e.getMessage());
		}
		return "redirect:/courses/" + courseId; // Redirect back to course detail page
	}

	@GetMapping("/my-courses")
	public String listMyCourses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User currentUser = getCurrentUser(userDetails);
		List<Enrollment> enrollments = enrollmentService.getUserEnrollments(currentUser);
		model.addAttribute("enrollments", enrollments);
		model.addAttribute("pageTitle", "My Enrolled Courses");
		return "enrollments/list";
	}

	@PostMapping("/drop/{enrollmentId}")
	public String dropCourse(@PathVariable Long enrollmentId, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User currentUser = getCurrentUser(userDetails);
		try {
			enrollmentService.deleteEnrollment(enrollmentId, currentUser.getId());
			redirectAttributes.addFlashAttribute("successMessage", "Successfully dropped the course.");
		} catch (SecurityException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to drop this enrollment.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error dropping course: " + e.getMessage());
		}
		return "redirect:/enrollments/my-courses"; // Redirect back to my courses page
	}

	@PostMapping("/complete/{enrollmentId}")
	public String completeCourse(@PathVariable Long enrollmentId, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User currentUser = getCurrentUser(userDetails);
		try {
			Enrollment enrollment = enrollmentService.updateEnrollmentStatus(enrollmentId,
					Enrollment.EnrollmentStatus.COMPLETED, currentUser.getId());
			Course course = enrollment.getCourse();

			// Generate certificate upon course completion
			certificateService.generateCertificate(currentUser, course);

			redirectAttributes.addFlashAttribute("successMessage",
					"Course marked as completed! Your certificate has been issued.");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("infoMessage", "Course already completed and certificate issued.");
		} catch (SecurityException e) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"You are not authorized to mark this enrollment as completed.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Error marking course as completed: " + e.getMessage());
		}
		return "redirect:/enrollments/my-courses";
	}
}