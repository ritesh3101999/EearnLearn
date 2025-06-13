package com.earnlearn.service;

import com.earnlearn.model.Product;
import com.earnlearn.model.User;
import com.earnlearn.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	public Product saveProduct(Product product, User seller) {
		product.setSeller(seller);
		return productRepository.save(product);
	}

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	public List<Product> getProductsBySeller(User seller) {
		return productRepository.findBySeller(seller);
	}

	public List<Product> getProductsByCategory(String category) {
		return productRepository.findByCategory(category);
	}

	public void deleteProduct(Long id) {
		productRepository.deleteById(id);
	}

	// Add this method
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}

	// new method for review
	public Product getProductById(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
	}
}