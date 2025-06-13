package com.earnlearn.repository;

import com.earnlearn.model.LiveSession;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

    // Find all live sessions ordered by start time
    List<LiveSession> findAllByOrderByStartTimeAsc();

    // Find live sessions by instructor
    List<LiveSession> findByInstructorOrderByStartTimeAsc(User instructor);

    // Find live sessions starting after a certain time
    List<LiveSession> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime time);

    // Find live sessions by title containing a keyword
    List<LiveSession> findByTitleContainingIgnoreCaseOrderByStartTimeAsc(String keyword);
}