package water_billing_platform.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import water_billing_platform.security.JwtAuthFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =========================
                // PUBLIC
                // =========================
            		.requestMatchers("/auth/login", "/auth/register", "/auth/forgot-password", "/auth/reset-password").permitAll()


                // =========================
                // SUPER ADMIN
                // =========================
                .requestMatchers(
                    "/users/pending-admins",
                    "/users/*/approve",
                    "/users/*/reject"
                ).hasRole("SUPER_ADMIN")

                .requestMatchers(
                    "/users/commercial-admins"
                ).hasRole("SUPER_ADMIN")


                // =========================
                // USERS / RESIDENTS
                // =========================
                .requestMatchers(
                    "/users/residents/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )

                .requestMatchers(
                    "/users/profile/**"
                ).authenticated()


                // =========================
                // APARTMENTS
                // =========================
                .requestMatchers(
                    "/apartments/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )


                // =========================
                // HOUSEHOLDS
                // =========================
                .requestMatchers(
                    "/households/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )


                // =========================
                // ADMIN ASSIGNMENTS
                // =========================
                .requestMatchers(
                    "/admin-assignments/**"
                ).hasRole("SUPER_ADMIN")


                // =========================
                // TARIFF PLANS
                // =========================
                .requestMatchers(
                    "/tariff-plans/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )


                // =========================
                // SHARED EXPENSES
                // =========================
                .requestMatchers(
                    "/shared-expenses/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )


                // =========================
                // USAGE LOGS
                // =========================
                .requestMatchers(
                    "/usage-logs/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )


                // =========================
                // BILLS
                // =========================
                .requestMatchers(
                    "/bills/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )


                // =========================
                // ALERTS
                // =========================
                .requestMatchers(
                    "/alerts/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )


                // =========================
                // COMPLAINTS
                // =========================

                // Resident + Commercial Admin + Super Admin
                // can create complaints
                .requestMatchers(
                    HttpMethod.POST,
                    "/complaints"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )

                // Household complaints
                .requestMatchers(
                    HttpMethod.GET,
                    "/complaints/household/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )

                // Commercial Admin + Super Admin
                // can view apartment complaints
                .requestMatchers(
                    HttpMethod.GET,
                    "/complaints/apartment/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )

                // Only Super Admin can see ALL complaints
                .requestMatchers(
                    HttpMethod.GET,
                    "/complaints/all"
                ).hasRole("SUPER_ADMIN")

                // Admins can resolve complaints
                .requestMatchers(
                    HttpMethod.POST,
                    "/complaints/*/status"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )


                // =========================
                // NOTIFICATIONS
                // =========================
                .requestMatchers(
                    "/notifications/household/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )

                .requestMatchers(
                    "/notifications/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )


                // =========================
                // PAYMENTS
                // =========================
                .requestMatchers(
                    "/bills/payments/household/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )

                .requestMatchers(
                    "/bills/payments/apartment/**"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )

                .requestMatchers(
                    "/bills/record-payment"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN"
                )

                .requestMatchers(
                    "/bills/pay"
                ).hasAnyRole(
                    "SUPER_ADMIN",
                    "COMMERCIAL_ADMIN",
                    "RESIDENT"
                )


                // =========================
                // RESTORE
                // =========================
                .requestMatchers(
                    "/apartments/*/restore"
                ).hasRole("SUPER_ADMIN")

                .requestMatchers(
                    "/users/commercial-admins/*/restore",
                    "/users/residents/*/restore"
                ).hasRole("SUPER_ADMIN")

                .requestMatchers(
                    "/households/*/restore"
                ).hasRole("SUPER_ADMIN")
                
                .requestMatchers("/scheduled-tasks/**").hasRole("SUPER_ADMIN")

                .requestMatchers(
                    "/bills/*/restore"
                ).hasRole("SUPER_ADMIN")


                // =========================
                // TRASH
                // =========================
                .requestMatchers(
                    "/trash/**"
                ).authenticated()


                // =========================
                // EVERYTHING ELSE
                // =========================
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }


    // =========================
    // CORS
    // =========================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }
}