package com.pittrack.pittrack.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /** Returns a password encoder for hashing passwords.
     * 
     * @return PasswordEncoder instance.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Returns a user details service for loading user information.
     * 
     * @param repository the team account repository
     * @return UserDetailsService instance.
     */
    @Bean
    UserDetailsService userDetailsService(TeamAccountRepository repository) {
        return teamNumber -> {
            TeamAccount team = repository
                    .findByTeamNumber(teamNumber)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Invalid team number or password."));

            return User.withUsername(team.getTeamNumber())
                    .password(team.getPasswordHash())
                    .roles("TEAM")
                    .build();
        };
    }

    /** Registers the login rate limit filter.
     * 
     * @param filter the login rate limit filter.
     * @return the filter registration bean.
     */
    @Bean
    FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilterRegistration(LoginRateLimitFilter filter) {
        FilterRegistrationBean<LoginRateLimitFilter> registration = new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }

    /** Returns a security filter chain for configuring security settings.
     * 
     * @param http the HTTP security configuration
     * @return SecurityFilterChain instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LoginRateLimitFilter loginRateLimitFilter, LoginAttemptService loginAttemptService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login",
                    "/register",
                    "/style.css",
                    "/js/**",
                    "/images/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")

                .successHandler((request, response, authentication) -> {
                    String ip = request.getRemoteAddr();

                    loginAttemptService.recordSuccess(ip, authentication.getName());

                    response.sendRedirect(request.getContextPath() + "/dashboard");
                })

                .failureHandler((request, response, exception) -> {
                    String ip = request.getRemoteAddr();

                    String teamNumber = request.getParameter("username");

                    if (teamNumber == null || teamNumber.isBlank()) {
                        teamNumber = "unknown";
                    }

                    if (teamNumber.length() > 32) {
                        teamNumber = teamNumber.substring(0, 32);
                    }

                    loginAttemptService.recordFailure(ip, teamNumber);
                    logger.info("Failed login attempt. IP: {}, Team: {}", ip, maskTeamNumber(teamNumber));

                    response.sendRedirect(request.getContextPath() + "/login?error");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        
        http.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Masks a team number for logging purposes.
     * 
     * @param teamNumber the team number to mask.
     * @return masked team number.
     */
    private String maskTeamNumber(String teamNumber) {
        if (teamNumber == null || teamNumber.length() <= 2) {
            return "**";
        }

        return "*".repeat(teamNumber.length() - 2)
            + teamNumber.substring(teamNumber.length() - 2);
    }
}
