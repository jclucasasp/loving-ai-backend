package loving.ai.utils;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import loving.ai.user.UserRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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
                log.debug("Claims: [{}]", claims);
                String email = claims.getSubject();
                log.debug("Email: [{}]", email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userRepo.getUserByEmail(email).ifPresent(user -> {
                        if (jwtUtil.valid(token, email))  {

                            var authorities = (user.roles() != null ? user.roles() : Set.of("USER"))
                                    .stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .toList();

                            log.debug("User: [{}]", user);
                            log.debug("Authorities: [{}]", authorities);

                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);

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

    //TODO: Change to extract cookies.
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7).trim();

            // Remove surrounding quotes and brackets if present (your frontend bug)
            if (token.startsWith("[\"") && token.endsWith("\"]")) {
                token = token.substring(2, token.length() - 2);
            } else if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }

            log.debug("Extracted clean token: {}", token);
            return token;
        }
        return null;
    }
}