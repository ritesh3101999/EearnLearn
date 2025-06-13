package com.earnlearn.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "folders")
public class Folder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonBackReference("user-folders-ref") 
	private User user;

	@OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
	@SQLRestriction("deleted = false")
	@JsonManagedReference("folder-notes-ref") // Unique name for Folder-Note relationship
	private List<Note> notes = new ArrayList<>();

	public int getActiveNotesCount() {
		return this.notes.size(); // Works with @SQLRestriction("deleted = false")
	}

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

}