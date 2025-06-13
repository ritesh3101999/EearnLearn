package com.earnlearn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Note {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Lob // Indicates a large object, suitable for long text content
	private String content;

	private boolean bookmarked;

	private boolean deleted = false; // Soft delete flag

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToOne
	@JsonBackReference("user-notes-ref") // Matches User.notes
	private User user;

	@ManyToOne
	@JsonBackReference("folder-notes-ref") // Matches Folder.notes
	private Folder folder;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
	@Column(name = "tag")
	private List<String> tags = new ArrayList<>();

	@OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("versionNumber DESC") // Add ordering
	@JsonManagedReference("note-versions-ref") // Unique name for Note-NoteVersion relationship
	private List<NoteVersion> versions = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	// Getter Setter

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isBookmarked() {
		return bookmarked;
	}

	public void setBookmarked(boolean bookmarked) {
		this.bookmarked = bookmarked;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<NoteVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<NoteVersion> versions) {
		this.versions = versions;
	}

}
