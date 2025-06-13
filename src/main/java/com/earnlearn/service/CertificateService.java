package com.earnlearn.service;

import com.earnlearn.model.Certificate;
import com.earnlearn.model.Course;
import com.earnlearn.model.User;
import com.earnlearn.repository.CertificateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Transactional
    public Certificate generateCertificate(User user, Course course) {
        // Check if a certificate already exists for this user and course
        if (certificateRepository.existsByUserAndCourse(user, course)) {
            throw new IllegalStateException("A certificate for this course has already been issued to the user.");
        }
        Certificate certificate = new Certificate(user, course);
        return certificateRepository.save(certificate);
    }

    public Optional<Certificate> getCertificateByUid(String uid) {
        return certificateRepository.findByCertificateUid(uid);
    }

    public List<Certificate> getCertificatesForUser(User user) {
        return certificateRepository.findByUserOrderByIssueDateDesc(user);
    }
}