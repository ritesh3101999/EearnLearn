package com.earnlearn.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class GlobalErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorController.class);

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMsg = "An unexpected error occurred.";
        String detailedMsg = "We are sorry, but an unexpected error has occurred. Please try again later or contact support if the problem persists.";
        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        // Log the exception for internal debugging
        if (throwable != null) {
            logger.error("Error encountered: " + detailedMsg, throwable);
            // Optionally, for development, you might include stack trace in detailedMsg:
            // detailedMsg += "\nStack Trace: " + org.springframework.util.ExceptionUtils.getStackTrace(throwable);
        }

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorMsg = "Page Not Found (404)";
                detailedMsg = "The page you are looking for does not exist. It might have been moved or deleted.";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorMsg = "Internal Server Error (500)";
                detailedMsg = "Something went wrong on our side. We are working to fix it. Please try again later.";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorMsg = "Access Denied (403)";
                detailedMsg = "You do not have permission to access this page. Please ensure you are logged in with the correct privileges.";
                model.addAttribute("pageTitle", "Access Denied");
                return "error/access-denied"; // Specific template for 403
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) { // Added 400 Bad Request
                errorMsg = "Bad Request (400)";
                detailedMsg = "The request you sent was invalid. Please check your input and try again.";
            } else {
                errorMsg = "Error " + statusCode;
                detailedMsg = "An unexpected error with status " + statusCode + " occurred.";
            }
        }

        model.addAttribute("errorCode", status != null ? status.toString() : "N/A");
        model.addAttribute("errorMessage", errorMsg);
        model.addAttribute("detailedMessage", detailedMsg);
        model.addAttribute("pageTitle", "Error");
        return "error/general-error"; // General error template
    }

    @GetMapping("/access-denied")
    public String handleAccessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied");
        model.addAttribute("errorMessage", "Access Denied (403)");
        model.addAttribute("detailedMessage", "You do not have the necessary permissions to view this page.");
        return "error/access-denied";
    }
}
