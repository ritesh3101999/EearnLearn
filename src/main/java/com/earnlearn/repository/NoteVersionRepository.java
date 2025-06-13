package com.earnlearn.repository;

import com.earnlearn.model.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {
}