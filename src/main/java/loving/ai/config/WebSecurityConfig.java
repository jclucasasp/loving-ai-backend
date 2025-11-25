package loving.ai.config;

import loving.ai.user.User;
import loving.ai.user.UserRepo;
import loving.ai.utils.JwtAuthenticationFilter;
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
    private final UserRepo userRepo;               // <-- inject only what you really need
    private final PasswordEncoder passwordEncoder; // <-- we'll create it here

    public WebSecurityConfig(JwtAuthenticationFilter jwtFilter, Environment environment, UserRepo userRepo) {
        this.jwtFilter = jwtFilter;
        this.environment = environment;
        this.userRepo = userRepo;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(s -> s.requireExplicitSave(false))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/user/create", "/api/user/otp", "/api/user/verify", "/api/user/reset",
                    "/api/user/login", "/api/user/refresh",
                    "/api/personality/types", "/api/personality/descriptions",
                    "/images/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider()) // <-- this is fine now
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //TODO: need to check for user verification flag from profiles
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepo.getUserByEmail(email)
            .map(user -> org.springframework.security.core.userdetails.User
                .withUsername(user.email())
                .password(user.password())
                .roles(user.roles() != null ? user.roles().toArray(String[]::new) : new String[]{"USER"})
                .accountExpired(user.end_date() != null)
                .accountLocked(user.end_date() != null)
                .credentialsExpired(user.end_date() != null)
                .disabled(user.active() == null || !user.active())
                .build()
            )
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    // This bean uses the UserDetailsService bean above – no cycle
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        config.setAllowedOrigins(isDev
            ? List.of("http://localhost:5173", "https://loving-ai.com", "https://www.loving-ai.com")
            : List.of("https://loving-ai.com", "https://www.loving-ai.com"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}