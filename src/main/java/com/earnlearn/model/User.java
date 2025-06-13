package com.earnlearn.model;

import java.time.LocalDate;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	@NotBlank
	@Size(min = 3, max = 20)
	private String username;

	@Column(nullable = false)
	@NotBlank
	private String name;

	@Column(nullable = false)
	@NotBlank
	@Size(min = 6)
	private String password;

	@Email
	@NotBlank
	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "date_of_birth")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate dateOfBirth;

	@Column(name = "gender")
	private String gender;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-notes-ref")
	private List<Note> notes = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference("user-folders-ref")
	private List<Folder> folders = new ArrayList<>();

	// One-to-one relationship with SellerProfile
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private SellerProfile sellerProfile;

	// NEW: Add enrollments to User
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Enrollment> enrollments = new ArrayList<>();

	// Setter Getter
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

	public List<Folder> getFolders() {
		return folders;
	}

	public void setFolders(List<Folder> folders) {
		this.folders = folders;
	}

	// Getter for SellerProfile
	public SellerProfile getSellerProfile() {
		return sellerProfile;
	}

	// Setter for SellerProfile
	public void setSellerProfile(SellerProfile sellerProfile) {
		if (sellerProfile == null) {
			if (this.sellerProfile != null) {
				this.sellerProfile.setUser(null); // Unlink old profile
			}
		} else {
			sellerProfile.setUser(this); // Link new profile to this user
		}
		this.sellerProfile = sellerProfile;
	}

	public boolean isSeller() {
		return this.roles.stream().anyMatch(role -> "SELLER".equals(role.getName()));
	}

	// NEW: Getter and Setter for enrollments
	public List<Enrollment> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(List<Enrollment> enrollments) {
		this.enrollments = enrollments;
	}

	public boolean hasRole(String roleName) {
		return this.roles.stream().anyMatch(role -> role.getName().equals(roleName));
	}
}
