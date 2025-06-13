package com.earnlearn.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import com.earnlearn.model.Folder;
import com.earnlearn.model.User;
import com.earnlearn.service.FolderService;
import com.earnlearn.service.UserService;

@Controller
@RequestMapping("/folders")
public class FolderController {
	private final FolderService folderService;
	private final UserService userService;

	public FolderController(FolderService folderService, UserService userService) {
		this.folderService = folderService;
		this.userService = userService;
	}

	@PostMapping("/save")
	public String saveFolder(@ModelAttribute Folder folder, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) { 
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		folderService.saveFolder(folder, user);
		redirectAttributes.addFlashAttribute("successMessage", "Folder created successfully!"); 
		return "redirect:/notes";
	}
}
