package com.earnlearn.service;

import com.earnlearn.model.LiveSession;
import com.earnlearn.model.User;
import com.earnlearn.repository.LiveSessionRepository;
import com.earnlearn.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LiveSessionService {

	private final LiveSessionRepository liveSessionRepository;
	private final UserRepository userRepository;

	public LiveSessionService(LiveSessionRepository liveSessionRepository, UserRepository userRepository) {
		this.liveSessionRepository = liveSessionRepository;
		this.userRepository = userRepository;
	}

	public List<LiveSession> getAllLiveSessions() {
		return liveSessionRepository.findAllByOrderByStartTimeAsc();
	}

	public Optional<LiveSession> getLiveSessionById(Long id) {
		return liveSessionRepository.findById(id);
	}

	@Transactional
	public LiveSession saveLiveSession(LiveSession liveSession, String instructorUsername) {
		User instructor = userRepository.findByUsername(instructorUsername)
				.orElseThrow(() -> new RuntimeException("Instructor not found with username: " + instructorUsername));

		liveSession.setInstructor(instructor);

		// Basic validation: ensure end time is after start time
		if (liveSession.getStartTime().isAfter(liveSession.getEndTime())) {
			throw new IllegalArgumentException("End time must be after start time.");
		}

		return liveSessionRepository.save(liveSession);
	}

	@Transactional
	public void deleteLiveSession(Long id) {
		if (!liveSessionRepository.existsById(id)) {
			throw new RuntimeException("Live Session not found with ID: " + id);
		}
		liveSessionRepository.deleteById(id);
	}

	public List<LiveSession> getUpcomingLiveSessions() {
		return liveSessionRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());
	}

	public List<LiveSession> getLiveSessionsByInstructor(User instructor) {
		return liveSessionRepository.findByInstructorOrderByStartTimeAsc(instructor);
	}

	public List<LiveSession> searchLiveSessions(String keyword) {
		return liveSessionRepository.findByTitleContainingIgnoreCaseOrderByStartTimeAsc(keyword);
	}
}