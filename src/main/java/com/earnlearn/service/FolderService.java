package com.earnlearn.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.earnlearn.model.Folder;
import com.earnlearn.model.User;
import com.earnlearn.repository.FolderRepository;

@Service
public class FolderService {
	private final FolderRepository folderRepository;

	public FolderService(FolderRepository folderRepository) {
		this.folderRepository = folderRepository;
	}

	public Folder saveFolder(Folder folder, User user) {
		folder.setUser(user);
		return folderRepository.save(folder);
	}

	public void deleteFolder(Long id, User user) {
		Folder folder = folderRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new RuntimeException("Folder not found"));
		folderRepository.delete(folder);
	}

	public List<Folder> getUserFolders(User user) {
        return folderRepository.findByUserWithNotes(user);
    }

	public Folder getFolderByIdAndUser(Long id, User user) {
		return folderRepository.findByIdAndUser(id, user).orElseThrow(() -> new RuntimeException("Folder not found"));
	}
}
