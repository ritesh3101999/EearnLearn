package com.earnlearn.service;

import com.earnlearn.model.Payment;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class QRCodeService {
    
    public String generatePaymentQR(Payment payment) {
        String upiUrl = String.format(
            "upi://pay?pa=%s&pn=%s&am=%.2f&cu=INR",
            URLEncoder.encode("ritesh3101999@okicici", StandardCharsets.UTF_8),
            URLEncoder.encode("EarnLearn Payment", StandardCharsets.UTF_8),
            payment.getAmount()
        );

        return generateQRCodeImage(upiUrl);
    }

    private String generateQRCodeImage(String upiUrl) {
        try {
            return "https://api.qrserver.com/v1/create-qr-code/?" +
                   "size=300x300" +
                   "&data=" + URLEncoder.encode(upiUrl, StandardCharsets.UTF_8) +
                   "&ecc=L" +  // Error correction level
                   "&margin=10" +
                   "&format=png";
        } catch (Exception e) {
            // Fallback to placeholder if encoding fails
            return "<svg width='300' height='300'><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle'>QR Error</text></svg>";
        }
    }
}