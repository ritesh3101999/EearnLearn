package com.earnlearn.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.earnlearn.model.Course;
import com.earnlearn.model.Lecture;
import com.earnlearn.model.Product;
import com.earnlearn.repository.LectureRepository;

import jakarta.transaction.Transactional;

@Service
public class LectureService {
	private final LectureRepository lectureRepository;
	private final ProductService productService;
	private final CourseService courseService;

	public LectureService(LectureRepository lectureRepository, ProductService productService,
			CourseService courseService) {
		this.lectureRepository = lectureRepository;
		this.productService = productService;
		this.courseService = courseService;
	}

	public List<Lecture> getLecturesForProduct(Long productId) {
		return lectureRepository.findByProductId(productId);
	}

	@Transactional
	public void saveLecture(Long productId, Lecture lecture) {
		Product product = productService.getProductById(productId);
		lecture.setProduct(product);
		lectureRepository.save(lecture);
	}

	public Lecture getLectureById(Long lectureId) {
		return lectureRepository.findById(lectureId)
				.orElseThrow(() -> new RuntimeException("Lecture not found with id: " + lectureId));
	}

	@Transactional
	public void deleteLecture(Long lectureId) {
		lectureRepository.deleteById(lectureId);
	}

	public void saveCourseLecture(Long courseId, Lecture lecture) {
		Course course = courseService.getCourseById(courseId);
		lecture.setCourse(course);
		lectureRepository.save(lecture);
	}
	
	public List<Lecture> getAllLectures() {
        return lectureRepository.findAllWithProduct();
    }
}
