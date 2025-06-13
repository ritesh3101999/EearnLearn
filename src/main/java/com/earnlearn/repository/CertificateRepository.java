package com.earnlearn.repository;

import com.earnlearn.model.Certificate;
import com.earnlearn.model.Course;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCertificateUid(String certificateUid);

    List<Certificate> findByUserOrderByIssueDateDesc(User user);

    boolean existsByUserAndCourse(User user, Course course);
}