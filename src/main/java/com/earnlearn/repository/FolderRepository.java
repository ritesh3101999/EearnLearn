package com.earnlearn.repository;

import com.earnlearn.model.Folder;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
	List<Folder> findByUser(User user);

	Optional<Folder> findByIdAndUser(Long id, User user);

	@Query("SELECT f FROM Folder f LEFT JOIN FETCH f.notes n WHERE f.user = :user")
	List<Folder> findByUserWithNotes(User user);
}