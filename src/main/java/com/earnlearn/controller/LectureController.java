package com.earnlearn.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import com.earnlearn.model.Lecture;
import com.earnlearn.model.Product;
import com.earnlearn.service.LectureService;
import com.earnlearn.service.ProductService;

@Controller
@RequestMapping("/products/{productId}/lectures")
public class LectureController {
	private final LectureService lectureService;
	private final ProductService productService;

	public LectureController(LectureService lectureService, ProductService productService) {
		this.lectureService = lectureService;
		this.productService = productService;
	}

	@GetMapping
	public String showLectures(@PathVariable Long productId, Model model) {
		Product product = productService.getProductById(productId);
		List<Lecture> lectures = lectureService.getLecturesForProduct(productId);
		model.addAttribute("product", product);
		model.addAttribute("lectures", lectures);
		return "lectures/view";
	}

	@GetMapping("/new")
	public String showLectureForm(@PathVariable Long productId, Model model) {
		model.addAttribute("lecture", new Lecture());
		return "lectures/form";
	}

	@PostMapping("/save")
	public String saveLecture(@PathVariable Long productId, @ModelAttribute Lecture lecture,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		Product product = productService.getProductById(productId);
		if (!product.getSeller().getUsername().equals(userDetails.getUsername())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized access to save lecture.");
			throw new SecurityException("Unauthorized access");
		}
		lectureService.saveLecture(productId, lecture);
		redirectAttributes.addFlashAttribute("successMessage", "Lecture saved successfully!");
		return "redirect:/products/" + productId + "/lectures";
	}

	@GetMapping("/edit/{lectureId}")
	public String editLectureForm(@PathVariable Long productId, @PathVariable Long lectureId, Model model) {
		Lecture lecture = lectureService.getLectureById(lectureId);
		model.addAttribute("lecture", lecture);
		return "lectures/form";
	}

	@PostMapping("/update")
	public String updateLecture(@PathVariable Long productId, @ModelAttribute Lecture lecture,
			RedirectAttributes redirectAttributes) {
		lectureService.saveLecture(productId, lecture);
		redirectAttributes.addFlashAttribute("successMessage", "Lecture updated successfully!");
		return "redirect:/products/" + productId + "/lectures";
	}

	@GetMapping("/delete/{lectureId}")
	public String deleteLecture(@PathVariable Long productId, @PathVariable Long lectureId,
			RedirectAttributes redirectAttributes) {
		lectureService.deleteLecture(lectureId);
		redirectAttributes.addFlashAttribute("successMessage", "Lecture deleted successfully!");
		return "redirect:/products/" + productId + "/lectures";
	}
}
