package com.earnlearn.service;

import com.earnlearn.model.Activity;
import com.earnlearn.model.User;
import com.earnlearn.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    public void createActivity(User user, String type, String description, String link) {
        Activity activity = new Activity(user, type, description, link);
        activityRepository.save(activity);
    }

    public List<Activity> getRecentActivities(User user, int limit) {
        return activityRepository.findByUserOrderByTimestampDesc(user, PageRequest.of(0, limit));
    }
}