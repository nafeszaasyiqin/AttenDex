package com.example.Attendex;

import com.example.Attendex.model.UserEntity;
import com.example.Attendex.repo.UserRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final UserRepo userEntityRepository;

    public SecurityConfig(UserRepo userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    // Remove the BCryptPasswordEncoder since we're using plain text passwords
    // No need for password encoding for simple comparison
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers("/admin/admin-dashboard").hasRole("ADMIN")  // Only ADMIN role can access /admin
                                .requestMatchers("/lecturer/lecturer").hasRole("LECTURER")
                                .requestMatchers("/student/dashboard").hasRole("STUDENT")
                                .anyRequest().authenticated()  // Require authentication for all other requests
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .successHandler(customSuccessHandler()) // Attach custom success handler
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                );
        return http.build();
    }


    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.core.Authentication authentication) -> {
            String username = authentication.getName();  // Get the username of the logged-in user
            System.out.println("Logged-in user: " + username);  // Log the username for debugging

            String role = authentication.getAuthorities().iterator().next().getAuthority();
            System.out.println("Role retrieved: " + role); // Debug role retrieval

            // Handle redirection based on role
            if ("ROLE_ADMIN".equals(role)) {
                System.out.println("Redirecting to /admin/admin-dashboard");
                response.sendRedirect("/admin/admin-dashboard");
            } else if ("ROLE_LECTURER".equals(role)) {
                System.out.println("Redirecting to /lecturer");
                response.sendRedirect("/lecturer/lecturer");
            } else if ("ROLE_STUDENT".equals(role)) {
                System.out.println("Redirecting to /student");
                response.sendRedirect("/student/dashboard");
            } else {
                System.out.println("Redirecting to /welcome");
                response.sendRedirect("/welcome");
            }
        };
    }


    // Implement UserDetailsService to directly compare plain text passwords
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserEntity userEntity = userEntityRepository.findById(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if the role has the "ROLE_" prefix, and remove it if necessary
            String role = userEntity.getRole();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5); // Remove "ROLE_" prefix
            }

            return User.builder()
                    .username(userEntity.getUsername())
                    .password(userEntity.getPassword()) // Plain text password stored in the DB
                    .roles(role)  // Ensure role is passed correctly without "ROLE_" prefix
                    .build();
        };
    }

    @Bean
    public NoOpPasswordEncoder passwordEncoder() {
        // Since we're using plain text passwords, return NoOpPasswordEncoder
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }


}
