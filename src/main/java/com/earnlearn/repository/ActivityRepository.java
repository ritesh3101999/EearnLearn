package com.earnlearn.repository;

import com.earnlearn.model.Activity;
import com.earnlearn.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserOrderByTimestampDesc(User user, Pageable pageable);
}