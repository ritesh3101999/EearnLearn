package com.earnlearn.controller;

import com.earnlearn.model.Folder;
import com.earnlearn.service.FolderService;
import com.earnlearn.model.Note;
import com.earnlearn.model.User;
import com.earnlearn.service.NoteService;
import com.earnlearn.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class ApiNoteController {

	private final NoteService noteService;
	private final UserService userService;
	private final FolderService folderService;

	public ApiNoteController(NoteService noteService, UserService userService, FolderService folderService) {
		this.noteService = noteService;
		this.userService = userService;
		this.folderService = folderService;
	}

	private User getCurrentUser(UserDetails userDetails) {
		return userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	@GetMapping
	public List<Note> getUserNotes(@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available. Ensure the endpoint is secured.");
		}
		return noteService.getUserNotes(getCurrentUser(userDetails));
	}

	@PostMapping
	public Note createNote(@RequestBody Note note, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available.");
		}
		return noteService.saveNote(note, getCurrentUser(userDetails));
	}

	@PutMapping("/{id}")
	public Note updateNote(@PathVariable Long id, @RequestBody Note note,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available.");
		}
		note.setId(id);
		return noteService.saveNote(note, getCurrentUser(userDetails));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteNote(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available.");
		}
		noteService.deleteNote(id, getCurrentUser(userDetails));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/bookmark/{id}")
	public Note toggleBookmark(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available.");
		}
		noteService.toggleBookmark(id, getCurrentUser(userDetails));
		return noteService.getNoteByIdAndUser(id, getCurrentUser(userDetails));
	}

	@GetMapping("/{id}")
	public Note getNoteWithVersions(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available.");
		}
		return noteService.getNoteWithVersions(id, getCurrentUser(userDetails));
	}

	@GetMapping("/search")
	public List<Note> searchNotes(@RequestParam String keyword, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available.");
		}
		return noteService.searchNotes(keyword, getCurrentUser(userDetails));
	}

	@GetMapping("/bookmarked")
	public List<Note> getBookmarkedNotes(@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available.");
		}
		return noteService.getBookmarkedNotes(getCurrentUser(userDetails));
	}

	@GetMapping("/folder/{folderId}")
	public List<Note> getNotesByFolder(@PathVariable Long folderId, @AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException("User details not available for fetching notes by folder.");
		}
		User currentUser = getCurrentUser(userDetails);
		Folder folder = folderService.getFolderByIdAndUser(folderId, currentUser);
		if (folder == null) {
			throw new RuntimeException("Folder not found or not accessible by user.");
		}
		return noteService.getNotesByFolderAndUser(folder, currentUser);
	}
}