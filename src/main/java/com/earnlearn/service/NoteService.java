package com.earnlearn.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.earnlearn.model.Folder;
import com.earnlearn.model.Note;
import com.earnlearn.model.NoteVersion;
import com.earnlearn.model.User;
import com.earnlearn.repository.FolderRepository;
import com.earnlearn.repository.NoteRepository;
import com.earnlearn.repository.NoteVersionRepository;

import jakarta.transaction.Transactional;

@Service
public class NoteService {
	private final NoteRepository noteRepository;
	private final FolderRepository folderRepository;
	private final NoteVersionRepository noteVersionRepository;

	// Constructor
	public NoteService(NoteRepository noteRepository, FolderRepository folderRepository,
			NoteVersionRepository noteVersionRepository) {
		this.noteRepository = noteRepository;
		this.folderRepository = folderRepository;
		this.noteVersionRepository = noteVersionRepository;
	}

	public Note saveNote(Note note, User user) {
		note.setUser(user);

		// Process tags from comma-separated string to list
		if (note.getTags() != null && !note.getTags().isEmpty()) {
			List<String> cleanedTags = Arrays.stream(note.getTags().get(0).split(",")).map(String::trim)
					.filter(tag -> !tag.isEmpty()).collect(Collectors.toList());
			note.setTags(cleanedTags);
		} else {
			note.setTags(new ArrayList<>());
		}

		// Existing folder handling
		if (note.getFolder() != null && note.getFolder().getId() != null) {
			Folder folder = folderRepository.findById(note.getFolder().getId())
					.orElseThrow(() -> new RuntimeException("Folder not found"));
			note.setFolder(folder);
		} else {
			note.setFolder(null);
		}

		// Versioning
//		if (note.getId() != null) {
			createVersion(note);
//		}

		return noteRepository.save(note);
	}

	public List<Note> getUserNotes(User user) {
		return noteRepository.findByUserAndDeletedFalse(user); // Filter by deleted=false
	}

	public Note getNoteByIdAndUser(Long id, User user) {
		return noteRepository.findByIdAndUser(id, user).orElseThrow(() -> new RuntimeException("Note not found"));
	}

	public Note updateNote(Note updatedNote, User user) {
		Note existingNote = noteRepository.findByIdAndUser(updatedNote.getId(), user)
				.orElseThrow(() -> new RuntimeException("Note not found"));
		existingNote.setTitle(updatedNote.getTitle());
		existingNote.setContent(updatedNote.getContent());
		existingNote.setBookmarked(updatedNote.isBookmarked());
		existingNote.setFolder(updatedNote.getFolder());
		return noteRepository.save(existingNote);
	}

	public List<Note> searchNotes(String keyword, User user) {
		return noteRepository.findByUserAndTitleContainingIgnoreCaseAndDeletedFalse(user, keyword);
	}

//	@Transactional
//	public void deleteNote(Long id, User user) {
//		Note note = noteRepository.findByIdAndUser(id, user).orElseThrow(() -> new RuntimeException("Note not found"));
//		note.setDeleted(true);
//		noteRepository.save(note);
//	}
	@Transactional
	public void deleteNote(Long id, User user) {
		Note note = noteRepository.findByIdAndUser(id, user).orElseThrow(() -> new RuntimeException("Note not found"));

		note.setDeleted(true);
		noteRepository.save(note);

		// Manually update folder notes count
		if (note.getFolder() != null) {
			Folder folder = note.getFolder();
			folder.getNotes().remove(note); // Remove from collection
			folderRepository.save(folder);
		}
	}

	@Transactional
	public Note saveNoteWithVersion(Note note) {
		if (note.getId() != null) {
			Note existing = noteRepository.findById(note.getId()).orElseThrow();
			NoteVersion version = new NoteVersion();
			version.setContent(existing.getContent());
			version.setVersionNumber(existing.getVersions().size() + 1);
			version.setCreatedAt(LocalDateTime.now());
			version.setNote(existing);
			existing.getVersions().add(version);
		}
		return noteRepository.save(note);
	}

	@Transactional
	public Note restoreVersion(Long versionId, User user) {
		NoteVersion version = noteVersionRepository.findById(versionId)
				.orElseThrow(() -> new RuntimeException("Version not found"));
		Note note = version.getNote();

		if (!note.getUser().equals(user)) {
			throw new RuntimeException("Unauthorized");
		}

		// Create new version from current content
		NoteVersion newVersion = new NoteVersion();
		newVersion.setContent(note.getContent());
		newVersion.setVersionNumber(note.getVersions().size() + 1);
		newVersion.setCreatedAt(LocalDateTime.now());
		newVersion.setNote(note);
		note.getVersions().add(newVersion);

		// Restore old content
		note.setContent(version.getContent());
		return noteRepository.save(note);
	}

	public Note getNoteWithVersions(Long id, User user) {
		return noteRepository.findByIdAndUserWithVersions(id, user)
				.orElseThrow(() -> new RuntimeException("Note not found"));
	}

	private void createVersion(Note note) {
		NoteVersion version = new NoteVersion();
		version.setContent(note.getContent());
		version.setVersionNumber(note.getVersions().size() + 1);
		version.setCreatedAt(LocalDateTime.now());
		version.setNote(note);
		note.getVersions().add(version);
	}

	public List<Note> getNotesByFolderAndUser(Folder folder, User user) {
		return noteRepository.findByFolderAndUserAndDeletedFalse(folder, user);
	}

	public List<Note> searchNotesInFolder(String keyword, User user, Folder folder) {
		return noteRepository.findByFolderAndUserAndTitleContainingIgnoreCaseAndDeletedFalse(folder, user, keyword);
	}

	public List<Note> getBookmarkedNotes(User user) {
		return noteRepository.findByUserAndBookmarkedTrueAndDeletedFalse(user);
	}

	public List<Note> searchBookmarkedNotes(String keyword, User user) {
		return noteRepository.findByUserAndBookmarkedTrueAndTitleContainingIgnoreCaseAndDeletedFalse(user, keyword);
	}

	@Transactional
	public void toggleBookmark(Long id, User user) {
		Note note = noteRepository.findByIdAndUser(id, user).orElseThrow(() -> new RuntimeException("Note not found"));
		note.setBookmarked(!note.isBookmarked());
		noteRepository.save(note);
	}

}
