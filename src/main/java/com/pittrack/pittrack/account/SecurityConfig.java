package com.pittrack.pittrack.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

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
    UserDetailsService userDetailsService(
            TeamAccountRepository repository) {

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

    /** Returns a security filter chain for configuring security settings.
     * 
     * @param http the HTTP security configuration
     * @return SecurityFilterChain instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

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
                .defaultSuccessUrl("/items", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}