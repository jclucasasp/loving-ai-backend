package loving.ai.config;

import loving.ai.user.User;
import loving.ai.user.UserRepo;
import loving.ai.utils.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final Environment environment;

    // Remove String activeProfile — use environment.getActiveProfiles()
    public WebSecurityConfig(JwtAuthenticationFilter jwtFilter, Environment environment) {
        this.jwtFilter = jwtFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/create", "/api/user/otp", "/api/user/verify", "/api/user/reset",
                                "/api/user/login", "/api/user/refresh", "api/test",
                                "/images/**", "/v3/api-docs/**", "/swagger**",
                                "/api/personality/types", "/api/personality/descriptions"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(h -> h.cacheControl(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        configuration.setAllowedOrigins(isDev
                ? List.of("http://localhost:5173", "https://loving-ai.com", "https://www.loving-ai.com")
                : List.of("https://loving-ai.com", "https://www.loving-ai.com"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepo userRepo) {
        return email -> {
            User user = userRepo.getUserByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.email())
                    .password(user.password())
                    .roles(user.roles() != null ? user.roles().toArray(String[]::new) : new String[]{"USER"})
                    .accountExpired(user.end_date() != null)
                    .accountLocked(false)  // Add if you have lock logic
                    .credentialsExpired(user.passwordResetDate() != null /* + expiry check */)
                    .disabled(user.active() == null || !user.active())
                    .build();
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
