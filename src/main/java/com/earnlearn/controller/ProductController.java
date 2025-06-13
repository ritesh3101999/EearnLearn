package com.earnlearn.controller;

import com.earnlearn.model.Lecture;
import com.earnlearn.model.Product;
import com.earnlearn.model.Review;
import com.earnlearn.model.User;
import com.earnlearn.service.LectureService;
import com.earnlearn.service.ProductService;
import com.earnlearn.service.ReviewService;
import com.earnlearn.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private UserService userService;

	@Autowired
	private LectureService lectureService;

	@GetMapping("/new")
	public String showProductForm(Model model) {
		model.addAttribute("product", new Product());
		return "products/form";
	}

	@PostMapping("/save")
	public String saveProduct(@ModelAttribute Product product, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User seller = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		productService.saveProduct(product, seller);
		redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully!");
		return "redirect:/products";
	}

	@GetMapping
	public String listProducts(Model model, String category) {
		model.addAttribute("categories",
				productService.getAllProducts().stream().map(Product::getCategory).distinct().toList());
		model.addAttribute("selectedCategory", category);
		model.addAttribute("products", productService.getAllProducts());
		return "products/list";
	}

	@GetMapping("/seller")
	public String listSellerProducts(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		User seller = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("products", productService.getProductsBySeller(seller));
		return "products/seller-list";
	}

	@GetMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		productService.deleteProduct(id);
		redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
		return "redirect:/products/seller";
	}

	// Add this method
	public Product saveProduct(Product product) {
		return productService.saveProduct(product);
	}

	// new method for review
	@GetMapping("/{id}")
	public String viewProduct(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
		// Get product, reviews, and lectures
		Product product = productService.getProductById(id);
		List<Review> reviews = reviewService.getProductReviews(id);
		List<Lecture> lectures = lectureService.getLecturesForProduct(id);

		// Determine if the current authenticated user is the seller of this product
		// or if they are an ADMIN.
		// This 'isSeller' flag will be used in the Thymeleaf template for conditional rendering.
		boolean isSeller = false;
		User currentUser = null;
		if (userDetails != null) {
			currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("Authenticated user not found"));
			if (product.getSeller() != null && product.getSeller().getUsername().equals(userDetails.getUsername())) {
				isSeller = true;
			}
			// Also check if the current user has the 'ADMIN' role
			if (currentUser.hasRole("ADMIN")) {
				isSeller = true; // Admins can also manage products/lectures
			}
		}

		model.addAttribute("product", product);
		model.addAttribute("reviews", reviews);
		model.addAttribute("lectures", lectures);
		model.addAttribute("newReview", new Review());
		model.addAttribute("isSeller", isSeller); // Pass the flag to the template
		model.addAttribute("currentUser", currentUser); // Pass current user for role checks

		return "products/detail";
	}
}
