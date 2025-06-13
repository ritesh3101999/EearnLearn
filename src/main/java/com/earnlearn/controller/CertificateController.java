package com.earnlearn.controller;

import com.earnlearn.model.Certificate;
import com.earnlearn.model.User;
import com.earnlearn.service.CertificateService;
import com.earnlearn.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateService certificateService;
    private final UserService userService;

    public CertificateController(CertificateService certificateService, UserService userService) {
        this.certificateService = certificateService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User details not available.");
        }
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found for username: " + userDetails.getUsername()));
    }

    @GetMapping("/my-certificates")
    public String listMyCertificates(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        List<Certificate> certificates = certificateService.getCertificatesForUser(currentUser);
        model.addAttribute("certificates", certificates);
        model.addAttribute("pageTitle", "My Certificates");
        return "certificate/list";
    }

    @GetMapping("/view/{uid}")
    public String viewCertificate(@PathVariable String uid, Model model) {
        Certificate certificate = certificateService.getCertificateByUid(uid)
                .orElseThrow(() -> new RuntimeException("Certificate not found with ID: " + uid));
        model.addAttribute("certificate", certificate);
        model.addAttribute("pageTitle", "View Certificate");
        return "certificate/view";
    }
}