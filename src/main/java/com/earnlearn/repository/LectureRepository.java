package com.earnlearn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.earnlearn.model.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
	@Query("SELECT l FROM Lecture l WHERE l.product.id = :productId ORDER BY l.lessonOrder ASC")
	List<Lecture> findByProductId(@Param("productId") Long productId);

	Lecture findById(long id);
	
	//new 1/5/25 12:10PM
	 @Query("SELECT l FROM Lecture l JOIN FETCH l.product")
	    List<Lecture> findAllWithProduct();
}