package loving.ai.utils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import loving.ai.user.UserRepo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info(">>> JwtAuthenticationFilter is running for {}", request.getRequestURI());
        // ONLY CHANGE: read access_token from cookie, NOT from Bearer header
        String token = extractToken(request);

        if (token != null) {
            try {
                Claims claims = jwtUtil.parse(token);
                String email = claims.getSubject();

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userRepo.getUserByEmail(email).ifPresent(user -> {
                        if (jwtUtil.valid(token, email) && (user.active() == null || user.active())) {

                            var authorities = (user.roles() != null ? user.roles() : Set.of("USER"))
                                    .stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .toList();

                            UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);
                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    });
                }
            } catch (Exception e) {
               log.warn("Invalid JWT token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("authorization");
        log.info("Bearer token: [{}]",bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
