package com.pittrack.pittrack.account;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private final LoginAttemptService loginAttemptService;

    public LoginRateLimitFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Filters login requests to enforce rate limiting.
     *
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @param filterChain the filter chain.
     * @throws ServletException if a servlet error occurs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod()) || !"/login".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        String teamNumber = request.getParameter("username");

        if (teamNumber == null || teamNumber.isBlank()) {
            teamNumber = "unknown";
        }

        if (teamNumber.length() > 32) {
            teamNumber = teamNumber.substring(0, 32);
        }

        long retryAfter = loginAttemptService.getRetryAfter(ip, teamNumber);

        if (retryAfter > 0) {
            logger.warn(
                "Login blocked by rate limiter. IP: {}, Team: {}, Retry: {} seconds", ip, maskTeamNumber(teamNumber), retryAfter);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.sendRedirect(request.getContextPath() + "/login?rateLimit");

            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Masks the team number for logging purposes.
     *
     * @param teamNumber the team number to mask.
     * @return the masked team number.
     */
    private String maskTeamNumber(String teamNumber) {
        if (teamNumber == null || teamNumber.length() <= 2) {
            return "**";
        }

        return "*".repeat(teamNumber.length() - 2)
            + teamNumber.substring(teamNumber.length() - 2);
    }
}
