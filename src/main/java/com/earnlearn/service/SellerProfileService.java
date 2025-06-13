package com.earnlearn.service;

import com.earnlearn.model.SellerProfile;
import com.earnlearn.model.User;
import com.earnlearn.repository.SellerProfileRepository;
import com.earnlearn.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellerProfileService {

	@Autowired
	private SellerProfileRepository sellerProfileRepository;

	@Autowired
	private UserRepository userRepository;

	public Optional<SellerProfile> getSellerProfileById(Long id) {
		return sellerProfileRepository.findById(id);
	}

	public Optional<SellerProfile> getSellerProfileByUserId(Long userId) {
		return sellerProfileRepository.findByUserId(userId);
	}

	@Transactional
	public SellerProfile saveSellerProfile(Long userId, SellerProfile profileDetails) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

		Optional<SellerProfile> existingProfile = sellerProfileRepository.findByUser(user);

		SellerProfile profileToSave;
		if (existingProfile.isPresent()) {
			// Update existing profile
			profileToSave = existingProfile.get();
			profileToSave.setDescription(profileDetails.getDescription());
			profileToSave.setLocation(profileDetails.getLocation());
			profileToSave.setWebsiteUrl(profileDetails.getWebsiteUrl());
			profileToSave.setSocialMediaLinks(profileDetails.getSocialMediaLinks());
			profileToSave.setProfilePictureUrl(profileDetails.getProfilePictureUrl());
			// Ensure the link back to user is maintained
			profileToSave.setUser(user);
		} else {
			// Create new profile
			profileToSave = profileDetails;
			profileToSave.setUser(user); // Link the profile to the user
		}
		return sellerProfileRepository.save(profileToSave);
	}

	@Transactional
	public void deleteSellerProfile(Long id) {
		sellerProfileRepository.deleteById(id);
	}
}