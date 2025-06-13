package com.earnlearn.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null); // For compatibility with some frontend setups if not using Thymeleaf's _csrf directly

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/api/public/**") // Assuming API public endpoints don't need CSRF
            )
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/", "/dashboard", "/users/login", "/users/register", "/home").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    .requestMatchers("/courses/**", "/lectures/**").permitAll()
                    .requestMatchers("/enrollments/my-courses", "/enrollments/enroll/**", "/enrollments/drop/**", "/enrollments/complete/**").authenticated()
                    .requestMatchers("/live-sessions", "/live-sessions/{id}").permitAll() // Allow all to view list and detail
                    .requestMatchers("/live-sessions/new", "/live-sessions/save", "/live-sessions/edit/**", "/live-sessions/delete/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                    .requestMatchers("products/new").hasAnyRole("SELLER", "ADMIN")
                    .requestMatchers("/cart/**", "/orders/**", "/marketplace", "/payments/**", "/products/**").authenticated()
                    .requestMatchers("/profile", "/profile/**").authenticated()
                    .requestMatchers("/notes", "/notes/**", "/folders", "/folders/**").authenticated()
                    .requestMatchers("/api/notes/**", "/api/folders/**").authenticated()
                    .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/certificates/view/**").permitAll() // Publicly verifiable certificate
                    .requestMatchers("/certificates/my-certificates").authenticated() // User's own certificates
                    // NEW: Seller specific order management paths
                    .requestMatchers("/orders/seller/**").hasRole("SELLER")
                    .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .anyRequest().authenticated() // All other requests need authentication
             )
            .formLogin(form -> form
                .loginPage("/dashboard")
                .loginProcessingUrl("/api/users/login")
                .successHandler(dynamicLoginSuccessHandler())
                .failureHandler(apiLoginFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/users/logout")
                .logoutSuccessUrl("/dashboard?logout")
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                 .accessDeniedPage("/access-denied")
            );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080",
            "http://192.168.55.152:8080"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "*", // Allows all standard headers
            "X-Requested-With",
            "X-XSRF-TOKEN",
            "Content-Type",
            "Authorization"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Set-Cookie",
            "XSRF-TOKEN",
            "Authorization"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private AuthenticationSuccessHandler dynamicLoginSuccessHandler() {
        return (request, response, authentication) -> {
            if (isApiRequest(request)) {
                response.setContentType("application/json");
                response.getWriter().write("{\"status\": \"success\", \"message\": \"Login successful\"}");
            } else {
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
                boolean isSeller = authentication.getAuthorities().stream()
                                .anyMatch(ga -> ga.getAuthority().equals("ROLE_SELLER"));

                if (isAdmin) {
                    response.sendRedirect("/admin/dashboard");
                } else if (isSeller) {
                    response.sendRedirect("/orders/seller"); // Redirect sellers to their order management page
                } else {
                    response.sendRedirect("/home");
                }
            }
        };
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
               (request.getHeader("User-Agent") != null && request.getHeader("User-Agent").contains("smartnote-android"));
    }

    private AuthenticationFailureHandler apiLoginFailureHandler() {
        return (request, response, exception) -> {
            if (isApiRequest(request)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid credentials\", \"message\": \"" + exception.getMessage() + "\"}");
            } else {
                request.getSession().setAttribute("errorMessage", "Login failed: " + exception.getMessage());
                response.sendRedirect("/dashboard?error");
            }
        };
    }
}
