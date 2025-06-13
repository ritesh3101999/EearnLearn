package com.earnlearn.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.earnlearn.model.Folder;
import com.earnlearn.model.User;
import com.earnlearn.service.FolderService;
import com.earnlearn.service.UserService;

@RestController
@RequestMapping("/api/folders")
public class ApiFolderController {

    private final FolderService folderService;
    private final UserService userService;

    public ApiFolderController(FolderService folderService, UserService userService) {
        this.folderService = folderService;
        this.userService = userService;
    }

    /**
     * Retrieves the currently authenticated user from the database.
     * Throws a RuntimeException if user details are not available or the user is not found.
     * @param userDetails The authenticated user's details provided by Spring Security.
     * @return The User object corresponding to the authenticated user.
     */
    private User getCurrentUser(UserDetails userDetails) {
        // Ensure userDetails is not null before attempting to get username
        if (userDetails == null) {
            throw new RuntimeException("User details not available. Ensure the endpoint is secured and the user is authenticated.");
        }
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found for username: " + userDetails.getUsername()));
    }

    /**
     * Handles the creation of a new folder.
     * It consumes JSON requests and saves the folder associated with the authenticated user.
     * The 'consumes' attribute is set to MediaType.APPLICATION_JSON_VALUE to ensure
     * that the endpoint correctly processes JSON requests.
     *
     * @param folder The Folder object sent in the request body.
     * @param userDetails The authenticated user's details.
     * @return The saved Folder object.
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8"}) // MODIFIED: Added "application/json;charset=UTF-8" to supported media types
    public Folder createFolder(@RequestBody Folder folder, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails); // Get the current user
        // Pass both the folder and the user to the saveFolder method
        return folderService.saveFolder(folder, currentUser); // Pass currentUser as the second argument
    }

    /**
     * Retrieves all folders belonging to the authenticated user.
     *
     * @param userDetails The authenticated user's details.
     * @return A list of Folder objects.
     */
    @GetMapping
    public List<Folder> getUserFolders(@AuthenticationPrincipal UserDetails userDetails) {
        return folderService.getUserFolders(getCurrentUser(userDetails));
    }

    /**
     * Deletes a folder by its ID for the authenticated user.
     *
     * @param id The ID of the folder to delete.
     * @param userDetails The authenticated user's details.
     * @return ResponseEntity with no content if successful.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        folderService.deleteFolder(id, getCurrentUser(userDetails));
        return ResponseEntity.noContent().build();
    }
}
