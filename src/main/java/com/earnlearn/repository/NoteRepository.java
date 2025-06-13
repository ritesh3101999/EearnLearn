package com.earnlearn.repository;

import com.earnlearn.model.Folder;
import com.earnlearn.model.Note;
import com.earnlearn.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
	@Query("SELECT n FROM Note n LEFT JOIN FETCH n.versions WHERE n.id = :id AND n.user = :user")
	Optional<Note> findByIdAndUserWithVersions(@Param("id") Long id, @Param("user") User user);

	List<Note> findByUser(User user);

	List<Note> findByFolderAndUserAndDeletedFalse(Folder folder, User user);

	List<Note> findByFolderAndUserAndTitleContainingIgnoreCaseAndDeletedFalse(Folder folder, User user, String keyword);

	List<Note> findByFolderAndUser(Folder folder, User user);

	List<Note> findByBookmarkedAndUser(boolean bookmarked, User user);

	List<Note> findByUserAndBookmarkedTrueAndDeletedFalse(User user);

	List<Note> findByUserAndBookmarkedTrueAndTitleContainingIgnoreCaseAndDeletedFalse(User user, String keyword);

	Optional<Note> findByIdAndUser(Long id, User user);

	List<Note> findByUserAndDeletedFalse(User user);

	List<Note> findByBookmarkedAndUserAndDeletedFalse(boolean bookmarked, User user);

	Optional<Note> findByIdAndUserAndDeletedFalse(Long id, User user);

	List<Note> findByUserAndTitleContainingIgnoreCaseAndDeletedFalse(User user, String keyword);

}