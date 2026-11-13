package com.campusconnect.config;

import com.campusconnect.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ---------- Public: Auth endpoints ----------
                .requestMatchers(HttpMethod.POST,
                    "/users/signup", "/users/login", "/users/verify-otp", "/users/resend-otp"
                ).permitAll()

                // ---------- Public: GET-only browsing ----------
                .requestMatchers(HttpMethod.GET,
                    "/events", "/events/**",
                    "/colleges", "/colleges/**",
                    "/venues", "/venues/**",
                    "/event-categories", "/event-categories/**",
                    "/home-categories", "/home-categories/**",
                    "/event-media/**",
                    "/reviews/event/**"
                ).permitAll()

                // ---------- Admin-only writes ----------
                .requestMatchers(HttpMethod.POST, "/colleges", "/event-categories", "/home-categories").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/colleges/**", "/venues/**", "/event-categories/**", "/home-categories/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/colleges/**", "/venues/**", "/event-categories/**", "/home-categories/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/vendors/*/approve", "/vendors/*/reject").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/events/*/remove").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/reports/*/resolve").hasAuthority("ADMIN")
                .requestMatchers("/vendors/pending").hasAuthority("ADMIN")
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/payments").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/users", "/users/admins").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasAuthority("ADMIN")

                // ---------- Vendor-only ----------
                .requestMatchers(HttpMethod.POST, "/events").hasRole("VENDOR")
                .requestMatchers(HttpMethod.PUT, "/events/**").hasRole("VENDOR")
                .requestMatchers(HttpMethod.POST, "/venues").hasAnyAuthority("ADMIN", "ROLE_VENDOR")
                .requestMatchers("/vendors/*/earnings", "/vendors/*/analytics").hasRole("VENDOR")
                .requestMatchers(HttpMethod.POST, "/upload/event/**").hasRole("VENDOR")
                .requestMatchers(HttpMethod.POST, "/upload/vendor/**").hasRole("VENDOR")

                // ---------- Everything else requires a valid JWT ----------
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}