package com.earnlearn.controller;

import com.earnlearn.model.Product;
import com.earnlearn.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MarketplaceController {

	@Autowired
	private ProductService productService;

	@GetMapping("/marketplace")
	public String showMarketplace(@RequestParam(required = false) String category,
			@RequestParam(required = false) String keyword, Model model) {

		List<Product> products;

		if (category != null && !category.isEmpty()) {
			products = productService.getProductsByCategory(category);
		} else if (keyword != null && !keyword.isEmpty()) {
			products = productService.getAllProducts().stream()
					.filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase())).toList(); // Java 17+
		} else {
			products = productService.getAllProducts();
		}

		model.addAttribute("products", products);
		model.addAttribute("categories",
				productService.getAllProducts().stream().map(Product::getCategory).distinct().toList());
		model.addAttribute("selectedCategory", category);
		model.addAttribute("keyword", keyword);

		return "marketplace";
	}
}
