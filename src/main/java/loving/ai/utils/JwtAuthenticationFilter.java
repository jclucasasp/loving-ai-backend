package loving.ai.utils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import loving.ai.user.UserRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepo userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        log.debug(">>> JwtAuthenticationFilter is running for {}", request.getRequestURI());

        String token = extractToken(request);

        if (token != null) {
            try {
                Claims claims = jwtUtil.parse(token);
                log.debug("Claims: [{}]", claims);
                String email = claims.getSubject();
                log.debug("Email: [{}]", email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("Finding user by email: [{}]", email);
                    userRepo.getUserByEmail(email).ifPresent(user -> {
                        log.debug("Checking if the token is valid...");
                        if (jwtUtil.valid(token, email)) {
                            log.debug("Token is valid. Creating authentication object...");
                            var authorities = (user.roles() != null ? user.roles() : Set.of("USER"))
                                    .stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .toList();

                            log.debug("User: [{}]", user);
                            log.debug("Authorities: [{}]", authorities);
                            // This is for Spring Security to know that the user is authenticated
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);

                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            log.debug("Auth object details set: [{}]", auth);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            log.debug("Authentication object set in SecurityContextHolder");
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
            }
        }
        log.debug("JWT Authentication Filter executing...");
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            log.debug("Extracting token from header: {}", header);
            return header.substring(7).trim();
        } else {
            log.debug("Attempting to extract from cookies...");
            return Optional.ofNullable(WebUtils.getCookie(request, TokenType.refreshToken.name()))
                    .map(Cookie::getValue)
                    .orElse(null);
        }
    }
}