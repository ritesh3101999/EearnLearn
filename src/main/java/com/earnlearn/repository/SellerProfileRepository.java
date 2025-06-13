package com.earnlearn.repository;

import com.earnlearn.model.SellerProfile;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    // Find a seller profile by the associated User entity
    Optional<SellerProfile> findByUser(User user);

    // Find a seller profile by the associated user's ID
    Optional<SellerProfile> findByUserId(Long userId);
}