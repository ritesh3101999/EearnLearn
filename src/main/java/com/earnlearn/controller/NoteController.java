package com.earnlearn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.earnlearn.model.Folder;
import com.earnlearn.model.Note;
import com.earnlearn.model.User;
import com.earnlearn.service.FolderService;
import com.earnlearn.service.NoteService;
import com.earnlearn.service.UserService;
import com.earnlearn.service.ActivityService;

@Controller
@RequestMapping("/notes")
public class NoteController {

	@Autowired
	private ActivityService activityService;

	private final NoteService noteService;
	private final UserService userService;
	private final FolderService folderService;

	public NoteController(NoteService noteService, UserService userService, FolderService folderService) {
		this.noteService = noteService;
		this.userService = userService;
		this.folderService = folderService;
	}

	@GetMapping
	public String listNotes(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(required = false) Long folder, @RequestParam(required = false) Boolean bookmarked,
			String keyword, Model model) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		List<Note> notes = (keyword != null && !keyword.isEmpty()) ? noteService.searchNotes(keyword, user)
				: noteService.getUserNotes(user);
		if (Boolean.TRUE.equals(bookmarked)) {
			// Get bookmarked notes
			notes = (keyword != null && !keyword.isEmpty()) ? noteService.searchBookmarkedNotes(keyword, user)
					: noteService.getBookmarkedNotes(user);
		} else if (folder != null) {
			// Get notes for specific folder
			Folder selectedFolder = folderService.getFolderByIdAndUser(folder, user);
			notes = (keyword != null && !keyword.isEmpty())
					? noteService.searchNotesInFolder(keyword, user, selectedFolder)
					: noteService.getNotesByFolderAndUser(selectedFolder, user);
		} else {
			// Get all notes
			notes = (keyword != null && !keyword.isEmpty()) ? noteService.searchNotes(keyword, user)
					: noteService.getUserNotes(user);
		}
		model.addAttribute("notes", notes);
		model.addAttribute("isBookmarkedView", Boolean.TRUE.equals(bookmarked));
		model.addAttribute("folders", folderService.getUserFolders(user));
		model.addAttribute("note", new Note());
		model.addAttribute("keyword", keyword);
		model.addAttribute("currentUser", user);
		// Calculate bookmarks count
		int bookmarksCount = noteService.getBookmarkedNotes(user).size();
		model.addAttribute("bookmarksCount", bookmarksCount);
		model.addAttribute("newFolder", new Folder());
		return "notes/list";
	}

	@PostMapping("/save")
	public String saveNote(@ModelAttribute Note note, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Note savedNote = noteService.saveNote(note, user); // Get the saved note to access its ID
		redirectAttributes.addFlashAttribute("successMessage", "Note saved successfully!");

		// Log activity
		String description = "You created a new note: '" + savedNote.getTitle() + "'";
		String link = "/notes/edit/" + savedNote.getId();
		activityService.createActivity(user, "New Note", description, link);

		return "redirect:/notes";
	}

	@PostMapping("/bookmark/{id}")
	public String toggleBookmark(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		noteService.toggleBookmark(id, user);
		redirectAttributes.addFlashAttribute("successMessage", "Bookmark status updated!");
		return "redirect:/notes";
	}

	@GetMapping("/edit/{id}")
	public String editNoteForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Note note = noteService.getNoteWithVersions(id, user);
		model.addAttribute("note", note);
		model.addAttribute("folders", folderService.getUserFolders(user));
		return "notes/edit";
	}

	@PostMapping("/update/{id}")
	public String updateNote(@PathVariable Long id, @ModelAttribute Note note,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		note.setId(id); // Ensure ID is set for update
		noteService.saveNote(note, user);
		redirectAttributes.addFlashAttribute("successMessage", "Note updated successfully!");
		return "redirect:/notes";
	}

	@PostMapping("/delete/{id}")
	public String deleteNote(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		noteService.deleteNote(id, user);
		redirectAttributes.addFlashAttribute("successMessage", "Note deleted successfully!");
		return "redirect:/notes";
	}

	@PostMapping("/restore/{versionId}")
	public String restoreVersion(@PathVariable Long versionId, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Note note = noteService.restoreVersion(versionId, user);
		redirectAttributes.addFlashAttribute("successMessage", "Note restored to selected version!");
		return "redirect:/notes/edit/" + note.getId();
	}
}
