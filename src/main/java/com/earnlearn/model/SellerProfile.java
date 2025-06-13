package com.earnlearn.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seller_profiles")
public class SellerProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// One-to-one relationship with User
	// The user_id column in seller_profiles table will be the foreign key
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
	private User user;

	@Lob // For larger text content, suitable for descriptions
	@Column(columnDefinition = "TEXT")
	private String description;

	private String location;

	private String websiteUrl;

	private String socialMediaLinks;

	private String profilePictureUrl;

	// Setter Getter

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public String getSocialMediaLinks() {
		return socialMediaLinks;
	}

	public void setSocialMediaLinks(String socialMediaLinks) {
		this.socialMediaLinks = socialMediaLinks;
	}

	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}

	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}

}